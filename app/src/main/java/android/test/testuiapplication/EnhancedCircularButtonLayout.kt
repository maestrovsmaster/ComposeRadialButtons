package android.test.testuiapplication

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.*

/**
 * Enhanced Horizontal Circular Button Layout з іконками
 * Використовує один Canvas для всіх кнопок
 */
@Composable
fun EnhancedCircularButtonLayout(
    modifier: Modifier = Modifier,
    leftButtons: List<CircularButtonData>,
    rightButtons: List<CircularButtonData>,
    topWideButton: CircularButtonData? = null,      // Верхня широка кнопка
    bottomWideButton: CircularButtonData? = null,   // Нижня широка кнопка
    centerLabel: String = "Menu",
    onCenterClick: () -> Unit = {},
    centerColor: Color = Color(0xFF4CAF50),
    buttonColor: Color = Color(0xFF455A64),
    selectedButtonColor: Color = Color(0xFFFF9800),
    centerRadiusRatio: Float = 0.5f,
    iconSegmentRadiusRatio: Float = 1.2f, // Радіус секції іконок у % від радіуса центрального кола (1.0 = 100%, 1.2 = 120%)
    outerRadiusRatio: Float = 1.0f,       // Зовнішній радіус (ширина кнопок) відносно baseDimension
    buttonsPaddingRatio: Float = 0.0f,    // Padding зверху/знизу для бічних кнопок (0.0 - 0.5)
    textRadialLayout: Boolean = false,    // true = радіальне розташування тексту, false = вертикальне
    circlePaddingRatio: Float = 0.0f      // Padding для центрального кола (0.0 - 0.5)
) {
    var selectedButton by remember { mutableStateOf<Pair<Side, Int>?>(null) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // Calculate base dimension with circle padding
        // Діаметр кола = Найменша_сторона_екрану - 2 * паддінг_для_кола
        val circlePadding = min(widthPx, heightPx) * circlePaddingRatio
        val baseDimension = min(widthPx, heightPx) - 2 * circlePadding

        val centerRadius = baseDimension * centerRadiusRatio
        val middleRadius = centerRadius * iconSegmentRadiusRatio  // Відносно радіуса центрального кола
        val outerRadius = baseDimension * outerRadiusRatio

        // Padding для бічних кнопок
        val buttonsPadding = baseDimension * buttonsPaddingRatio

        // Висота області бічних кнопок = діаметр - 2*padding
        val buttonsAreaHeight = 2 * centerRadius - 2 * buttonsPadding

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(leftButtons, rightButtons) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)

                        if (distance <= centerRadius) {
                            selectedButton = null
                            onCenterClick()
                            return@detectTapGestures
                        }

                        if (offset.x < centerX) {
                            leftButtons.forEachIndexed { index, button ->
                                if (isPointInButton(
                                        offset, index, leftButtons.size,
                                        centerX, centerY,
                                        centerRadius, outerRadius,
                                        Side.LEFT
                                    )
                                ) {
                                    selectedButton = Side.LEFT to index
                                    button.onClick()
                                    return@detectTapGestures
                                }
                            }
                        } else {
                            rightButtons.forEachIndexed { index, button ->
                                if (isPointInButton(
                                        offset, index, rightButtons.size,
                                        centerX, centerY,
                                        centerRadius, outerRadius,
                                        Side.RIGHT
                                    )
                                ) {
                                    selectedButton = Side.RIGHT to index
                                    button.onClick()
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Малюємо ліві кнопки
            drawDualSegmentButtons(
                buttons = leftButtons,
                side = Side.LEFT,
                selectedButton = selectedButton,
                centerX = centerX,
                centerY = centerY,
                centerRadius = centerRadius,
                middleRadius = middleRadius,
                outerRadius = outerRadius,
                buttonsPadding = buttonsPadding,
                buttonColor = buttonColor,
                selectedButtonColor = selectedButtonColor,
                textRadialLayout = textRadialLayout
            )

            // Малюємо праві кнопки
            drawDualSegmentButtons(
                buttons = rightButtons,
                side = Side.RIGHT,
                selectedButton = selectedButton,
                centerX = centerX,
                centerY = centerY,
                centerRadius = centerRadius,
                middleRadius = middleRadius,
                outerRadius = outerRadius,
                buttonsPadding = buttonsPadding,
                buttonColor = buttonColor,
                selectedButtonColor = selectedButtonColor,
                textRadialLayout = textRadialLayout
            )

            // Малюємо верхню полярну зону (якщо є padding)
            if (buttonsPadding > 0) {
                topWideButton?.let { button ->
                    drawPolarZone(
                        button = button,
                        isTop = true,
                        centerX = centerX,
                        centerY = centerY,
                        centerRadius = centerRadius,
                        middleRadius = middleRadius,
                        buttonsPadding = buttonsPadding,
                        buttonColor = buttonColor
                    )
                }
            }

            // Малюємо нижню полярну зону (якщо є padding)
            if (buttonsPadding > 0) {
                bottomWideButton?.let { button ->
                    drawPolarZone(
                        button = button,
                        isTop = false,
                        centerX = centerX,
                        centerY = centerY,
                        centerRadius = centerRadius,
                        middleRadius = middleRadius,
                        buttonsPadding = buttonsPadding,
                        buttonColor = buttonColor
                    )
                }
            }

            // Малюємо центральну кнопку
            drawCircle(
                color = centerColor,
                radius = centerRadius,
                center = Offset(centerX, centerY),
                style = Fill
            )

            drawCircle(
                color = Color.White,
                radius = centerRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f)
            )

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.textSize = 40f
                    this.isFakeBoldText = true
                    this.isAntiAlias = true
                }
                this.drawText(centerLabel, centerX, centerY + 15f, paint)
            }
        }
    }
}

