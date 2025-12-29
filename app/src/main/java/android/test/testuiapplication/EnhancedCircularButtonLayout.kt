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
 * Група полярних кнопок (верхня або нижня зона)
 * Візуально виглядає як одна область, але містить дві окремі кнопки
 */
data class PolarButtonGroup(
    val leftButton: CircularButtonData,   // Ліва половина
    val rightButton: CircularButtonData,  // Права половина
    val title: String? = null,            // Заголовок по центру
    val subtitle: String? = null,         // Підзаголовок по центру
    val titleSize: Float = 32f,           // Розмір тексту заголовка
    val titleColor: Color = Color.White,  // Колір тексту заголовка
    val subtitleSize: Float = 24f,        // Розмір тексту підзаголовка
    val subtitleColor: Color = Color.White.copy(alpha = 0.7f), // Колір тексту підзаголовка
    val iconOffsetFromEdge: Float = 0.12f // Відступ іконок від краю компонента (0.0 - 0.5)
)

/**
 * Enhanced Horizontal Circular Button Layout з іконками
 * Використовує один Canvas для всіх кнопок
 */
@Composable
fun EnhancedCircularButtonLayout(
    modifier: Modifier = Modifier,
    leftButtons: List<CircularButtonData>,
    rightButtons: List<CircularButtonData>,
    topPolarButtonGroup: PolarButtonGroup? = null,      // Верхня полярна група кнопок
    bottomPolarButtonGroup: PolarButtonGroup? = null,   // Нижня полярна група кнопок
    topUnderPolarButton: CircularButtonData? = null,    // Верхня under-polar кнопка
    bottomUnderPolarButton: CircularButtonData? = null, // Нижня under-polar кнопка
    centerLabel: String = "Menu",
    onCenterClick: () -> Unit = {},
    centerColor: Color = Color(0xFF4CAF50),
    buttonColor: Color = Color(0xFF455A64),
    selectedButtonColor: Color = Color(0xFFFF9800),
    polarButtonColor: Color = Color(0xFF455A64),    // Колір полярних зон
    underPolarColor: Color = Color(0xFF4CAF50),     // Колір under-polar зон
    centerRadiusRatio: Float = 0.5f,
    iconSegmentRadiusRatio: Float = 1.2f, // Радіус секції іконок у % від радіуса центрального кола (1.0 = 100%, 1.2 = 120%)
    outerRadiusRatio: Float = 1.0f,       // Зовнішній радіус (ширина кнопок) відносно baseDimension
    buttonsPaddingRatio: Float = 0.0f,    // Padding зверху/знизу для бічних кнопок (0.0 - 0.5)
    textRadialLayout: Boolean = false,    // true = радіальне розташування тексту, false = вертикальне
    circlePaddingRatio: Float = 0.0f      // Padding для центрального кола (0.0 - 0.5)
) {
    // State для вибраної кнопки: бічна кнопка або полярна кнопка
    var selectedSideButton by remember { mutableStateOf<Pair<Side, Int>?>(null) }
    var selectedPolarButton by remember { mutableStateOf<Triple<PolarZone, PolarSide, Boolean>?>(null) } // (зона, сторона, isTop)

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
        val outerRadius = min(widthPx, heightPx) * outerRadiusRatio

        // Padding для бічних кнопок
        val buttonsPadding = baseDimension * buttonsPaddingRatio

        // Висота області бічних кнопок = діаметр - 2*padding
        val buttonsAreaHeight = 2 * centerRadius - 2 * buttonsPadding

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(leftButtons, rightButtons, topPolarButtonGroup, bottomPolarButtonGroup) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)

                        // Перевірка центральної кнопки
                        if (distance <= centerRadius) {
                            selectedSideButton = null
                            selectedPolarButton = null
                            onCenterClick()
                            return@detectTapGestures
                        }

                        // Перевірка полярних зон (якщо є padding)
                        if (buttonsPadding > 0) {
                            // Верхня полярна зона
                            topPolarButtonGroup?.let { group ->
                                val polarSide = isPointInPolarZone(
                                    offset, true, centerX, centerY,
                                    centerRadius, middleRadius, buttonsPadding
                                )
                                if (polarSide != null) {
                                    selectedSideButton = null
                                    selectedPolarButton = Triple(PolarZone.POLAR, polarSide, true)
                                    if (polarSide == PolarSide.LEFT) {
                                        group.leftButton.onClick()
                                    } else {
                                        group.rightButton.onClick()
                                    }
                                    return@detectTapGestures
                                }
                            }

                            // Нижня полярна зона
                            bottomPolarButtonGroup?.let { group ->
                                val polarSide = isPointInPolarZone(
                                    offset, false, centerX, centerY,
                                    centerRadius, middleRadius, buttonsPadding
                                )
                                if (polarSide != null) {
                                    selectedSideButton = null
                                    selectedPolarButton = Triple(PolarZone.POLAR, polarSide, false)
                                    if (polarSide == PolarSide.LEFT) {
                                        group.leftButton.onClick()
                                    } else {
                                        group.rightButton.onClick()
                                    }
                                    return@detectTapGestures
                                }
                            }
                        }

                        // Перевірка бічних кнопок
                        if (offset.x < centerX) {
                            leftButtons.forEachIndexed { index, button ->
                                if (isPointInButton(
                                        offset, index, leftButtons.size,
                                        centerX, centerY,
                                        centerRadius, outerRadius,
                                        Side.LEFT
                                    )
                                ) {
                                    selectedSideButton = Side.LEFT to index
                                    selectedPolarButton = null
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
                                    selectedSideButton = Side.RIGHT to index
                                    selectedPolarButton = null
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
                selectedButton = selectedSideButton,
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
                selectedButton = selectedSideButton,
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
                topPolarButtonGroup?.let { group ->
                    drawPolarButtonGroup(
                        buttonGroup = group,
                        isTop = true,
                        centerX = centerX,
                        centerY = centerY,
                        centerRadius = centerRadius,
                        middleRadius = middleRadius,
                        componentWidth = size.width,
                        buttonsPadding = buttonsPadding,
                        buttonColor = polarButtonColor,
                        selectedButtonColor = selectedButtonColor,
                        selectedPolarButton = selectedPolarButton
                    )
                }
            }

            // Малюємо нижню полярну зону (якщо є padding)
            if (buttonsPadding > 0) {
                bottomPolarButtonGroup?.let { group ->
                    drawPolarButtonGroup(
                        buttonGroup = group,
                        isTop = false,
                        centerX = centerX,
                        centerY = centerY,
                        centerRadius = centerRadius,
                        middleRadius = middleRadius,
                        componentWidth = size.width,
                        buttonsPadding = buttonsPadding,
                        buttonColor = polarButtonColor,
                        selectedButtonColor = selectedButtonColor,
                        selectedPolarButton = selectedPolarButton
                    )
                }
            }

            // Малюємо верхню under-polar зону (якщо є padding)
            if (buttonsPadding > 0) {
                topUnderPolarButton?.let { button ->
                    drawUnderPolarZone(
                        button = button,
                        isTop = true,
                        centerX = centerX,
                        centerY = centerY,
                        centerRadius = centerRadius,
                        middleRadius = middleRadius,
                        outerRadius = outerRadius,
                        buttonsPadding = buttonsPadding,
                        underPolarColor = underPolarColor
                    )
                }
            }

            // Малюємо нижню under-polar зону (якщо є padding)
            if (buttonsPadding > 0) {
                bottomUnderPolarButton?.let { button ->
                    drawUnderPolarZone(
                        button = button,
                        isTop = false,
                        centerX = centerX,
                        centerY = centerY,
                        centerRadius = centerRadius,
                        middleRadius = middleRadius,
                        outerRadius = outerRadius,
                        buttonsPadding = buttonsPadding,
                        underPolarColor = underPolarColor
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
 * Малює полярну групу кнопок (верхню або нижню) - дві половини з іконками по боках і текстом по центру
 */
private fun DrawScope.drawPolarButtonGroup(
    buttonGroup: PolarButtonGroup,
    isTop: Boolean,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    componentWidth: Float,
    buttonsPadding: Float,
    buttonColor: Color,
    selectedButtonColor: Color,
    selectedPolarButton: Triple<PolarZone, PolarSide, Boolean>?
) {
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

    // Обчислюємо кути
    val angleTopInner = asin(max(-1f, min(1f, (yTop - centerY) / centerRadius)))
    val angleBottomInner = asin(max(-1f, min(1f, (yBottom - centerY) / centerRadius)))
    val angleTopOuter = asin(max(-1f, min(1f, (yTop - centerY) / middleRadius)))
    val angleBottomOuter = asin(max(-1f, min(1f, (yBottom - centerY) / middleRadius)))

    // Колір - як внутрішня секція іконок (затемнений)
    val polarZoneColor = buttonColor.copy(
        red = buttonColor.red * 0.6f,
        green = buttonColor.green * 0.6f,
        blue = buttonColor.blue * 0.6f
    )

    val polarZoneSelectedColor = selectedButtonColor.copy(
        red = selectedButtonColor.red * 0.6f,
        green = selectedButtonColor.green * 0.6f,
        blue = selectedButtonColor.blue * 0.6f
    )

    // Перевірка чи вибрана ліва половина
    val isLeftSelected = selectedPolarButton?.let { (zone, side, top) ->
        zone == PolarZone.POLAR && side == PolarSide.LEFT && top == isTop
    } ?: false

    // Перевірка чи вибрана права половина
    val isRightSelected = selectedPolarButton?.let { (zone, side, top) ->
        zone == PolarZone.POLAR && side == PolarSide.RIGHT && top == isTop
    } ?: false

    // Малюємо ліву половину
    drawPolarHalf(
        isLeft = true,
        isTop = isTop,
        centerX = centerX,
        centerY = centerY,
        centerRadius = centerRadius,
        middleRadius = middleRadius,
        yTop = yTop,
        yBottom = yBottom,
        angleTopInner = angleTopInner,
        angleBottomInner = angleBottomInner,
        angleTopOuter = angleTopOuter,
        angleBottomOuter = angleBottomOuter,
        color = if (isLeftSelected) polarZoneSelectedColor else polarZoneColor
    )

    // Малюємо праву половину
    drawPolarHalf(
        isLeft = false,
        isTop = isTop,
        centerX = centerX,
        centerY = centerY,
        centerRadius = centerRadius,
        middleRadius = middleRadius,
        yTop = yTop,
        yBottom = yBottom,
        angleTopInner = angleTopInner,
        angleBottomInner = angleBottomInner,
        angleTopOuter = angleTopOuter,
        angleBottomOuter = angleBottomOuter,
        color = if (isRightSelected) polarZoneSelectedColor else polarZoneColor
    )

    // Центр зони для тексту і іконок
    val zoneCenterY = (yTop + yBottom) / 2

    // Позиції іконок - фіксовані від країв компонента (відносно ширини екрану)
    val iconOffsetX = (componentWidth / 2) * buttonGroup.iconOffsetFromEdge
    val leftIconX = iconOffsetX
    val rightIconX = componentWidth - iconOffsetX
    val iconY = zoneCenterY + 18f

    // Малюємо ліву іконку (збільшений розмір для кращої видимості)
    drawContext.canvas.nativeCanvas.drawCenteredText(
        buttonGroup.leftButton.icon,
        leftIconX,
        iconY,
        64f,
        buttonGroup.leftButton.iconColor
    )

    // Малюємо праву іконку (збільшений розмір для кращої видимості)
    drawContext.canvas.nativeCanvas.drawCenteredText(
        buttonGroup.rightButton.icon,
        rightIconX,
        iconY,
        64f,
        buttonGroup.rightButton.iconColor
    )

    // Малюємо заголовок по центру (якщо є)
    buttonGroup.title?.let { title ->
        val titleY = if (buttonGroup.subtitle != null) {
            zoneCenterY - 5f
        } else {
            zoneCenterY + 12f
        }
        drawContext.canvas.nativeCanvas.drawCenteredText(
            title,
            centerX,
            titleY,
            buttonGroup.titleSize,
            buttonGroup.titleColor
        )
    }

    // Малюємо підзаголовок по центру (якщо є)
    buttonGroup.subtitle?.let { subtitle ->
        drawContext.canvas.nativeCanvas.drawCenteredText(
            subtitle,
            centerX,
            zoneCenterY + 22f,
            buttonGroup.subtitleSize,
            buttonGroup.subtitleColor
        )
    }
}

/**
 * Малює половину полярної зони (ліву або праву)
 */
private fun DrawScope.drawPolarHalf(
    isLeft: Boolean,
    isTop: Boolean,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    yTop: Float,
    yBottom: Float,
    angleTopInner: Float,
    angleBottomInner: Float,
    angleTopOuter: Float,
    angleBottomOuter: Float,
    color: Color
) {
    // Обчислюємо X координати
    val xLeftOuterTop = centerX - sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
    val xRightOuterTop = centerX + sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
    val xLeftOuterBottom = centerX - sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))
    val xRightOuterBottom = centerX + sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))

    val xLeftInnerTop = centerX - sqrt(max(0f, centerRadius * centerRadius - (yTop - centerY) * (yTop - centerY)))
    val xRightInnerTop = centerX + sqrt(max(0f, centerRadius * centerRadius - (yTop - centerY) * (yTop - centerY)))
    val xLeftInnerBottom = centerX - sqrt(max(0f, centerRadius * centerRadius - (yBottom - centerY) * (yBottom - centerY)))
    val xRightInnerBottom = centerX + sqrt(max(0f, centerRadius * centerRadius - (yBottom - centerY) * (yBottom - centerY)))

    val path = Path().apply {
        if (isLeft) {
            // Ліва половина
            moveTo(xLeftOuterTop, yTop)
            lineTo(centerX, yTop)
            lineTo(centerX, yBottom)
            lineTo(xLeftOuterBottom, yBottom)

            // Дуга зовнішнього кола (ліва сторона)
            arcTo(
                Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
                180f - Math.toDegrees(angleBottomOuter.toDouble()).toFloat(),
                Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
                false
            )

            close()

            // Вирізаємо внутрішню частину
            moveTo(xLeftInnerTop, yTop)
            lineTo(centerX, yTop)
            lineTo(centerX, yBottom)
            lineTo(xLeftInnerBottom, yBottom)

            // Дуга внутрішнього кола (ліва сторона)
            arcTo(
                Rect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius, centerY + centerRadius),
                180f - Math.toDegrees(angleBottomInner.toDouble()).toFloat(),
                Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
                false
            )

            close()
        } else {
            // Права половина
            moveTo(centerX, yTop)
            lineTo(xRightOuterTop, yTop)

            // Дуга зовнішнього кола (права сторона)
            arcTo(
                Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
                Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
                Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
                false
            )

            lineTo(centerX, yBottom)
            close()

            // Вирізаємо внутрішню частину
            moveTo(centerX, yTop)
            lineTo(xRightInnerTop, yTop)

            // Дуга внутрішнього кола (права сторона)
            arcTo(
                Rect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius, centerY + centerRadius),
                Math.toDegrees(angleTopInner.toDouble()).toFloat(),
                Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
                false
            )

            lineTo(centerX, yBottom)
            close()
        }

        fillType = PathFillType.EvenOdd
    }

    drawPath(
        path = path,
        color = color,
        style = Fill
    )

    // Малюємо тільки зовнішню обводку (тільки дуги, без вертикальних і горизонтальних ліній)
    if (isLeft) {
        // Зовнішня дуга
        val outerArc = Path().apply {
            moveTo(xLeftOuterTop, yTop)
            arcTo(
                Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
                180f - Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
                -Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
                false
            )
        }
        drawPath(
            path = outerArc,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = 1f)
        )

        // Внутрішня дуга
        val innerArc = Path().apply {
            moveTo(xLeftInnerTop, yTop)
            arcTo(
                Rect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius, centerY + centerRadius),
                180f - Math.toDegrees(angleTopInner.toDouble()).toFloat(),
                -Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
                false
            )
        }
        drawPath(
            path = innerArc,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = 1f)
        )
    } else {
        // Зовнішня дуга
        val outerArc = Path().apply {
            moveTo(xRightOuterTop, yTop)
            arcTo(
                Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
                Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
                Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
                false
            )
        }
        drawPath(
            path = outerArc,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = 1f)
        )

        // Внутрішня дуга
        val innerArc = Path().apply {
            moveTo(xRightInnerTop, yTop)
            arcTo(
                Rect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius, centerY + centerRadius),
                Math.toDegrees(angleTopInner.toDouble()).toFloat(),
                Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
                false
            )
        }
        drawPath(
            path = innerArc,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = 1f)
        )
    }
}

