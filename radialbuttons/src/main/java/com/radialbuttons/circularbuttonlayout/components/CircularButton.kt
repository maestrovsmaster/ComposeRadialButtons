package com.radialbuttons.circularbuttonlayout.components

import com.radialbuttons.circularbuttonlayout.data.CircularButtonData
import com.radialbuttons.circularbuttonlayout.data.Side
import com.radialbuttons.circularbuttonlayout.geometry.createButtonPath
import com.radialbuttons.circularbuttonlayout.geometry.visualIndex
import com.radialbuttons.circularbuttonlayout.utils.drawCenteredText
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.max

/**
 * Візуальний компонент однієї кругової кнопки
 * Складається з двох радіальних сегментів:
 * 1. Внутрішній сегмент з іконкою (від innerRadius до middleRadius)
 * 2. Зовнішній сегмент з текстом (від middleRadius до outerRadius)
 */
@Composable
fun CircularButton(
    modifier: Modifier = Modifier,
    buttonData: CircularButtonData,
    buttonIndex: Int,
    totalButtons: Int,
    centerX: Float,
    centerY: Float,
    innerRadius: Float,      // Радіус внутрішнього кола (біля центральної кнопки)
    middleRadius: Float,     // Радіус між іконкою та текстом
    outerRadius: Float,      // Зовнішній радіус
    side: Side,
    isSelected: Boolean = false,
    buttonColor: Color = Color(0xFF455A64),
    selectedButtonColor: Color = Color(0xFFFF9800),
    iconSegmentColor: Color = Color(0xFF2C3E50)
) {
    // Анімація при кліку
    var isPressed by remember { mutableStateOf(false) }

    // Ripple effect
    var rippleCenter by remember { mutableStateOf<Offset?>(null) }
    var rippleRadius by remember { mutableStateOf(0f) }

    val rippleAlpha by animateFloatAsState(
        targetValue = if (rippleCenter != null) 0f else 1f,
        animationSpec = tween(durationMillis = 600),
        finishedListener = {
            rippleCenter = null
            rippleRadius = 0f
        }, label = "ripple-alpha"
    )

    // Анімація масштабу при натисканні
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "button-scale"
    )

    Canvas(
        modifier = modifier.pointerInput(buttonData) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    rippleCenter = it
                    rippleRadius = max(outerRadius - innerRadius, 100f)
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { offset ->
                    buttonData.onClick()
                }
            )
        }
    ) {
        val scaledInnerRadius = innerRadius * scale
        val scaledMiddleRadius = middleRadius * scale
        val scaledOuterRadius = outerRadius * scale

        // Малюємо зовнішній сегмент (текст)
        drawButtonSegment(
            buttonIndex = buttonIndex,
            totalButtons = totalButtons,
            centerX = centerX,
            centerY = centerY,
            baseRadius = scaledInnerRadius,
            innerRadius = scaledMiddleRadius,
            outerRadius = scaledOuterRadius,
            side = side,
            color = if (isSelected) selectedButtonColor else buttonColor
        )

        // Малюємо внутрішній сегмент (іконка)
        drawButtonSegment(
            buttonIndex = buttonIndex,
            totalButtons = totalButtons,
            centerX = centerX,
            centerY = centerY,
            baseRadius = scaledInnerRadius,
            innerRadius = scaledInnerRadius,
            outerRadius = scaledMiddleRadius,
            side = side,
            color = iconSegmentColor
        )

        // Ripple effect
        rippleCenter?.let { center ->
            drawCircle(
                color = Color.White.copy(alpha = 0.3f * rippleAlpha),
                radius = rippleRadius * (1f - rippleAlpha),
                center = center
            )
        }

        // Текст
        val textPos = getSegmentTextPosition(
            buttonIndex, totalButtons,
            centerX, centerY,
            scaledMiddleRadius, scaledOuterRadius,
            side
        )

        drawContext.canvas.nativeCanvas.drawCenteredText(
            buttonData.text,
            textPos.x,
            textPos.y,
            32f,
            buttonData.textColor
        )

        // Іконка
        val iconPos = getSegmentTextPosition(
            buttonIndex, totalButtons,
            centerX, centerY,
            scaledInnerRadius, scaledMiddleRadius,
            side
        )

        drawContext.canvas.nativeCanvas.drawCenteredText(
            buttonData.icon,
            iconPos.x,
            iconPos.y,
            48f,
            buttonData.iconColor
        )
    }
}

/**
 * Малює один радіальний сегмент кнопки
 */
private fun DrawScope.drawButtonSegment(
    buttonIndex: Int,
    totalButtons: Int,
    centerX: Float,
    centerY: Float,
    baseRadius: Float,
    innerRadius: Float,
    outerRadius: Float,
    side: Side,
    color: Color
) {
    val path = createButtonPath(
        index = buttonIndex,
        total = totalButtons,
        centerX = centerX,
        centerY = centerY,
        baseRadius = baseRadius,
        innerRadius = innerRadius,
        outerRadius = outerRadius,
        buttonsPadding = 0f,
        side = side
    )

    drawPath(path = path, color = color, style = Fill)
    drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1f))
}

/**
 * Обчислює позицію тексту в сегменті
 */
private fun getSegmentTextPosition(
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    innerRadius: Float,
    outerRadius: Float,
    side: Side
): Offset {
    val vIndex = visualIndex(index, total, side)
    val h = (2 * innerRadius) / total
    val y = centerY - innerRadius + vIndex * h + h / 2
    val x = if (side == Side.LEFT)
        centerX - (innerRadius + outerRadius) / 2
    else
        centerX + (innerRadius + outerRadius) / 2

    return Offset(x, y + 12f)
}
