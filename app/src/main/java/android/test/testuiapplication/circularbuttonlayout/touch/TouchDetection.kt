package android.test.testuiapplication.circularbuttonlayout.touch

import android.test.testuiapplication.circularbuttonlayout.data.Side
import android.test.testuiapplication.circularbuttonlayout.data.PolarSide
import android.test.testuiapplication.circularbuttonlayout.geometry.getXOnCircle
import android.test.testuiapplication.circularbuttonlayout.geometry.visualIndex
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
 * Перевіряє, чи точка знаходиться в полярній зоні, і повертає сторону (LEFT/RIGHT) або null
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
    // Y координати для полярної зони
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

    // Перевірка чи точка в межах Y
    if (point.y !in yTop..yBottom) return null

    // Перевірка відстані від центру (має бути між centerRadius та middleRadius)
    val dx = point.x - centerX
    val dy = point.y - centerY
    val distance = sqrt(dx * dx + dy * dy)

    if (distance < centerRadius || distance > middleRadius) return null

    // Визначення сторони (ліва чи права)
    return if (point.x < centerX) PolarSide.LEFT else PolarSide.RIGHT
}
