package android.test.testuiapplication.circularbuttonlayout.drawing

import android.test.testuiapplication.circularbuttonlayout.data.CircularButtonData
import android.test.testuiapplication.circularbuttonlayout.data.Side
import android.test.testuiapplication.circularbuttonlayout.geometry.createButtonPath
import android.test.testuiapplication.circularbuttonlayout.geometry.createNotchPath
import android.test.testuiapplication.circularbuttonlayout.utils.drawCenteredText
import android.test.testuiapplication.circularbuttonlayout.utils.getIconPosition
import android.test.testuiapplication.circularbuttonlayout.utils.getTextPosition
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas

/**
 * Малює кнопки з dual-segment (іконка + текст)
 */
fun DrawScope.drawDualSegmentButtons(
    buttons: List<CircularButtonData>,
    side: Side,
    selectedButton: Pair<Side, Int>?,
    selectedRadioButtons: Map<String, Pair<Side, Int>>,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    outerRadius: Float,
    buttonsPadding: Float,
    buttonColor: Color,
    selectedButtonColor: Color,
    notchColor: Color,
    activeNotchColor: Color,
    textRadialLayout: Boolean,
    padding: Float = 0f
) {
    buttons.forEachIndexed { index, button ->
        val isPressed = selectedButton == side to index

        // Визначаємо, чи кнопка активна (для notch індикатора)
        val isActive = if (button.radioGroupId != null) {
            // Якщо кнопка в радіогрупі - перевіряємо, чи вона вибрана в цій групі
            selectedRadioButtons[button.radioGroupId] == (side to index)
        } else {
            // Якщо звичайна кнопка - активна тільки під час натискання
            isPressed
        }

        // Малюємо зовнішній сегмент (текст) - від middleRadius до outerRadius
        val outerPath = createButtonPath(
            index, buttons.size,
            centerX, centerY,
            centerRadius,  // baseRadius для обчислення висоти
            middleRadius, outerRadius,
            buttonsPadding,
            side,
        )

        val notchPathCut = createNotchPath(
            index,
            buttons.size,
            centerX,
            centerY,
            centerRadius,
            middleRadius,
            outerRadius,
            buttonsPadding,
            side,
            notchHeight = 20f,
            cornerSize = 20f,
            koefWidth = 0.8f,
            cutBottomSize = 10f
        )
        val resultPath = Path()
        resultPath.op(outerPath, notchPathCut, PathOperation.Difference)
        //return resultPath

        val notchIndicator = createNotchPath(
            index,
            buttons.size,
            centerX,
            centerY,
            centerRadius,
            middleRadius,
            outerRadius,
            buttonsPadding,
            side,
            notchHeight = 16f,
            cornerSize = 80f,
            koefWidth = 0.78f,
            cutBottomSize = 0f
        )


        // Малюємо notch індикатор з відповідним кольором
        drawPath(
            path = notchIndicator,
            color = if (isActive) activeNotchColor else notchColor,
            style = Fill
        )

        drawContext.canvas.saveLayer(size.toRect(), Paint())
        drawPath(
            path = resultPath,
            color = if (isPressed) selectedButtonColor else buttonColor,
            style = Fill
        )

        // Додаємо 3D градієнт для об'ємності
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
            path = resultPath,
            brush = gradientBrush,
            style = Fill
        )

        drawPath(
            path = resultPath,
            color = Color.Black,
            style = Stroke(width = padding),
            blendMode = BlendMode.Clear
        )
        drawContext.canvas.restore()

        // Малюємо внутрішній сегмент (іконка) - від centerRadius до middleRadius
        val innerPath = createButtonPath(
            index, buttons.size,
            centerX, centerY,
            centerRadius,  // baseRadius для обчислення висоти
            centerRadius, middleRadius,
            buttonsPadding,
            side
        )

        // Колір внутрішнього сегмента - затемнена версія зовнішнього
        val innerSegmentColor = if (isPressed) {
            selectedButtonColor.copy(
                red = selectedButtonColor.red * 0.6f,
                green = selectedButtonColor.green * 0.6f,
                blue = selectedButtonColor.blue * 0.6f
            )
        } else {
            buttonColor.copy(
                red = buttonColor.red * 0.6f,
                green = buttonColor.green * 0.6f,
                blue = buttonColor.blue * 0.6f
            )
        }

        drawContext.canvas.saveLayer(size.toRect(), Paint())
        drawPath(
            path = innerPath,
            color = innerSegmentColor,
            style = Fill
        )

        // Додаємо 3D градієнт для об'ємності внутрішнього сегмента
        drawPath(
            path = innerPath,
            brush = gradientBrush,
            style = Fill
        )

        drawPath(
            path = innerPath,
            color = Color.Black,
            style = Stroke(width = padding),
            blendMode = BlendMode.Clear
        )

        drawContext.canvas.restore()

        // Малюємо текст у зовнішньому сегменті
        val textPos = if (textRadialLayout) {
            // Радіальне X, але центрований Y (по висоті кнопки)
            val radialPos = getIconPosition(
                index, buttons.size,
                centerX, centerY,
                middleRadius,      // innerRadius для тексту
                outerRadius,       // outerRadius для тексту
                buttonsPadding,
                side
            )
            val verticalPos = getTextPosition(
                index, buttons.size,
                centerX, centerY,
                centerRadius,  // baseRadius
                middleRadius, outerRadius,
                buttonsPadding,
                side
            )
            // X з радіального, Y з вертикального центрування
            Offset(radialPos.x, verticalPos.y)
        } else {
            // Вертикальне розташування
            getTextPosition(
                index, buttons.size,
                centerX, centerY,
                centerRadius,  // baseRadius
                middleRadius, outerRadius,
                buttonsPadding,
                side
            )
        }

        drawContext.canvas.nativeCanvas.drawCenteredText(
            button.text,
            textPos.x,
            textPos.y,
            32f,
            button.textColor
        )

        // Малюємо іконку у внутрішньому сегменті (радіально по колу)
        val iconPos = getIconPosition(
            index, buttons.size,
            centerX, centerY,
            centerRadius,
            middleRadius,
            buttonsPadding,
            side
        )

        drawContext.canvas.nativeCanvas.drawCenteredText(
            button.icon,
            iconPos.x,
            iconPos.y,
            48f,
            button.iconColor
        )
    }
}
