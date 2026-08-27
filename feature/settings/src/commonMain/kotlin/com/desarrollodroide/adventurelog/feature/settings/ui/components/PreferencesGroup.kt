package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.core.model.MapStyles
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileForm
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileSectionState

private const val METRIC = "metric"
private const val IMPERIAL = "imperial"

/**
 * The preferences the server keeps, so they follow the account onto the web client too.
 *
 * Each row wears its current value, and each change is saved the moment it is made - a settings
 * screen with a Save button makes you remember to press it, and punishes you when you don't.
 */
@Composable
fun PreferencesGroup(
    state: ProfileSectionState,
    onChange: ((ProfileForm) -> ProfileForm) -> Unit,
    modifier: Modifier = Modifier
) {
    var openSheet by remember { mutableStateOf<PreferenceSheet?>(null) }
    val form = state.form

    SettingsGroup(
        title = "Preferences",
        modifier = modifier,
        busy = state.isSaving,
        caption = "Stored on your account, so the web client reads the same values."
    ) {
        SettingsSwitchRow(
            title = "Public profile",
            icon = if (form.publicProfile) Icons.Outlined.Public else Icons.Outlined.Lock,
            supporting = if (form.publicProfile) {
                "Other users can find you and see your public places"
            } else {
                "Only you can see your profile"
            },
            checked = form.publicProfile,
            onCheckedChange = { value -> onChange { it.copy(publicProfile = value) } }
        )
        SettingsRowDivider()
        SettingsRow(
            title = "Units",
            icon = Icons.Outlined.Straighten,
            supporting = if (form.imperialUnits) "Imperial - miles and pounds"
            else "Metric - kilometres and kilograms",
            onClick = { openSheet = PreferenceSheet.UNITS }
        )
        SettingsRowDivider()
        SettingsRow(
            title = "Currency",
            icon = Icons.Outlined.Payments,
            supporting = "${form.currency} - ${Currencies.labelFor(form.currency)}",
            onClick = { openSheet = PreferenceSheet.CURRENCY }
        )
        SettingsRowDivider()
        SettingsRow(
            title = "Map style",
            icon = Icons.Outlined.Layers,
            supporting = MapStyles.labelFor(form.mapStyle),
            onClick = { openSheet = PreferenceSheet.MAP_STYLE }
        )
    }

    when (openSheet) {
        PreferenceSheet.UNITS -> SettingsChoiceSheet(
            title = "Units",
            selected = if (form.imperialUnits) IMPERIAL else METRIC,
            entries = listOf(
                ChoiceEntry(
                    value = METRIC,
                    label = "Metric",
                    supporting = "Kilometres, metres and kilograms"
                ),
                ChoiceEntry(
                    value = IMPERIAL,
                    label = "Imperial",
                    supporting = "Miles, feet and pounds"
                )
            ),
            onSelect = { value ->
                openSheet = null
                onChange { it.copy(imperialUnits = value == IMPERIAL) }
            },
            onDismiss = { openSheet = null }
        )

        PreferenceSheet.CURRENCY -> SettingsChoiceSheet(
            title = "Currency",
            selected = form.currency,
            entries = Currencies.options.map { (code, name) ->
                ChoiceEntry(value = code, label = code, supporting = name)
            },
            onSelect = { value ->
                openSheet = null
                onChange { it.copy(currency = value) }
            },
            onDismiss = { openSheet = null }
        )

        PreferenceSheet.MAP_STYLE -> SettingsChoiceSheet(
            title = "Map style",
            note = "Used as the base map everywhere. The phone draws the closest Google Maps " +
                "equivalent of the style you pick.",
            selected = form.mapStyle,
            entries = MapStyles.options.map { option ->
                ChoiceEntry(value = option.code, label = option.label, group = option.group)
            },
            onSelect = { value ->
                openSheet = null
                onChange { it.copy(mapStyle = value) }
            },
            onDismiss = { openSheet = null }
        )

        null -> Unit
    }
}

private enum class PreferenceSheet { UNITS, CURRENCY, MAP_STYLE }
