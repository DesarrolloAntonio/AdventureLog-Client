package com.desarrollodroide.adventurelog.feature.settings.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.constants.ADVENTURELOG_GITHUB_URL
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.settings.platform.PlatformActionsProvider
import com.desarrollodroide.adventurelog.feature.settings.ui.components.AboutSection
import com.desarrollodroide.adventurelog.feature.settings.ui.components.EmailsSection
import com.desarrollodroide.adventurelog.feature.settings.ui.components.ProfileSection
import com.desarrollodroide.adventurelog.feature.settings.ui.components.SecuritySection
import com.desarrollodroide.adventurelog.feature.settings.ui.components.SettingsCard
import com.desarrollodroide.adventurelog.feature.settings.ui.components.StorageSection
import com.desarrollodroide.adventurelog.feature.settings.ui.components.VisualSection
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.EmailsSectionState
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileForm
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileSectionState
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.StorageSectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateToSourceCode: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val profile by viewModel.profile.collectAsState()
    val emails by viewModel.emails.collectAsState()
    val storage by viewModel.storage.collectAsState()
    val user by viewModel.user.collectAsState()
    val isChangingPassword by viewModel.isChangingPassword.collectAsState()

    SettingsContent(
        onNavigateToSourceCode = onNavigateToSourceCode,
        onNavigateToTermsOfUse = onNavigateToTermsOfUse,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        themeMode = viewModel.themeMode,
        onThemeModeChanged = viewModel::setThemeMode,
        useDynamicColors = viewModel.useDynamicColors,
        onDynamicColorsChanged = viewModel::setUseDynamicColors,
        serverUrl = viewModel.getServerUrl(),
        user = user,
        profile = profile,
        onEditProfile = viewModel::editProfile,
        onSaveProfile = viewModel::saveProfile,
        onDiscardProfile = viewModel::discardProfileChanges,
        isChangingPassword = isChangingPassword,
        onChangePassword = viewModel::changePassword,
        emails = emails,
        onNewEmailChange = viewModel::editNewEmail,
        onAddEmail = viewModel::addEmail,
        onVerifyEmail = viewModel::verifyEmail,
        onSetPrimaryEmail = viewModel::setPrimaryEmail,
        onRemoveEmail = viewModel::removeEmail,
        onReloadEmails = viewModel::loadEmails,
        storage = storage,
        onReloadStorage = viewModel::loadStorage,
        messages = viewModel.messages
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    onNavigateToSourceCode: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    themeMode: StateFlow<ThemeMode>,
    onThemeModeChanged: (ThemeMode) -> Unit,
    useDynamicColors: StateFlow<Boolean>,
    onDynamicColorsChanged: (Boolean) -> Unit,
    serverUrl: String,
    user: UserDetails?,
    profile: ProfileSectionState,
    onEditProfile: (((ProfileForm) -> ProfileForm)) -> Unit,
    onSaveProfile: () -> Unit,
    onDiscardProfile: () -> Unit,
    isChangingPassword: Boolean,
    onChangePassword: (String, String, () -> Unit) -> Unit,
    emails: EmailsSectionState,
    onNewEmailChange: (String) -> Unit,
    onAddEmail: () -> Unit,
    onVerifyEmail: (String) -> Unit,
    onSetPrimaryEmail: (String) -> Unit,
    onRemoveEmail: (String) -> Unit,
    onReloadEmails: () -> Unit,
    storage: StorageSectionState,
    onReloadStorage: () -> Unit,
    messages: Flow<String>,
) {
    val platformActions by PlatformActionsProvider.platformActions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(messages) {
        messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(0.dp)) }
            item {
                ProfileSection(
                    state = profile,
                    onEdit = onEditProfile,
                    onSave = onSaveProfile,
                    onDiscard = onDiscardProfile
                )
            }
            item {
                SecuritySection(
                    hasPassword = user?.hasPassword ?: true,
                    isChangingPassword = isChangingPassword,
                    onChangePassword = onChangePassword
                )
            }
            item {
                EmailsSection(
                    state = emails,
                    onNewAddressChange = onNewEmailChange,
                    onAdd = onAddEmail,
                    onVerify = onVerifyEmail,
                    onSetPrimary = onSetPrimaryEmail,
                    onRemove = onRemoveEmail,
                    onRetry = onReloadEmails
                )
            }
            item {
                // Theme and dynamic colours are the only settings on this screen the server knows
                // nothing about - they belong to this device.
                SettingsCard(
                    emoji = "🎨",
                    title = "Appearance",
                    subtitle = "How this app looks on this device"
                ) {
                    VisualSection(
                        themeMode = themeMode,
                        dynamicColors = useDynamicColors,
                        onThemeModeChanged = onThemeModeChanged,
                        onDynamicColorsChanged = onDynamicColorsChanged,
                        showHeader = false
                    )
                }
            }
            item {
                StorageSection(state = storage, onRetry = onReloadStorage)
            }
            item {
                AboutSection(
                    user = user,
                    serverUrl = serverUrl,
                    appVersion = platformActions?.getAppVersion() ?: "",
                    onNavigateToServerSettings = {
                        platformActions?.openUrlInBrowser(ADVENTURELOG_GITHUB_URL)
                    },
                    onNavigateToSourceCode = onNavigateToSourceCode,
                    onSendFeedbackEmail = { platformActions?.sendFeedbackEmail() },
                    onNavigateToTermsOfUse = onNavigateToTermsOfUse,
                    onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

data class Item(
    val title: String,
    val icon: ImageVector,
    val subtitle: String = "",
    val onClick: () -> Unit = {},
    val switchState: MutableStateFlow<Boolean> = MutableStateFlow(false)
)

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
        onNavigateToSourceCode = {},
        onNavigateToTermsOfUse = {},
        onNavigateToPrivacyPolicy = {},
        themeMode = MutableStateFlow(mode),
        onThemeModeChanged = {},
        useDynamicColors = MutableStateFlow(dynamicColors),
        onDynamicColorsChanged = {},
        serverUrl = "https://example-server.com",
        user = null,
        profile = ProfileSectionState(),
        onEditProfile = {},
        onSaveProfile = {},
        onDiscardProfile = {},
        isChangingPassword = false,
        onChangePassword = { _, _, _ -> },
        emails = EmailsSectionState(isLoading = false),
        onNewEmailChange = {},
        onAddEmail = {},
        onVerifyEmail = {},
        onSetPrimaryEmail = {},
        onRemoveEmail = {},
        onReloadEmails = {},
        storage = StorageSectionState(isLoading = false),
        onReloadStorage = {},
        messages = emptyFlow()
    )
}