/**
 * Малює under-polar зону (верхню або нижню) - горизонтальний сегмент між middleRadius та outerRadius
 */
private fun DrawScope.drawUnderPolarZone(
    button: CircularButtonData,
    isTop: Boolean,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    outerRadius: Float,
    buttonsPadding: Float,
    underPolarColor: Color
) {
    // Y координати для under-polar зони
    val yTop = if (isTop) {
        centerY - outerRadius
    } else {
        centerY + centerRadius - buttonsPadding
    }
    val yBottom = if (isTop) {
        centerY - centerRadius + buttonsPadding
    } else {
        centerY + outerRadius
    }

    // Обчислюємо кути
    val angleTopInner = asin(max(-1f, min(1f, (yTop - centerY) / middleRadius)))
    val angleBottomInner = asin(max(-1f, min(1f, (yBottom - centerY) / middleRadius)))
    val angleTopOuter = asin(max(-1f, min(1f, (yTop - centerY) / outerRadius)))
    val angleBottomOuter = asin(max(-1f, min(1f, (yBottom - centerY) / outerRadius)))

    // Обчислюємо X координати
    val xLeftOuterTop = centerX - sqrt(max(0f, outerRadius * outerRadius - (yTop - centerY) * (yTop - centerY)))
    val xRightOuterTop = centerX + sqrt(max(0f, outerRadius * outerRadius - (yTop - centerY) * (yTop - centerY)))
    val xLeftOuterBottom = centerX - sqrt(max(0f, outerRadius * outerRadius - (yBottom - centerY) * (yBottom - centerY)))
    val xRightOuterBottom = centerX + sqrt(max(0f, outerRadius * outerRadius - (yBottom - centerY) * (yBottom - centerY)))

    val xLeftInnerTop = centerX - sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
    val xRightInnerTop = centerX + sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
    val xLeftInnerBottom = centerX - sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))
    val xRightInnerBottom = centerX + sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))

    // Створюємо path
    val path = Path().apply {
        moveTo(xLeftOuterTop, yTop)
        lineTo(xRightOuterTop, yTop)

        arcTo(
            Rect(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius),
            Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )

        lineTo(xLeftOuterBottom, yBottom)

        arcTo(
            Rect(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius),
            180f - Math.toDegrees(angleBottomOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )

        close()

        // Вирізаємо внутрішню частину
        moveTo(xLeftInnerTop, yTop)
        lineTo(xRightInnerTop, yTop)

        arcTo(
            Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
            Math.toDegrees(angleTopInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )

        lineTo(xLeftInnerBottom, yBottom)

        arcTo(
            Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
            180f - Math.toDegrees(angleBottomInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )

        close()
        fillType = PathFillType.EvenOdd
    }

    drawPath(
        path = path,
        color = underPolarColor,
        style = Fill
    )

    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = 1f)
    )

    // Малюємо іконку
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

/**
 * Перевіряє, чи точка знаходиться в полярній зоні, і повертає сторону (LEFT/RIGHT) або null
 */
private fun isPointInPolarZone(
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

// Polar zone enums
enum class PolarZone { POLAR, UNDER_POLAR }
enum class PolarSide { LEFT, RIGHT }
