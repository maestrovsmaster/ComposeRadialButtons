package android.test.testuiapplication.circularbuttonlayout.geometry

import android.test.testuiapplication.circularbuttonlayout.data.Side
import android.util.Log
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import kotlin.math.abs

/**
 * Created by maestromaster$ on 29/12/2025$.
 */

fun createNotchPath(
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    baseRadius: Float,
    innerRadius: Float,
    outerRadius: Float,
    buttonsPadding: Float,
    side: Side,
    notchHeight: Float = 20f,// Глибина вирізу вгору
    cornerSize: Float = 20f,// Скруглення кутів вирізу
    koefWidth: Float = 0.8f,
    cutBottomSize: Float = 0f
): Path {
    val notchPath = Path().apply {
        val vIndex = visualIndex(index, total, side)
        val buttonHeight = (2 * baseRadius - 2 * buttonsPadding) / total

        val yTop = centerY - baseRadius + buttonsPadding + vIndex * buttonHeight
        val yBottom = yTop + buttonHeight

        val www = abs(outerRadius - innerRadius) /2

        val widht = koefWidth * www * 2f

        val koef = if(side == Side.LEFT) 1 else -1


        val nR =  outerRadius - abs(outerRadius - innerRadius) /2 + koef * widht/2

        val centerNotchX = getXOnCircle(centerX, centerY, nR, yBottom, side)



        // Центруємо прямокутник відносно краю кнопки
        val left = centerNotchX
        val top = yBottom - notchHeight
        val right = left + widht
        val bottom = yBottom + cutBottomSize // виводимо за межі кнопки вниз

        addRoundRect(
            RoundRect(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                topLeftCornerRadius = CornerRadius(cornerSize),
                topRightCornerRadius = CornerRadius(cornerSize),
                bottomLeftCornerRadius = CornerRadius(0f),
                bottomRightCornerRadius = CornerRadius(0f)
            )
        )
    }

    return notchPath
}