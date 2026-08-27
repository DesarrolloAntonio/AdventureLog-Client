package com.desarrollodroide.adventurelog.feature.settings.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.desarrollodroide.adventurelog.core.constants.ADVENTURELOG_CLIENT_GITHUB_URL
import com.desarrollodroide.adventurelog.core.constants.ADVENTURELOG_GITHUB_URL
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.settings.platform.PlatformActionsProvider
import com.desarrollodroide.adventurelog.feature.settings.ui.components.AboutSection
import com.desarrollodroide.adventurelog.feature.settings.ui.components.AppearanceGroup
import com.desarrollodroide.adventurelog.feature.settings.ui.components.EditProfileDialog
import com.desarrollodroide.adventurelog.feature.settings.ui.components.PreferencesGroup
import com.desarrollodroide.adventurelog.feature.settings.ui.components.PrivacyPolicyScreen
import com.desarrollodroide.adventurelog.feature.settings.ui.components.SignInGroup
import com.desarrollodroide.adventurelog.feature.settings.ui.components.StorageSection
import com.desarrollodroide.adventurelog.feature.settings.ui.components.TermsOfUseScreen
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.EmailsSectionState
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileForm
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileSectionState
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.StorageSectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import com.desarrollodroide.adventurelog.feature.ui.components.settings.AccountHeader

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val profile by viewModel.profile.collectAsState()
    val emails by viewModel.emails.collectAsState()
    val storage by viewModel.storage.collectAsState()
    val user by viewModel.user.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColors by viewModel.useDynamicColors.collectAsState()
    val isChangingPassword by viewModel.isChangingPassword.collectAsState()

    SettingsContent(
        themeMode = themeMode,
        onThemeModeChanged = viewModel::setThemeMode,
        dynamicColors = dynamicColors,
        onDynamicColorsChanged = viewModel::setUseDynamicColors,
        serverUrl = viewModel.getServerUrl(),
        user = user,
        profile = profile,
        onChangeProfile = viewModel::updateProfile,
        onSaveIdentity = viewModel::saveIdentity,
        isChangingPassword = isChangingPassword,
        onChangePassword = viewModel::changePassword,
        emails = emails,
        onAddEmail = viewModel::addEmail,
        onVerifyEmail = viewModel::verifyEmail,
        onSetPrimaryEmail = viewModel::setPrimaryEmail,
        onRemoveEmail = viewModel::removeEmail,
        onReloadEmails = viewModel::loadEmails,
        storage = storage,
        onReloadStorage = viewModel::loadStorage,
        onLogout = onLogout,
        messages = viewModel.messages
    )
}

