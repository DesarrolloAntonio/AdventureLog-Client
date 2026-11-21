package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.feature.ui.components.LocationPlaceholder
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader

/** The top of a place's page when it has a photograph. Without one, see [CompactPlaceBar]. */
@Composable
fun CoverImageWithButtons(
    imageUrl: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    /** Null where this page has nowhere to edit the place from. */
    onEditClick: (() -> Unit)? = null,
    // False when this page is a pane beside the list it came from: there is nothing to go back
    // to, because the list never left.
    showBack: Boolean = true,
    modifier: Modifier = Modifier
) {
    val imageLoader = LocalImageLoader.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                imageLoader = imageLoader
            ),
            contentDescription = "Adventure image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Back button
        if (showBack) Box(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 24.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f))
                .align(Alignment.TopStart)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // Edit and Share. The page had no way to edit the place: the callback existed and was never
        // attached to anything.
        Row(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 24.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onEditClick != null) {
                CoverButton(icon = Icons.Default.Edit, description = "Edit place", onClick = onEditClick)
            }
            CoverButton(icon = Icons.Default.Share, description = "Share", onClick = onShareClick)
        }
    }
}

@Composable
private fun CoverButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.7f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * The top of a place's page when it has no photograph: a plain bar in place of a 300dp cover.
 *
 * A stand-in picture that tall is the first screen of the page spent on nothing. The actions the
 * cover carried stay. The name doesn't: the page's own heading is right under the bar, and the bar
 * repeating it said the same thing twice in one glance.
 */
@Composable
fun CompactPlaceBar(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    showBack: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                BarButton(icon = Icons.AutoMirrored.Filled.ArrowBack, description = "Back", onClick = onBackClick)
            }
            Spacer(Modifier.weight(1f))
            if (onEditClick != null) {
                BarButton(icon = Icons.Outlined.Edit, description = "Edit place", onClick = onEditClick)
            }
            BarButton(icon = Icons.Default.Share, description = "Share", onClick = onShareClick)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun BarButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Where a place's photograph would be, said once and with the way to add one.
 *
 * Laid across rather than as a tall block, so it doesn't take the height the cover gave back. There
 * is no upload here: photos are added in the place editor, so the button opens that.
 */
@Composable
fun NoPhotoCard(
    categoryIcon: String?,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val dash = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawRoundRect(
                    color = dash,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                    )
                )
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LocationPlaceholder(
            icon = categoryIcon,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "No photo yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Add one in the place editor and it becomes the cover everywhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = onAddPhoto,
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Add photo", style = MaterialTheme.typography.labelLarge)
        }
    }
}
