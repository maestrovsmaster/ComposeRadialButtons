package com.radialbuttons.circularbuttonlayout.touch

import com.radialbuttons.circularbuttonlayout.data.Side
import com.radialbuttons.circularbuttonlayout.data.PolarSide
import com.radialbuttons.circularbuttonlayout.geometry.getXOnCircle
import com.radialbuttons.circularbuttonlayout.geometry.visualIndex
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Перевіряє, чи точка знаходиться всередині кнопки
 */
fun isPointInButton(
    point: Offset,
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    outerRadius: Float,
    side: Side
): Boolean {
    val vIndex = visualIndex(index, total, side)
    val buttonHeight = (2 * centerRadius) / total

    val yTop = centerY - centerRadius + vIndex * buttonHeight
    val yBottom = yTop + buttonHeight
    if (point.y !in yTop..yBottom) return false

    val dy = point.y - centerY
    if (abs(dy) > outerRadius) return false

    val xInner = getXOnCircle(centerX, centerY, centerRadius, point.y, side)
    val xOuter = getXOnCircle(centerX, centerY, outerRadius, point.y, side)

    return when (side) {
        Side.LEFT -> point.x in xOuter..xInner
        Side.RIGHT -> point.x in xInner..xOuter
    }
}

/**
 * Check if point is in polar zone and return side (LEFT/RIGHT) or null
 */
fun isPointInPolarZone(
    point: Offset,
    isTop: Boolean,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    buttonsPadding: Float
): PolarSide? {
    // Y coordinates for polar zone
    val yTop = if (isTop) {
        centerY - middleRadius
    } else {
        centerY + centerRadius - buttonsPadding
    }
    val yBottom = if (isTop) {
        centerY - centerRadius + buttonsPadding
    } else {
        centerY + middleRadius
    }

    // Check if point is within Y bounds
    if (point.y !in yTop..yBottom) return null

    // Check distance from center (must be between centerRadius and middleRadius)
    val dx = point.x - centerX
    val dy = point.y - centerY
    val distance = sqrt(dx * dx + dy * dy)

    if (distance < centerRadius || distance > middleRadius) return null

    // Determine side (left or right)
    return if (point.x < centerX) PolarSide.LEFT else PolarSide.RIGHT
}

/**
 * Check if point is in icon segment (between innerRadius and middleRadius)
 * Returns true if point is in icon segment, false if in text segment
 */
fun isPointInIconSegment(
    point: Offset,
    centerX: Float,
    centerY: Float,
    innerRadius: Float,
    middleRadius: Float
): Boolean {
    val dx = point.x - centerX
    val dy = point.y - centerY
    val distance = sqrt(dx * dx + dy * dy)

    return distance in innerRadius..middleRadius
}
