package android.test.testuiapplication

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Дані для однієї кнопки з іконкою та текстом
 */
data class CircularButtonData(
    val icon: String,           // Іконка (або emoji)
    val text: String,           // Текст
    val onClick: () -> Unit,
    val iconColor: Color = Color(0xFFFF9800),  // Помаранчевий як на скріні
    val textColor: Color = Color.White
)

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
        }
    )

    // Анімація масштабу при натисканні
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
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
    innerRadius: Float,
    outerRadius: Float,
    side: Side,
    color: Color
) {
    val vIndex = visualIndex(buttonIndex, totalButtons, side)
    val buttonHeight = (2 * innerRadius) / totalButtons

    val yTop = centerY - innerRadius + vIndex * buttonHeight
    val yBottom = yTop + buttonHeight

    val path = createSegmentPath(
        centerX, centerY,
        innerRadius, outerRadius,
        yTop, yBottom,
        side
    )

    drawPath(path = path, color = color, style = Fill)
    drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1f))
}

/**
 * Створює Path для радіального сегмента
 */
private fun createSegmentPath(
    centerX: Float,
    centerY: Float,
    innerRadius: Float,
    outerRadius: Float,
    yTop: Float,
    yBottom: Float,
    side: Side
): Path {
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

/**
 * Малює текст з обводкою
 */
private fun android.graphics.Canvas.drawCenteredText(
    text: String,
    x: Float,
    y: Float,
    size: Float,
    color: Color = Color.White
) {
    // Обводка
    val strokePaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.BLACK
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = size
        isFakeBoldText = true
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 3f
    }
    drawText(text, x, y, strokePaint)

    // Текст
    val fillPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = size
        isFakeBoldText = true
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    drawText(text, x, y, fillPaint)
}

// Helper functions (копії з HorizontalCircularButtonLayout)
private fun visualIndex(index: Int, total: Int, side: Side): Int =
    if (side == Side.RIGHT) total - 1 - index else index

private fun getXOnCircle(
    cx: Float,
    cy: Float,
    r: Float,
    y: Float,
    side: Side
): Float {
    val dx = sqrt(r * r - (y - cy).pow(2))
    return if (side == Side.LEFT) cx - dx else cx + dx
}

private fun innerStartAngle(a: Float, side: Side) =
    if (side == Side.LEFT) 180f - Math.toDegrees(a.toDouble()).toFloat()
    else Math.toDegrees(a.toDouble()).toFloat()

private fun innerSweep(a1: Float, a2: Float, side: Side) =
    Math.toDegrees((a2 - a1).toDouble()).toFloat() * if (side == Side.LEFT) -1 else 1

private fun outerStartAngle(a: Float, side: Side) =
    if (side == Side.LEFT) 180f - Math.toDegrees(a.toDouble()).toFloat()
    else Math.toDegrees(a.toDouble()).toFloat()

private fun outerSweep(a1: Float, a2: Float, side: Side) =
    Math.toDegrees((a1 - a2).toDouble()).toFloat() * if (side == Side.LEFT) -1 else 1
