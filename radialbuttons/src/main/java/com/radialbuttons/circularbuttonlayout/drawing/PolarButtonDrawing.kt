package com.radialbuttons.circularbuttonlayout.drawing

import com.radialbuttons.circularbuttonlayout.data.PolarButtonGroup
import com.radialbuttons.circularbuttonlayout.data.PolarSide
import com.radialbuttons.circularbuttonlayout.data.PolarZone
import com.radialbuttons.circularbuttonlayout.utils.drawCenteredText
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*

/**
 * Малює полярну групу кнопок (верхню або нижню) як один об'єднаний Path
 */
fun DrawScope.drawPolarButtonGroup(
    buttonGroup: PolarButtonGroup,
    isTop: Boolean,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    componentWidth: Float,
    heightPx: Float,
    buttonsPadding: Float,
    buttonColor: Color,
    selectedButtonColor: Color,
    selectedPolarButton: Triple<PolarZone, PolarSide, Boolean>?,
    padding: Float = 0f
) {
    // 1. Y координати
    val yTop = if (isTop) centerY - middleRadius else centerY + centerRadius - buttonsPadding
    val yBottom = if (isTop) centerY - centerRadius + buttonsPadding else centerY + middleRadius

    // 2. Кольори
    val polarZoneColor = buttonColor.copy(
        red = buttonColor.red * 0.6f,
        green = buttonColor.green * 0.6f,
        blue = buttonColor.blue * 0.6f
    )
    val polarZoneSelectedColor = selectedButtonColor.copy(
        red = selectedButtonColor.red * 0.6f,
        green = selectedButtonColor.green * 0.6f,
        blue = selectedButtonColor.blue * 0.6f
    )


    val outerPath = Path().apply {
        val outerRect = Rect(
            centerX - middleRadius,
            centerY - middleRadius,
            centerX + middleRadius,
            centerY + middleRadius
        )
        val angleTopOuter = asin(max(-1f, min(1f, (yTop - centerY) / middleRadius)))
        val angleBottomOuter = asin(max(-1f, min(1f, (yBottom - centerY) / middleRadius)))
        val xLeftOuterTop = centerX - sqrt(max(0f, middleRadius.pow(2) - (yTop - centerY).pow(2)))
        val xRightOuterTop = centerX + sqrt(max(0f, middleRadius.pow(2) - (yTop - centerY).pow(2)))
        val xLeftOuterBottom =
            centerX - sqrt(max(0f, middleRadius.pow(2) - (yBottom - centerY).pow(2)))

        moveTo(xLeftOuterTop, yTop)
        lineTo(xRightOuterTop, yTop)
        arcTo(
            outerRect,
            Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )
        lineTo(xLeftOuterBottom, yBottom)
        arcTo(
            outerRect,
            180f - Math.toDegrees(angleBottomOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )
        close()
    }

    val innerPath = Path().apply {
        val innerRect = Rect(
            centerX - centerRadius,
            centerY - centerRadius,
            centerX + centerRadius,
            centerY + centerRadius
        )
        val angleTopInner = asin(max(-1f, min(1f, (yTop - centerY) / centerRadius)))
        val angleBottomInner = asin(max(-1f, min(1f, (yBottom - centerY) / centerRadius)))
        val xLeftInnerTop = centerX - sqrt(max(0f, centerRadius.pow(2) - (yTop - centerY).pow(2)))
        val xRightInnerTop = centerX + sqrt(max(0f, centerRadius.pow(2) - (yTop - centerY).pow(2)))
        val xLeftInnerBottom =
            centerX - sqrt(max(0f, centerRadius.pow(2) - (yBottom - centerY).pow(2)))

        moveTo(xLeftInnerTop, yTop)
        lineTo(xRightInnerTop, yTop)
        arcTo(
            innerRect,
            Math.toDegrees(angleTopInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )
        lineTo(xLeftInnerBottom, yBottom)
        arcTo(
            innerRect,
            180f - Math.toDegrees(angleBottomInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )
        close()
    }

    // Справжнє кільце без внутрішніх ліній
    val fullPath = Path().apply {
        op(outerPath, innerPath, PathOperation.Difference)
    }

    // 4. Малюємо основну фігуру
    drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())

    drawPath(path = fullPath, color = polarZoneColor, style = Fill)

    // 5. Малюємо виділення (якщо вибрано сторону)
    selectedPolarButton?.let { (zone, side, top) ->
        if (zone == PolarZone.POLAR && top == isTop) {
            val selectionRect =
                if (side == PolarSide.LEFT) Rect(0f, 0f, centerX, size.height) else Rect(
                    centerX,
                    0f,
                    size.width,
                    size.height
                )
            val sidePath = Path().apply { addRect(selectionRect) }
            val highlightPath = Path().apply { op(fullPath, sidePath, PathOperation.Intersect) }
            drawPath(path = highlightPath, color = polarZoneSelectedColor, style = Fill)
        }
    }

    // Додаємо 3D градієнт для об'ємності (як на основних кнопках)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.3f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.2f)
        ),
        startY = centerY - centerRadius,
        endY = centerY + centerRadius
    )
    drawPath(
        path = fullPath,
        brush = gradientBrush,
        style = Fill
    )

    // 6. Малюємо загальний контур (без лінії посередині)
    drawPath(
        path = fullPath,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = padding),
        blendMode = BlendMode.Clear
    )
    drawContext.canvas.restore()
    // 7. Відмальовка тексту та іконок (ваша оригінальна логіка)
    val zoneCenterY = if (isTop) (yBottom / 2) else yTop + abs(heightPx - yTop) / 2
    val iconRadius = (centerRadius + middleRadius) / 2
    val dy = zoneCenterY - centerY
    val angle =
        if (abs(dy) > iconRadius) (if (dy > 0) PI / 2 else -PI / 2) else asin((dy / iconRadius).toDouble())
    val baseIconDx = (iconRadius * cos(angle)).toFloat()
    val iconDistanceMultiplier = 1.0f + buttonGroup.iconOffsetFromEdge
    val iconY = zoneCenterY + 18f

    drawContext.canvas.nativeCanvas.apply {
        drawCenteredText(
            buttonGroup.leftButton.icon,
            centerX - baseIconDx * iconDistanceMultiplier,
            iconY,
            64f,
            buttonGroup.leftButton.iconColor
        )
        drawCenteredText(
            buttonGroup.rightButton.icon,
            centerX + baseIconDx * iconDistanceMultiplier,
            iconY,
            64f,
            buttonGroup.rightButton.iconColor
        )

        buttonGroup.title?.let { title ->
            val titleY = if (buttonGroup.subtitle != null) zoneCenterY - 5f else zoneCenterY + 12f
            drawCenteredText(title, centerX, titleY, buttonGroup.titleSize, buttonGroup.titleColor)
        }
        buttonGroup.subtitle?.let { subtitle ->
            drawCenteredText(
                subtitle,
                centerX,
                zoneCenterY + 22f,
                buttonGroup.subtitleSize,
                buttonGroup.subtitleColor
            )
        }
    }
}