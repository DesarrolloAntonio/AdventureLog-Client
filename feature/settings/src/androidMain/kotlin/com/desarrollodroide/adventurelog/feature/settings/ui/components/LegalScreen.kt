package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import com.desarrollodroide.adventurelog.feature.ui.platform.PlatformBackHandler

enum class LegalDoc { TERMS, PRIVACY }

/**
 * Terms and Privacy as one page with a tab for each, as the canvas draws it: Back, the two tabs,
 * then the document's title large and its text. Reached as either, from Settings' About, in a
 * full-screen dialog - so it paints its own background.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LegalScreen(initial: LegalDoc, onBack: () -> Unit) {
    PlatformBackHandler { onBack() }
    var doc by rememberSaveable { mutableStateOf(initial) }
    val colors = MaterialTheme.colorScheme

    Surface(Modifier.fillMaxSize(), color = colors.background) {
        ContentColumn(Modifier.statusBarsPadding().navigationBarsPadding()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colors.surfaceContainerLowest,
                            contentColor = colors.onSurface,
                        ),
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                        listOf(LegalDoc.TERMS to "Terms", LegalDoc.PRIVACY to "Privacy").forEachIndexed { index, (value, label) ->
                            ToggleButton(
                                checked = doc == value,
                                onCheckedChange = { if (it) doc = value },
                                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes()
                                else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                colors = ToggleButtonDefaults.colors(
                                    containerColor = colors.surfaceContainerHigh,
                                    contentColor = colors.onSurfaceVariant,
                                    checkedContainerColor = colors.inverseSurface,
                                    checkedContentColor = colors.inverseOnSurface,
                                ),
                                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                            ) { Text(label, style = MaterialTheme.typography.labelLarge) }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (doc == LegalDoc.TERMS) "Terms of Use" else "Privacy policy",
                    style = AdventureType.display(44.sp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (doc == LegalDoc.TERMS) TermsOfUse.annotated() else PrivacyPolicy.annotated(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
