package com.desarrollodroide.adventurelog.feature.settings.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.desarrollodroide.adventurelog.core.constants.ADVENTURELOG_GITHUB_URL
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.feature.settings.platform.PlatformActionsProvider
import com.desarrollodroide.adventurelog.feature.settings.ui.components.AccountSection
import com.desarrollodroide.adventurelog.feature.settings.ui.components.VisualSection
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSourceCode: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
) {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    
    SettingsContent(
        onNavigateToSourceCode = onNavigateToSourceCode,
        onNavigateToTermsOfUse = onNavigateToTermsOfUse,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        themeMode = settingsViewModel.themeMode,
        onThemeModeChanged = settingsViewModel::setThemeMode,
        useDynamicColors = settingsViewModel.useDynamicColors,
        onDynamicColorsChanged = settingsViewModel::setUseDynamicColors,
        serverUrl = settingsViewModel.getServerUrl()
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
    ) {
    val platformActions by PlatformActionsProvider.platformActions.collectAsState()
    
//    if (settingsUiState) {
//        //InfiniteProgressDialog(onDismissRequest = {})
//    }
//    if (!settingsUiState.error.isNullOrEmpty()) {
//        ErrorDialog(
//            title = "Error",
//            content = settingsUiState.error,
//            openDialog = remember { mutableStateOf(true) },
//            onConfirm = {
//                goToLogin()
//            }
//        )
//
//    } else if (settingsUiState.data == null) {
//
//    } else {
//        LaunchedEffect(Unit) {
//            goToLogin()
//        }
//    }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            VisualSection(
                themeMode = themeMode,
                dynamicColors = useDynamicColors,
                onThemeModeChanged = onThemeModeChanged,
                onDynamicColorsChanged = onDynamicColorsChanged
            )
            CustomHorizontalDivider()
            Spacer(modifier = Modifier.height(18.dp))
            AccountSection(
                serverUrl = serverUrl,
                onNavigateToTermsOfUse = onNavigateToTermsOfUse,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                onNavigateToSeverSettings = {
                    platformActions?.openUrlInBrowser(ADVENTURELOG_GITHUB_URL)
                },
                onSendFeedbackEmail = {
                    platformActions?.sendFeedbackEmail()
                },
                onNavigateToSourceCode = onNavigateToSourceCode
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "App version",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "App v${platformActions?.getAppVersion() ?: ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun CustomHorizontalDivider(){
    HorizontalDivider(
        modifier = Modifier
            .height(1.dp)
            .padding(horizontal = 6.dp,),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    )
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
            SettingsContent(
                onNavigateToSourceCode = {},
                onNavigateToTermsOfUse = {},
                onNavigateToPrivacyPolicy = {},
                themeMode = MutableStateFlow(ThemeMode.LIGHT),
                onThemeModeChanged = {},
                useDynamicColors = MutableStateFlow(true),
                onDynamicColorsChanged = {},
                serverUrl = "https://example-server.com"
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun SettingsScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            SettingsContent(
                onNavigateToSourceCode = {},
                onNavigateToTermsOfUse = {},
                onNavigateToPrivacyPolicy = {},
                themeMode = MutableStateFlow(ThemeMode.DARK),
                onThemeModeChanged = {},
                useDynamicColors = MutableStateFlow(false),
                onDynamicColorsChanged = {},
                serverUrl = "https://example-server.com"
            )
        }
    }
}
