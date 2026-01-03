package com.radialbuttons.circularbuttonlayout.drawing

import com.radialbuttons.circularbuttonlayout.data.CircularButtonData
import com.radialbuttons.circularbuttonlayout.data.Side
import com.radialbuttons.circularbuttonlayout.geometry.createButtonPath
import com.radialbuttons.circularbuttonlayout.geometry.createNotchPath
import com.radialbuttons.circularbuttonlayout.geometry.visualIndex
import com.radialbuttons.circularbuttonlayout.touch.isPointInIconSegment
import com.radialbuttons.circularbuttonlayout.utils.drawCenteredText
import com.radialbuttons.circularbuttonlayout.utils.getIconPosition
import com.radialbuttons.circularbuttonlayout.utils.getTextPosition
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
    textColor: Color,
    activeTextColor: Color,
    iconColor: Color,
    activeIconColor: Color,
    textRadialLayout: Boolean,
    padding: Float = 0f,
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


        // Малюємо notch індикатор з градієнтом для ефекту лампочки
        if (isActive) {
            // Активна notch - теплий світловий ефект з градієнтами
            // Обчислюємо центр notch для радіального градієнта
            val vIndex = visualIndex(index, buttons.size, side)
            val h = (2 * centerRadius) / buttons.size
            val notchCenterY = centerY - centerRadius + vIndex * h + h
            val notchCenterX = if (side == Side.LEFT) {
                centerX - (centerRadius + outerRadius) / 2
            } else {
                centerX + (centerRadius + outerRadius) / 2
            }

            // Базовий колір лампочки
            drawPath(
                path = notchIndicator,
                color = activeNotchColor.copy(alpha = 0.9f),
                style = Fill
            )

            // Додаємо радіальний градієнт для ефекту світіння (яскравий центр)
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFAA66),  // Яскравий теплий центр (майже білий з жовтим відтінком)
                    activeNotchColor,    // Основний червоно-помаранчевий
                    activeNotchColor.copy(alpha = 0.6f)  // Затемнення по краях
                ),
                center = Offset(notchCenterX, notchCenterY),
                radius = 50f
            )
            drawPath(
                path = notchIndicator,
                brush = glowBrush,
                style = Fill
            )

            // Додаємо вертикальний градієнт для об'ємності (світло зверху)
            val depthBrush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.4f),  // Блик зверху
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.3f)   // Тінь знизу
                )
            )
            drawPath(
                path = notchIndicator,
                brush = depthBrush,
                style = Fill
            )
        } else {
            // Неактивна notch - простий сірий колір без градієнтів
            drawPath(
                path = notchIndicator,
                color = notchColor,
                style = Fill
            )
        }

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
            if (isActive) activeTextColor else textColor
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
            if (isActive) activeIconColor else iconColor
        )
    }
}