@Composable
fun SettingsContent(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    dynamicColors: Boolean,
    onDynamicColorsChanged: (Boolean) -> Unit,
    serverUrl: String,
    user: UserDetails?,
    profile: ProfileSectionState,
    onChangeProfile: ((ProfileForm) -> ProfileForm) -> Unit,
    onSaveIdentity: (username: String, firstName: String, lastName: String) -> Unit,
    isChangingPassword: Boolean,
    onChangePassword: (String, String, () -> Unit) -> Unit,
    emails: EmailsSectionState,
    onAddEmail: (String) -> Unit,
    onVerifyEmail: (String) -> Unit,
    onSetPrimaryEmail: (String) -> Unit,
    onRemoveEmail: (String) -> Unit,
    onReloadEmails: () -> Unit,
    storage: StorageSectionState,
    onReloadStorage: () -> Unit,
    onLogout: () -> Unit,
    messages: Flow<String>,
) {
    val platformActions by PlatformActionsProvider.platformActions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var editProfileOpen by remember { mutableStateOf(false) }
    var legalPage by remember { mutableStateOf<LegalPage?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(messages) {
        messages.collect { snackbarHostState.showSnackbar(it) }
    }

    val primaryEmail = emails.addresses.firstOrNull { it.primary }?.email
        ?: emails.addresses.firstOrNull()?.email
        ?: user?.email

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AccountHeader(
                    user = user,
                    primaryEmail = primaryEmail,
                    serverUrl = serverUrl,
                    onClick = { editProfileOpen = true }
                )
            }
            item {
                PreferencesGroup(state = profile, onChange = onChangeProfile)
            }
            item {
                AppearanceGroup(
                    themeMode = themeMode,
                    onThemeModeChanged = onThemeModeChanged,
                    dynamicColors = dynamicColors,
                    onDynamicColorsChanged = onDynamicColorsChanged
                )
            }
            item {
                SignInGroup(
                    hasPassword = user?.hasPassword ?: true,
                    isChangingPassword = isChangingPassword,
                    onChangePassword = onChangePassword,
                    emails = emails,
                    onAddEmail = onAddEmail,
                    onVerifyEmail = onVerifyEmail,
                    onSetPrimaryEmail = onSetPrimaryEmail,
                    onRemoveEmail = onRemoveEmail,
                    onRetryEmails = onReloadEmails
                )
            }
            item {
                StorageSection(state = storage, onRetry = onReloadStorage)
            }
            item {
                AboutSection(
                    user = user,
                    serverUrl = serverUrl,
                    appVersion = platformActions?.getAppVersion() ?: "",
                    onNavigateToServerGuide = {
                        platformActions?.openUrlInBrowser(ADVENTURELOG_GITHUB_URL)
                    },
                    onNavigateToSourceCode = {
                        platformActions?.openUrlInBrowser(ADVENTURELOG_CLIENT_GITHUB_URL)
                    },
                    onSendFeedbackEmail = { platformActions?.sendFeedbackEmail() },
                    onNavigateToTermsOfUse = { legalPage = LegalPage.TERMS },
                    onNavigateToPrivacyPolicy = { legalPage = LegalPage.PRIVACY }
                )
            }
            item {
                SignOutCard(onClick = { confirmLogout = true })
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    if (editProfileOpen) {
        EditProfileDialog(
            initial = profile.form,
            isSaving = profile.isSaving,
            onDismiss = { editProfileOpen = false },
            onConfirm = { username, firstName, lastName ->
                editProfileOpen = false
                onSaveIdentity(username, firstName, lastName)
            }
        )
    }

    // Both pages already exist as full screens; showing them here keeps them one back-press away
    // instead of routing through the whole navigation graph for two static documents.
    legalPage?.let { page ->
        Dialog(
            onDismissRequest = { legalPage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            when (page) {
                LegalPage.TERMS -> TermsOfUseScreen(onBack = { legalPage = null })
                LegalPage.PRIVACY -> PrivacyPolicyScreen(onBack = { legalPage = null })
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Sign out?") },
            text = { Text("You will need your password to sign back in.") },
            confirmButton = {
                Button(
                    onClick = {
                        confirmLogout = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Sign out")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Signing out lived only behind the avatar menu. It belongs at the bottom of settings too - that
 * is the first place anyone looks for it - and on its own card, away from anything routine.
 */
@Composable
private fun SignOutCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        SettingsRow(
            title = "Sign out",
            icon = Icons.AutoMirrored.Outlined.Logout,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            showChevron = false,
            onClick = onClick
        )
    }
}

private enum class LegalPage { TERMS, PRIVACY }

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun SettingsScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            SettingsPreviewContent(ThemeMode.LIGHT, dynamicColors = true)
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun SettingsScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            SettingsPreviewContent(ThemeMode.DARK, dynamicColors = false)
        }
    }
}

@Composable
private fun SettingsPreviewContent(mode: ThemeMode, dynamicColors: Boolean) {
    SettingsContent(
        themeMode = mode,
        onThemeModeChanged = {},
        dynamicColors = dynamicColors,
        onDynamicColorsChanged = {},
        serverUrl = "https://example-server.com",
        user = null,
        profile = ProfileSectionState(),
        onChangeProfile = {},
        onSaveIdentity = { _, _, _ -> },
        isChangingPassword = false,
        onChangePassword = { _, _, _ -> },
        emails = EmailsSectionState(isLoading = false),
        onAddEmail = {},
        onVerifyEmail = {},
        onSetPrimaryEmail = {},
        onRemoveEmail = {},
        onReloadEmails = {},
        storage = StorageSectionState(isLoading = false),
        onReloadStorage = {},
        onLogout = {},
        messages = emptyFlow()
    )
}
