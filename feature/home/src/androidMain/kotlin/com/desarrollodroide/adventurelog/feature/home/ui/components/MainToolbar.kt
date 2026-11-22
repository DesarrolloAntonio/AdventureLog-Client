package com.desarrollodroide.adventurelog.feature.home.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen
import com.desarrollodroide.adventurelog.feature.ui.components.ShellAction
import kotlin.math.roundToInt

object MainToolbarDefaults {
    /** The canvas's pill: 64dp tall, an item 48dp. */
    val Height: Dp = 64.dp
    val ItemSize: Dp = 48.dp

    /** The screen's main action beside the toolbar. 56dp, not the canvas's 64, so the toolbar
     *  with its longest label ("Collections") and the button still fit a 412dp phone. */
    val ActionSize: Dp = 56.dp

    /** Gap between the toolbar and the gesture bar. */
    val BottomGap: Dp = 24.dp

    const val TestTag = "main_toolbar"

    /** Clear space kept either side of the floating row. */
    val SideMargin: Dp = 12.dp

    internal val ContentPadding: Dp = 6.dp
    internal val PillStart: Dp = 12.dp
    internal val PillEnd: Dp = 16.dp
    internal val PillIcon: Dp = 22.dp
    internal val PillGap: Dp = 8.dp
    internal val MaxPillWidth: Dp = 168.dp

    /** The toolbar's width with the current destination's name [labelWidth] wide, or with no
     *  current destination among the five when it is null. */
    internal fun toolbarWidth(labelWidth: Dp?): Dp {
        if (labelWidth == null) return ContentPadding * 2 + ItemSize * 5
        val pill = (PillStart + PillIcon + PillGap + labelWidth + PillEnd).coerceAtMost(MaxPillWidth)
        return ContentPadding * 2 + ItemSize * 4 + pill
    }
}

/**
 * How far the floating row has slid off the bottom edge. Scrolling content down pushes it out,
 * scrolling up brings it back, a fling settles it fully in or out - Material's "exit always"
 * behaviour. Hidden at the end of a list, so nothing stays under it for good.
 */
@Stable
class ToolbarScrollState {
    /** 0 shown, [limitPx] fully hidden. */
    var hiddenPx by mutableFloatStateOf(0f)
        private set
    internal var limitPx by mutableFloatStateOf(0f)

    fun show() {
        hiddenPx = 0f
    }

    val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            // consumed.y < 0 when content moves up, i.e. the user scrolls down the list.
            hiddenPx = (hiddenPx - consumed.y).coerceIn(0f, limitPx)
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val target = if (hiddenPx > limitPx / 2) limitPx else 0f
            if (hiddenPx != target) animate(hiddenPx, target) { value, _ -> hiddenPx = value }
            return Velocity.Zero
        }
    }
}

@Composable
fun rememberToolbarScrollState(): ToolbarScrollState = remember { ToolbarScrollState() }

/**
 * The phone's floating row: the five destinations and, when the screen has one, its main action
 * beside them - as the canvas draws Places ("New place") and Collections. It floats: content runs
 * behind it, and it slides away while the content scrolls down.
 */
@Composable
fun MainToolbarRow(
    current: CurrentScreen,
    onSelect: (CurrentScreen) -> Unit,
    action: ShellAction?,
    scroll: ToolbarScrollState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val navBar = WindowInsets.navigationBars.getBottom(density)
    val gap = with(density) { MainToolbarDefaults.BottomGap.roundToPx() }
    val labelStyle = MaterialTheme.typography.labelLarge
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier) {
    // The current destination keeps its name only when the whole row still fits with a margin
    // either side. "Places" and the "+" fit a phone; "Collections" and the "+" do not, and a
    // name cut to "Collect..." says less than the icon alone.
    // Settings, Calendar and People are in the shell but none of the five: then no pill at all.
    val label = Destination.entries.firstOrNull { it.screen == current }?.label
    val labelWidth = label?.let { with(density) { measurer.measure(it, labelStyle).size.width.toDp() } }
    val needed = MainToolbarDefaults.toolbarWidth(labelWidth) +
        if (action != null) 8.dp + MainToolbarDefaults.ActionSize else 0.dp
    val showLabel = needed <= maxWidth - MainToolbarDefaults.SideMargin * 2
    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = MainToolbarDefaults.BottomGap)
            .onSizeChanged { scroll.limitPx = (it.height + gap + navBar).toFloat() }
            .offset { IntOffset(0, scroll.hiddenPx.roundToInt()) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MainToolbar(
            current = current,
            onSelect = onSelect,
            showLabel = showLabel,
            modifier = Modifier.testTag(MainToolbarDefaults.TestTag),
        )
        if (action != null) {
            FloatingActionButton(
                onClick = action.onClick,
                modifier = Modifier.size(MainToolbarDefaults.ActionSize),
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(action.icon, contentDescription = action.description)
            }
        }
    }
    }
}

/**
 * The phone's five destinations, as the Expressive canvas draws them: a dark pill floating over
 * the content, the current destination a light pill with its name, the others icons alone.
 *
 * It takes the bottom bar's place on a compact window only. On a wider one the rail stays - a
 * toolbar centred under a tablet's content is a long way from either hand.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainToolbar(
    current: CurrentScreen,
    onSelect: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier.height(MainToolbarDefaults.Height),
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = colors.inverseSurface,
            toolbarContentColor = colors.inverseOnSurface,
        ),
        contentPadding = PaddingValues(horizontal = MainToolbarDefaults.ContentPadding),
    ) {
        Row(
            modifier = Modifier.selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Destination.entries.forEach { destination ->
                if (destination.screen == current) {
                    SelectedDestination(destination, showLabel)
                } else {
                    // An icon button is interactive, which the toolbar's first item must be.
                    IconButton(
                        onClick = { onSelect(destination.screen) },
                        modifier = Modifier.size(MainToolbarDefaults.ItemSize),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = colors.inverseOnSurface),
                    ) {
                        Icon(destination.icon, contentDescription = destination.label)
                    }
                }
            }
        }
    }
}

/** The current destination: icon and name on a light pill. Not a button - it is where you are. */
@Composable
private fun SelectedDestination(destination: Destination, showLabel: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .height(MainToolbarDefaults.ItemSize)
            .widthIn(max = MainToolbarDefaults.MaxPillWidth)
            .clip(CircleShape)
            .background(colors.primaryContainer)
            .selectable(selected = true, role = Role.Tab, onClick = {})
            .then(if (showLabel) Modifier else Modifier.semantics { contentDescription = destination.label })
            .padding(start = MainToolbarDefaults.PillStart, end = if (showLabel) MainToolbarDefaults.PillEnd else MainToolbarDefaults.PillStart)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            destination.selectedIcon,
            contentDescription = null,
            tint = colors.onPrimaryContainer,
            modifier = Modifier.size(MainToolbarDefaults.PillIcon),
        )
        if (showLabel) {
            Spacer(Modifier.width(MainToolbarDefaults.PillGap))
            Text(
                text = destination.label,
                color = colors.onPrimaryContainer,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
