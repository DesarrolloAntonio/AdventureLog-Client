package com.desarrollodroide.adventurelog.feature.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The shape every part of this screen is cut from: a quiet label, then one rounded card holding a
 * short run of rows.
 *
 * The screen used to be six expanded forms stacked on top of each other, which meant scrolling
 * past everything to find anything. A row that carries its own current value underneath is
 * readable at a glance and only opens when there is a reason to.
 */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    busy: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    // Pure white pops against the daylight backdrop; pure black against the dimmed one is a hole.
    // Same card, one step apart on the tonal scale depending on which way round the theme is.
    val scheme = MaterialTheme.colorScheme
    val cardColor = if (scheme.background.luminance() < 0.5f) {
        scheme.surfaceContainerLow
    } else {
        scheme.surfaceContainerLowest
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
                // Inside the card, not under it: this screen floats over a photograph, and small
                // grey text on a mountainside is not text anyone can read.
                if (caption != null) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp
                        )
                    )
                }
            }
            // A save started by flipping a switch has no button to sit inside, so the card itself
            // reports it.
            if (busy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

/**
 * One line of the list. [supporting] is where the current value goes - the whole point of the
 * layout is that you can read the state of the account without opening anything.
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    showChevron: Boolean = onClick != null,
    tint: Color? = null,
    supportingColor: Color? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val contentColor = tint ?: MaterialTheme.colorScheme.onSurface
    val alpha = if (enabled) 1f else 0.38f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick)
                else Modifier
            )
            .heightIn(min = 56.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = (tint ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = alpha),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor.copy(alpha = alpha)
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = (supportingColor ?: MaterialTheme.colorScheme.onSurfaceVariant)
                        .copy(alpha = alpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        } else if (showChevron) {
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * A switch row applies the moment it is flipped - which is what a switch promises, and what the
 * old form with its Update button quietly broke.
 */
@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    SettingsRow(
        title = title,
        modifier = modifier,
        supporting = supporting,
        icon = icon,
        onClick = { onCheckedChange(!checked) },
        enabled = enabled,
        showChevron = false,
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
    )
}

/** Divider between rows, inset so it starts where the text does. */
@Composable
fun SettingsRowDivider(inset: Boolean = true) {
    HorizontalDivider(
        modifier = Modifier.padding(start = if (inset) 60.dp else 20.dp, end = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
