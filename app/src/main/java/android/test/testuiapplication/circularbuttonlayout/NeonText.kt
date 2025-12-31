package android.test.testuiapplication.circularbuttonlayout

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

/**
 * Неоновий текст всередині сфери з glow ефектом
 */
@Composable
fun NeonText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 1.0f  // Прозорість для fade ефекту
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Конфігурація для неонового ефекту
            val baseTextSize = 60f
            val glowColor = color.toArgb()

            drawContext.canvas.nativeCanvas.apply {
                // Шар 1: Найбільший glow (найслабший)
                val outerGlowPaint = Paint().apply {
                    this.color = glowColor
                    this.textAlign = Paint.Align.CENTER
                    this.textSize = baseTextSize
                    this.isFakeBoldText = true
                    this.isAntiAlias = true
                    this.alpha = (40 * alpha).toInt()
                    this.setShadowLayer(30f, 0f, 0f, glowColor)
                }
                drawText(text, centerX, centerY + 20f, outerGlowPaint)

                // Шар 2: Середній glow
                val middleGlowPaint = Paint().apply {
                    this.color = glowColor
                    this.textAlign = Paint.Align.CENTER
                    this.textSize = baseTextSize
                    this.isFakeBoldText = true
                    this.isAntiAlias = true
                    this.alpha = (80 * alpha).toInt()
                    this.setShadowLayer(15f, 0f, 0f, glowColor)
                }
                drawText(text, centerX, centerY + 20f, middleGlowPaint)

                // Шар 3: Внутрішній glow (яскравіший)
                val innerGlowPaint = Paint().apply {
                    this.color = glowColor
                    this.textAlign = Paint.Align.CENTER
                    this.textSize = baseTextSize
                    this.isFakeBoldText = true
                    this.isAntiAlias = true
                    this.alpha = (150 * alpha).toInt()
                    this.setShadowLayer(8f, 0f, 0f, glowColor)
                }
                drawText(text, centerX, centerY + 20f, innerGlowPaint)

                // Шар 4: Основний текст (найяскравіший, майже білий)
                val mainTextPaint = Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    this.textAlign = Paint.Align.CENTER
                    this.textSize = baseTextSize
                    this.isFakeBoldText = true
                    this.isAntiAlias = true
                    this.alpha = (255 * alpha).toInt()
                    this.setShadowLayer(3f, 0f, 0f, glowColor)
                }
                drawText(text, centerX, centerY + 20f, mainTextPaint)
            }
        }
    }
}
