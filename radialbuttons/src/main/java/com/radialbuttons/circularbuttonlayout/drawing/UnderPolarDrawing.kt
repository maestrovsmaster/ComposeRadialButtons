package com.radialbuttons.circularbuttonlayout.drawing

import com.radialbuttons.circularbuttonlayout.data.CircularButtonData
import com.radialbuttons.circularbuttonlayout.utils.drawCenteredText
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.*

/**
 * Draws under-polar zone (top or bottom) - horizontal segment between middleRadius and outerRadius
 */
fun DrawScope.drawUnderPolarZone(
    button: CircularButtonData,
    isTop: Boolean,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    outerRadius: Float,
    buttonsPadding: Float,
    underPolarColor: Color,
    padding: Float = 0f
) {
    // Y coordinates for under-polar zone
    val yTop = if (isTop) {
        centerY - outerRadius
    } else {
        centerY + centerRadius - buttonsPadding
    }
    val yBottom = if (isTop) {
        centerY - centerRadius + buttonsPadding
    } else {
        centerY + outerRadius
    }

    // Calculate angles
    val angleTopInner = asin(max(-1f, min(1f, (yTop - centerY) / middleRadius)))
    val angleBottomInner = asin(max(-1f, min(1f, (yBottom - centerY) / middleRadius)))
    val angleTopOuter = asin(max(-1f, min(1f, (yTop - centerY) / outerRadius)))
    val angleBottomOuter = asin(max(-1f, min(1f, (yBottom - centerY) / outerRadius)))

    // Calculate X coordinates
    val xLeftOuterTop = centerX - sqrt(max(0f, outerRadius * outerRadius - (yTop - centerY) * (yTop - centerY)))
    val xRightOuterTop = centerX + sqrt(max(0f, outerRadius * outerRadius - (yTop - centerY) * (yTop - centerY)))
    val xLeftOuterBottom = centerX - sqrt(max(0f, outerRadius * outerRadius - (yBottom - centerY) * (yBottom - centerY)))
    val xRightOuterBottom = centerX + sqrt(max(0f, outerRadius * outerRadius - (yBottom - centerY) * (yBottom - centerY)))

    val xLeftInnerTop = centerX - sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
    val xRightInnerTop = centerX + sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
    val xLeftInnerBottom = centerX - sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))
    val xRightInnerBottom = centerX + sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))

    // Create path
    val path = Path().apply {
        moveTo(xLeftOuterTop, yTop)
        lineTo(xRightOuterTop, yTop)

        arcTo(
            Rect(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius),
            Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )

        lineTo(xLeftOuterBottom, yBottom)

        arcTo(
            Rect(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius),
            180f - Math.toDegrees(angleBottomOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )

        close()

        // Cut out inner part
        moveTo(xLeftInnerTop, yTop)
        lineTo(xRightInnerTop, yTop)

        arcTo(
            Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
            Math.toDegrees(angleTopInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )

        lineTo(xLeftInnerBottom, yBottom)

        arcTo(
            Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
            180f - Math.toDegrees(angleBottomInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )

        close()
        fillType = PathFillType.EvenOdd
    }

    drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())

    drawPath(
        path = path,
        color = underPolarColor,
        style = Fill
    )

    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = padding),
        blendMode = BlendMode.Clear
    )

    drawContext.canvas.restore()

    // Draw icon
    val iconY = (yTop + yBottom) / 2 + 18f
    drawContext.canvas.nativeCanvas.drawCenteredText(
        button.icon,
        centerX,
        iconY,
        48f,
        button.iconColor
    )
}
