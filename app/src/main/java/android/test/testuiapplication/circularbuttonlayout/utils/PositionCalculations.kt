package android.test.testuiapplication.circularbuttonlayout.utils

import android.test.testuiapplication.circularbuttonlayout.data.Side
import android.test.testuiapplication.circularbuttonlayout.geometry.visualIndex
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos

/**
 * Обчислює позицію тексту (вертикальна лінія між middleRadius та outerRadius)
 */
fun getTextPosition(
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    baseRadius: Float,    // Базовий радіус для обчислення висоти
    innerRadius: Float,   // middleRadius
    outerRadius: Float,   // outerRadius
    buttonsPadding: Float,
    side: Side
): Offset {
    val vIndex = visualIndex(index, total, side)
    val h = (2 * baseRadius - 2 * buttonsPadding) / total
    val y = centerY - baseRadius + buttonsPadding + vIndex * h + h / 2
    val x = if (side == Side.LEFT)
        centerX - (innerRadius + outerRadius) / 2
    else
        centerX + (innerRadius + outerRadius) / 2

    return Offset(x, y + 10f)
}

/**
 * Обчислює позицію іконки (радіально по колу між centerRadius та middleRadius)
 */
fun getIconPosition(
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    innerRadius: Float,   // centerRadius
    outerRadius: Float,   // middleRadius
    buttonsPadding: Float,
    side: Side
): Offset {
    val vIndex = visualIndex(index, total, side)
    val h = (2 * innerRadius - 2 * buttonsPadding) / total

    // Вертикальна позиція центру сегмента
    val y = centerY - innerRadius + buttonsPadding + vIndex * h + h / 2

    // Радіус для іконки - середина між centerRadius та middleRadius
    val iconRadius = (innerRadius + outerRadius) / 2

    // Обчислюємо кут на основі Y позиції
    val dy = y - centerY

    // Якщо точка поза межами кола, використовуємо граничне значення
    val angle = if (abs(dy) > iconRadius) {
        if (dy > 0) Math.PI / 2 else -Math.PI / 2
    } else {
        asin((dy / iconRadius).toDouble())
    }

    // Обчислюємо X на основі кута (радіальна позиція)
    val dx = (iconRadius * cos(angle)).toFloat()
    val x = if (side == Side.LEFT) centerX - dx else centerX + dx

    return Offset(x, y)
}
