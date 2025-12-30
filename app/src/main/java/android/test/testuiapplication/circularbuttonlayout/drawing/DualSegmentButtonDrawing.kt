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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
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
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    outerRadius: Float,
    buttonsPadding: Float,
    buttonColor: Color,
    selectedButtonColor: Color,
    textRadialLayout: Boolean,
    padding: Float = 0f
) {
    buttons.forEachIndexed { index, button ->
        val isSelected = selectedButton == side to index

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


        drawPath(
            path = notchIndicator,
            color = selectedButtonColor,
            style = Fill
        )

        drawContext.canvas.saveLayer(size.toRect(), Paint())
        drawPath(
            path = resultPath,
            color = if (isSelected) selectedButtonColor else buttonColor,
            style = Fill
        )

        drawPath(
            path = resultPath,
            color = Color.Black,//White.copy(alpha = 0.3f),
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
        val innerSegmentColor = if (isSelected) {
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

        drawPath(
            path = innerPath,
            color = Color.Black,//White.copy(alpha = 0.8f),
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
