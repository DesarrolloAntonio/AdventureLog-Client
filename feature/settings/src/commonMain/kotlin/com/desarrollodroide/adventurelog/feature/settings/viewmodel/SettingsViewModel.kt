package com.desarrollodroide.adventurelog.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.domain.repository.AccountError
import com.desarrollodroide.adventurelog.core.domain.repository.AccountRepository
import com.desarrollodroide.adventurelog.core.domain.repository.SettingsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.desarrollodroide.adventurelog.core.domain.usecase.RefreshVisitedRegionsUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import com.desarrollodroide.adventurelog.feature.settings.domain.BackupExporter
import com.desarrollodroide.adventurelog.feature.settings.domain.BackupResult

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val refreshVisitedRegionsUseCase: RefreshVisitedRegionsUseCase,
    private val backupExporter: BackupExporter
) : ViewModel() {

    private val _backupInProgress = MutableStateFlow(false)
    val backupInProgress: StateFlow<Boolean> = _backupInProgress.asStateFlow()

    /**
     * Downloads the account's backup zip and hands it to the share sheet.
     *
     * The endpoint is behind the same auth check as everything else, so the bytes are fetched with
     * the signed-in client and offered as a file - a plain URL would come back 403, and on a
     * server reachable only over Tailscale a link is no use to anyone anyway.
     */
    fun downloadBackup() {
        if (_backupInProgress.value) return
        val server = getServerUrl().trimEnd('/')
        if (server.isEmpty()) return

        _backupInProgress.value = true
        viewModelScope.launch {
            _regionsMessage.value = when (backupExporter.export(server, backupFileName())) {
                BackupResult.CouldNotDownload -> "Could not download the backup"
                BackupResult.NowhereToPutIt -> "Nothing on this device can take the file"
                BackupResult.Handed -> null
            }
            _backupInProgress.value = false
        }
    }

    // The date, without pulling kotlinx-datetime into this module for one filename.
    private fun backupFileName(): String {
        val stamp = Clock.System.now().toString().substringBefore('T')
        return "adventurelog-backup-$stamp.zip"
    }

    private val _regionsRefreshing = MutableStateFlow(false)
    val regionsRefreshing: StateFlow<Boolean> = _regionsRefreshing.asStateFlow()

    private val _regionsMessage = MutableStateFlow<String?>(null)
    val regionsMessage: StateFlow<String?> = _regionsMessage.asStateFlow()

    /**
     * Asks the server to work out which regions and cities the saved places actually fall in.
     * Places added before their coordinates were known never got counted, which is why the World
     * tab can read lower than the map looks.
     */
    fun refreshVisitedRegions() {
        if (_regionsRefreshing.value) return
        _regionsRefreshing.value = true
        viewModelScope.launch {
            when (val result = refreshVisitedRegionsUseCase()) {
                is Either.Right -> {
                    val (regions, cities) = result.value
                    _regionsMessage.value = when {
                        regions == 0 && cities == 0 -> "Everything was already up to date"
                        else -> buildString {
                            append(regions)
                            append(if (regions == 1) " new region" else " new regions")
                            if (cities > 0) {
                                append(", ")
                                append(cities)
                                append(if (cities == 1) " new city" else " new cities")
                            }
                        }
                    }
                }
                is Either.Left -> _regionsMessage.value = result.value
            }
            _regionsRefreshing.value = false
        }
    }

    fun clearRegionsMessage() {
        _regionsMessage.value = null
    }

    val themeMode = settingsRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.AUTO)

    val useDynamicColors = settingsRepository.getUseDynamicColors()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val user = userRepository.getUserSession()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _profile = MutableStateFlow(ProfileSectionState())
    val profile = _profile.asStateFlow()

    private val _emails = MutableStateFlow(EmailsSectionState())
    val emails = _emails.asStateFlow()

    private val _storage = MutableStateFlow(StorageSectionState())
    val storage = _storage.asStateFlow()

    private val _isChangingPassword = MutableStateFlow(false)
    val isChangingPassword = _isChangingPassword.asStateFlow()

    /**
     * One-shot results, so a message is shown once rather than replayed on every recomposition.
     */
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            userRepository.getUserSession().collect { details ->
                if (details != null) seedProfile(details)
            }
        }
        loadEmails()
        loadStorage()
    }

    /**
     * Re-seed the form from the session, but only where the user has not typed: a save elsewhere
     * (or the refresh that follows one) must not wipe a field being edited.
     */
    private fun seedProfile(details: UserDetails) {
        val fromServer = ProfileForm.from(details)
        _profile.update { state ->
            state.copy(
                form = if (state.hasChanges) state.form else fromServer,
                saved = fromServer
            )
        }
    }

    /**
     * Apply one change and send it immediately.
     *
     * Every control on the screen now applies on the spot, so there is no Update button to forget
     * and nothing half-saved to leave behind. If the server refuses, the form goes back to what
     * the server holds and the switch or row visibly returns - which is the only honest way to
     * report it.
     */
    fun updateProfile(transform: (ProfileForm) -> ProfileForm) {
        _profile.update { it.copy(form = transform(it.form)) }
        saveProfile()
    }

    /**
     * The name and username, saved together from the edit dialog.
     *
     * [onResult] hears null once the server has them, or the reason it refused. The dialog waits
     * for it: it used to close on Save, so a refused username vanished along with everything typed,
     * and the reason went to a snackbar behind the dialog's scrim (measured).
     */
    fun saveIdentity(
        username: String,
        firstName: String,
        lastName: String,
        onResult: (refusal: String?) -> Unit = {}
    ) {
        _profile.update {
            it.copy(form = it.form.copy(username = username, firstName = firstName, lastName = lastName))
        }
        saveProfile(onResult)
    }

    /** [onResult], when given, takes the outcome instead of the snackbar. */
    private fun saveProfile(onResult: ((String?) -> Unit)? = null) {
        val state = _profile.value
        if (state.isSaving) return
        if (!state.hasChanges) {
            onResult?.invoke(null)
            return
        }
        val form = state.form
        val saved = state.saved

        if (form.username.isBlank()) {
            _profile.update { it.copy(form = it.saved) }
            val message = "Username cannot be empty."
            if (onResult != null) onResult(message) else viewModelScope.launch { _messages.send(message) }
            return
        }

        _profile.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            // Only what changed: the server rejects a username it already holds, and the user's
            // own username is one it already holds.
            val result = accountRepository.updateProfile(
                username = form.username.takeIf { it != saved.username },
                firstName = form.firstName.takeIf { it != saved.firstName },
                lastName = form.lastName.takeIf { it != saved.lastName },
                publicProfile = form.publicProfile.takeIf { it != saved.publicProfile },
                measurementSystem = if (form.imperialUnits != saved.imperialUnits) {
                    if (form.imperialUnits) "imperial" else "metric"
                } else null,
                defaultCurrency = form.currency.takeIf { it != saved.currency },
                mapStyle = form.mapStyle.takeIf { it != saved.mapStyle }
            )
            _profile.update { it.copy(isSaving = false) }
            if (result is Either.Left) {
                // A refusal is the server's verdict on this value - "that username is taken" -
                // so the form goes back to what the server holds. Not having reached the server
                // is not a verdict on anything: keep what was typed, or the message telling them
                // to try again is asking them to type it a second time first.
                if (result.value.serverRefused) {
                    _profile.update { it.copy(form = it.saved) }
                }
                if (onResult != null) onResult(result.value.message) else _messages.send(result.value.message)
                return@launch
            }
            // The server has what was sent, whether or not the session it republishes has reached
            // this ViewModel yet. Waiting for that echo to clear hasChanges re-sent the same PATCH
            // until it arrived - forever, in a test where it never did (measured: a hung build).
            _profile.update { it.copy(saved = form) }
            onResult?.invoke(null)
            // Something flipped while this one was in flight - send that too rather than leaving
            // the screen showing a value the server never received.
            if (_profile.value.hasChanges) saveProfile()
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit) {
        if (_isChangingPassword.value) return
        _isChangingPassword.value = true
        viewModelScope.launch {
            val result = accountRepository.changePassword(currentPassword, newPassword)
            _isChangingPassword.value = false
            when (result) {
                is Either.Right -> {
                    _messages.send("Password changed.")
                    onSuccess()
                }
                is Either.Left -> _messages.send(result.value.message)
            }
        }
    }

    fun loadEmails() {
        viewModelScope.launch {
            _emails.update { it.copy(isLoading = true, error = null) }
            when (val result = accountRepository.getEmailAddresses()) {
                is Either.Right -> _emails.update {
                    it.copy(addresses = result.value, isLoading = false)
                }
                is Either.Left -> _emails.update {
                    it.copy(isLoading = false, error = result.value.message)
                }
            }
        }
    }

    fun addEmail(address: String) {
        val trimmed = address.trim()
        if (trimmed.isEmpty() || _emails.value.isBusy) return
        runEmailAction("Verification email sent to $trimmed.") {
            accountRepository.addEmailAddress(trimmed)
        }
    }

    fun verifyEmail(address: String) =
        runEmailAction("Verification email sent to $address.") {
            accountRepository.requestEmailVerification(address)
        }

    fun setPrimaryEmail(address: String) =
        runEmailAction("$address is now the primary address.") {
            accountRepository.setPrimaryEmailAddress(address)
        }

    fun removeEmail(address: String) =
        runEmailAction("$address removed.") {
            accountRepository.removeEmailAddress(address)
        }

    /**
     * Every address action ends with a re-read: the server decides what "verified" and "primary"
     * mean, and guessing locally is how the two drift apart.
     */
    private fun runEmailAction(success: String, action: suspend () -> Either<AccountError, Unit>) {
        if (_emails.value.isBusy) return
        _emails.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val result = action()
            _emails.update { it.copy(isBusy = false) }
            when (result) {
                is Either.Right -> {
                    _messages.send(success)
                    loadEmails()
                }
                is Either.Left -> _messages.send(result.value.message)
            }
        }
    }

    fun loadStorage() {
        viewModelScope.launch {
            _storage.update { it.copy(isLoading = true, error = null) }
            when (val result = accountRepository.getMediaUsage()) {
                is Either.Right -> _storage.update {
                    it.copy(usage = result.value, isLoading = false)
                }
                is Either.Left -> _storage.update {
                    it.copy(isLoading = false, error = result.value.message)
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setUseDynamicColors(useDynamic: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColors(useDynamic)
        }
    }

    fun getServerUrl(): String = userRepository.activeSession?.serverUrl.orEmpty()
}
