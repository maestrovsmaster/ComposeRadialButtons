package com.radialbuttons.circularbuttonlayout.utils

import androidx.compose.ui.graphics.Color

/**
 * Малює текст з обводкою по центру заданої позиції
 */
fun android.graphics.Canvas.drawCenteredText(
    text: String,
    x: Float,
    y: Float,
    size: Float,
    color: Color = Color.White
) {
    // Обводка
    val strokePaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.BLACK
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = size
        isFakeBoldText = true
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 3f
    }
    drawText(text, x, y, strokePaint)

    // Текст
    val fillPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = size
        isFakeBoldText = true
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    drawText(text, x, y, fillPaint)
}
