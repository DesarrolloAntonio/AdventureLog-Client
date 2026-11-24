package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable

/**
 * The same directive with the scaffold's auto-focus turned off.
 *
 * By default the list-detail scaffold moves focus into the pane it considers current whenever that
 * changes, and focus enters the pane at its first focusable node - on Places and Collections, the
 * search field. On a tablet every visit to either tab raised the keyboard with nothing typed.
 * `copy()` does not carry the flag, so the directive is rebuilt field by field.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun PaneScaffoldDirective.withoutAutoFocus(): PaneScaffoldDirective = PaneScaffoldDirective(
    maxHorizontalPartitions = maxHorizontalPartitions,
    horizontalPartitionSpacerSize = horizontalPartitionSpacerSize,
    maxVerticalPartitions = maxVerticalPartitions,
    verticalPartitionSpacerSize = verticalPartitionSpacerSize,
    defaultPanePreferredWidth = defaultPanePreferredWidth,
    defaultPanePreferredHeight = defaultPanePreferredHeight,
    excludedBounds = excludedBounds,
    shouldAutoFocusCurrentDestination = false,
)

/** The list-detail strategy for screens whose list opens with a text field: no focus moved on its own. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun <T : Any> rememberQuietListDetailSceneStrategy(): ListDetailSceneStrategy<T> =
    rememberListDetailSceneStrategy(
        directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()).withoutAutoFocus()
    )

/**
 * Whether the list-detail strategy is showing both panes right now - read from the same directive
 * the strategy lays out with. "Wider than compact" is not the same thing: a tablet held upright
 * (about 700dp) is medium width yet gets one pane, and screens that thought otherwise opened the
 * first item over their list with no Back arrow to leave it.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun rememberShowsTwoPanes(): Boolean =
    calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()).maxHorizontalPartitions > 1
