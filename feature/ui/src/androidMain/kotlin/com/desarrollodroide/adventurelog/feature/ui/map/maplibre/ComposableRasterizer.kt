package com.desarrollodroide.adventurelog.feature.ui.map.maplibre

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView

/**
 * Draws a composable into a bitmap, the way maps-compose's `MarkerComposable` does for Google:
 * MapLibre draws icons, not views. The composable is laid out at its own size inside [host] for an
 * instant, under [composition] so it sees the app's theme, then removed. Main thread only.
 */
internal class ComposableRasterizer(
    private val host: ViewGroup,
    private val composition: CompositionContext,
) {
    fun render(content: @Composable () -> Unit): Bitmap? {
        val view = ComposeView(host.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setParentCompositionContext(composition)
            setContent(content)
        }
        host.addView(view)
        try {
            // Composition happens on the first draw; only then does the view know its size.
            view.draw(Canvas())
            val unspecified = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(unspecified, unspecified)
            if (view.measuredWidth == 0 || view.measuredHeight == 0) return null
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            return bitmap
        } finally {
            host.removeView(view)
        }
    }
}
