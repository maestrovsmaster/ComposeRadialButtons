package android.test.testuiapplication.circularbuttonlayout.geometry

import android.test.testuiapplication.circularbuttonlayout.data.Side
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import kotlin.math.asin

/**
 * Створює Path для радіального сегмента кнопки
 */
fun createButtonPath(
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    baseRadius: Float,    // Базовий радіус для обчислення висоти кнопки
    innerRadius: Float,   // Внутрішній радіус сегмента
    outerRadius: Float,   // Зовнішній радіус сегмента
    buttonsPadding: Float, // Padding зверху/знизу
    side: Side
): Path {
    val vIndex = visualIndex(index, total, side)
    val buttonHeight = (2 * baseRadius - 2 * buttonsPadding) / total

    val yTop = centerY - baseRadius + buttonsPadding + vIndex * buttonHeight
    val yBottom = yTop + buttonHeight

    val angleTopInner = asin((yTop - centerY) / innerRadius)
    val angleBottomInner = asin((yBottom - centerY) / innerRadius)
    val angleTopOuter = asin((yTop - centerY) / outerRadius)
    val angleBottomOuter = asin((yBottom - centerY) / outerRadius)

    return Path().apply {
        val startX = getXOnCircle(centerX, centerY, innerRadius, yTop, side)
        moveTo(startX, yTop)

        arcTo(
            Rect(
                centerX - innerRadius,
                centerY - innerRadius,
                centerX + innerRadius,
                centerY + innerRadius
            ),
            innerStartAngle(angleTopInner, side),
            innerSweep(angleTopInner, angleBottomInner, side),
            false
        )

        lineTo(getXOnCircle(centerX, centerY, outerRadius, yBottom, side), yBottom)

        arcTo(
            Rect(
                centerX - outerRadius,
                centerY - outerRadius,
                centerX + outerRadius,
                centerY + outerRadius
            ),
            outerStartAngle(angleBottomOuter, side),
            outerSweep(angleTopOuter, angleBottomOuter, side),
            false
        )

        close()
    }
}
