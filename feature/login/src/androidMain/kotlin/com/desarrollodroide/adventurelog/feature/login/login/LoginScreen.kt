package com.desarrollodroide.adventurelog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun WaveBackground() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Dibujamos el fondo blanco
            drawRect(
                color = Color.White,
                size = Size(width, height)
            )

            // Creamos el path para la onda superior
            val path = Path().apply {
                // Comenzamos desde la esquina superior izquierda
                moveTo(0f, 0f)

                // Dibujamos el rectángulo superior
                lineTo(width, 0f)
                lineTo(width, height * 0.25f)

                // Creamos la curva suave
                cubicTo(
                    x1 = width * 0.85f,
                    y1 = height * 0.25f,
                    x2 = width * 0.6f,
                    y2 = height * 0.35f,
                    x3 = width * 0.4f,
                    y3 = height * 0.3f
                )

                // Completamos la curva hasta la izquierda
                cubicTo(
                    x1 = width * 0.2f,
                    y1 = height * 0.25f,
                    x2 = width * 0.1f,
                    y2 = height * 0.2f,
                    x3 = 0f,
                    y3 = height * 0.2f
                )

                // Cerramos el path
                close()
            }

            // Dibujamos la onda con el color azul/morado
            drawPath(
                path = path,
                color = Color(0xFF4A47F5) // Color azul/morado similar al de la imagen
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun WaveBackgroundPreview() {
    WaveBackground()
}

// Ejemplo de uso en una pantalla de login
@Composable
fun LoginScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        WaveBackground()
        // Aquí irían los componentes del formulario de login
    }
}