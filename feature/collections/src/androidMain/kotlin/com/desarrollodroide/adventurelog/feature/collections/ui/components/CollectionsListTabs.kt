package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTab

/**
 * Mine / Shared / Archived / Invites as one connected group, the chosen one inked dark as the
 * canvas draws it, Invites carrying how many are waiting. They replace the filter sheet's "Show"
 * and the invitations banner: what you are looking at is always on screen.
 *
 * Connected toggle buttons in a Row rather than the Expressive ButtonGroup, whose measure policy
 * crashed Shiori's list; this sits right above one.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionsListTabs(
    selected: CollectionsTab,
    pendingInvites: Int,
    onSelect: (CollectionsTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = CollectionsTab.entries
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        tabs.forEachIndexed { index, tab ->
            val count = if (tab == CollectionsTab.INVITES) pendingInvites else 0
            ToggleButton(
                checked = selected == tab,
                onCheckedChange = { if (it) onSelect(tab) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(
                    containerColor = colors.surfaceContainerHigh,
                    contentColor = colors.onSurfaceVariant,
                    checkedContainerColor = colors.inverseSurface,
                    checkedContentColor = colors.inverseOnSurface,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .semantics {
                        role = Role.Tab
                        this.selected = selected == tab
                        contentDescription = if (count > 0) "${tab.label}, $count waiting" else tab.label
                    },
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (count > 0) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = count.toString(),
                        color = colors.onError,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.error)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
        }
    }
}
