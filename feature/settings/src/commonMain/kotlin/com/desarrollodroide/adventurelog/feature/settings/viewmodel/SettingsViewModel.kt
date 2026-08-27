package com.desarrollodroide.adventurelog.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
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

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    val themeMode = settingsRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.AUTO)

    val useDynamicColors = settingsRepository.getUseDynamicColors()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val compactView = settingsRepository.getCompactView()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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

    /** The name and username, saved together from the edit dialog. */
    fun saveIdentity(username: String, firstName: String, lastName: String) {
        updateProfile {
            it.copy(username = username, firstName = firstName, lastName = lastName)
        }
    }

    private fun saveProfile() {
        val state = _profile.value
        if (state.isSaving || !state.hasChanges) return
        val form = state.form
        val saved = state.saved

        if (form.username.isBlank()) {
            _profile.update { it.copy(form = it.saved) }
            viewModelScope.launch { _messages.send("Username cannot be empty.") }
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
                _profile.update { it.copy(form = it.saved) }
                _messages.send(result.value)
                return@launch
            }
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
                is Either.Left -> _messages.send(result.value)
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
                    it.copy(isLoading = false, error = result.value)
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
    private fun runEmailAction(success: String, action: suspend () -> Either<String, Unit>) {
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
                is Either.Left -> _messages.send(result.value)
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
                    it.copy(isLoading = false, error = result.value)
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

    fun setCompactView(isCompact: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCompactView(isCompact)
        }
    }

    fun getServerUrl(): String = userRepository.activeSession?.serverUrl.orEmpty()
}
