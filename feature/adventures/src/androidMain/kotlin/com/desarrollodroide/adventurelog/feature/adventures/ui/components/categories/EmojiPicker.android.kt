package com.desarrollodroide.adventurelog.feature.adventures.ui.components.categories

import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import com.desarrollodroide.adventurelog.feature.adventures.R

@Composable
actual fun EmojiPicker(
    modifier: Modifier,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                // Use the style from this module's resources
                val themedContext = ContextThemeWrapper(ctx, R.style.EmojiPickerStyle)
                
                object : FrameLayout(themedContext) {
                    val emojiPicker = EmojiPickerView(themedContext).apply {
                        emojiGridColumns = 9
                        setOnEmojiPickedListener { item ->
                            onEmojiSelected(item.emoji)
                            onDismiss()
                        }
                    }

                    init {
                        addView(
                            emojiPicker,
                            LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }

                    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
                        parent?.requestDisallowInterceptTouchEvent(true)
                        return false
                    }

                    override fun onTouchEvent(event: MotionEvent): Boolean {
                        parent?.requestDisallowInterceptTouchEvent(true)
                        return super.onTouchEvent(event)
                    }
                }
            }
        )
    }
}