/**
 * Малює кнопки з dual-segment (іконка + текст)
 */
private fun DrawScope.drawDualSegmentButtons(
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
    textRadialLayout: Boolean
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
            side
        )

        drawPath(
            path = outerPath,
            color = if (isSelected) selectedButtonColor else buttonColor,
            style = Fill
        )

        drawPath(
            path = outerPath,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = 1f)
        )

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

        drawPath(
            path = innerPath,
            color = innerSegmentColor,
            style = Fill
        )

        drawPath(
            path = innerPath,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = 1f)
        )

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

/**
 * Малює полярну зону (верхню або нижню) - горизонтальний сегмент між centerRadius та middleRadius
 */
private fun DrawScope.drawPolarZone(
    button: CircularButtonData,
    isTop: Boolean,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    buttonsPadding: Float,
    buttonColor: Color
) {
    // Y координати для полярної зони
    val yTop = if (isTop) {
        centerY - centerRadius
    } else {
        centerY + centerRadius - buttonsPadding
    }
    val yBottom = if (isTop) {
        centerY - centerRadius + buttonsPadding
    } else {
        centerY + centerRadius
    }

    // Обчислюємо кути
    val angleTopInner = asin((yTop - centerY) / centerRadius)
    val angleBottomInner = asin((yBottom - centerY) / centerRadius)
    val angleTopOuter = asin((yTop - centerY) / middleRadius)
    val angleBottomOuter = asin((yBottom - centerY) / middleRadius)

    // Обчислюємо X координати на кожній висоті
    val xLeftOuterTop = centerX - sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
    val xRightOuterTop = centerX + sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
    val xLeftOuterBottom = centerX - sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))
    val xRightOuterBottom = centerX + sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))

    val xLeftInnerTop = centerX - sqrt(max(0f, centerRadius * centerRadius - (yTop - centerY) * (yTop - centerY)))
    val xRightInnerTop = centerX + sqrt(max(0f, centerRadius * centerRadius - (yTop - centerY) * (yTop - centerY)))
    val xLeftInnerBottom = centerX - sqrt(max(0f, centerRadius * centerRadius - (yBottom - centerY) * (yBottom - centerY)))
    val xRightInnerBottom = centerX + sqrt(max(0f, centerRadius * centerRadius - (yBottom - centerY) * (yBottom - centerY)))

    // Створюємо path - сегмент кільця
    val path = Path().apply {
        // Зовнішній контур
        moveTo(xLeftOuterTop, yTop)
        lineTo(xRightOuterTop, yTop)

        // Дуга зовнішнього кола від yTop до yBottom (права сторона)
        arcTo(
            Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
            Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )

        lineTo(xLeftOuterBottom, yBottom)

        // Дуга зовнішнього кола від yBottom до yTop (ліва сторона)
        arcTo(
            Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
            180f - Math.toDegrees(angleBottomOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )

        close()

        // Вирізаємо внутрішню частину
        moveTo(xLeftInnerTop, yTop)
        lineTo(xRightInnerTop, yTop)

        // Дуга внутрішнього кола від yTop до yBottom (права сторона)
        arcTo(
            Rect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius, centerY + centerRadius),
            Math.toDegrees(angleTopInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )

        lineTo(xLeftInnerBottom, yBottom)

        // Дуга внутрішнього кола від yBottom до yTop (ліва сторона)
        arcTo(
            Rect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius, centerY + centerRadius),
            180f - Math.toDegrees(angleBottomInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )

        close()

        // Встановлюємо fill type для вирізання внутрішньої частини
        fillType = PathFillType.EvenOdd
    }

    // Колір - як внутрішня секція іконок (затемнений)
    val polarZoneColor = buttonColor.copy(
        red = buttonColor.red * 0.6f,
        green = buttonColor.green * 0.6f,
        blue = buttonColor.blue * 0.6f
    )

    drawPath(
        path = path,
        color = polarZoneColor,
        style = Fill
    )

    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = 1f)
    )

    // Малюємо іконку в центрі полярної зони
    val iconY = (yTop + yBottom) / 2 + 18f
    drawContext.canvas.nativeCanvas.drawCenteredText(
        button.icon,
        centerX,
        iconY,
        48f,
        button.iconColor
    )
}

/**
 * Створює Path для радіального сегмента кнопки
 */
private fun createButtonPath(
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

/**
 * Перевіряє, чи точка знаходиться всередині кнопки
 */
private fun isPointInButton(
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
 * Обчислює позицію тексту (вертикальна лінія між middleRadius та outerRadius)
 */
private fun getTextPosition(
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
private fun getIconPosition(
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

// Helper functions
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
