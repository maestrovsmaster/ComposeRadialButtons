package android.test.testuiapplication.circularbuttonlayout

import android.graphics.Paint
import android.test.testuiapplication.circularbuttonlayout.data.CircularButtonData
import android.test.testuiapplication.circularbuttonlayout.data.Side
import android.test.testuiapplication.circularbuttonlayout.data.PolarButtonGroup
import android.test.testuiapplication.circularbuttonlayout.data.PolarSide
import android.test.testuiapplication.circularbuttonlayout.data.PolarZone
import android.test.testuiapplication.circularbuttonlayout.drawing.drawDualSegmentButtons
import android.test.testuiapplication.circularbuttonlayout.drawing.drawPolarButtonGroup
import android.test.testuiapplication.circularbuttonlayout.drawing.drawUnderPolarZone
import android.test.testuiapplication.circularbuttonlayout.touch.isPointInButton
import android.test.testuiapplication.circularbuttonlayout.touch.isPointInPolarZone
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.Float
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
    circlePaddingRatio: Float = 0.0f ,     // Padding для центрального кола (0.0 - 0.5)
    elementsPadding: Float = 8f  // Paddings between elements (0.0 - 20.0)
) {
    // State для вибраної кнопки: бічна кнопка або полярна кнопка
    var selectedSideButton by remember { mutableStateOf<Pair<Side, Int>?>(null) }
    var selectedPolarButton by remember { mutableStateOf<Triple<PolarZone, PolarSide, Boolean>?>(null) } // (зона, сторона, isTop)

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        Log.d("coords","widthPx: $widthPx, heightPx: $heightPx")

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
                textRadialLayout = textRadialLayout,
                padding = elementsPadding
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
                textRadialLayout = textRadialLayout,
                padding = elementsPadding
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
                        heightPx = heightPx,
                        buttonsPadding = buttonsPadding,
                        buttonColor = polarButtonColor,
                        selectedButtonColor = selectedButtonColor,
                        selectedPolarButton = selectedPolarButton,
                        padding = elementsPadding
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
                        heightPx = heightPx,
                        buttonsPadding = buttonsPadding,
                        buttonColor = polarButtonColor,
                        selectedButtonColor = selectedButtonColor,
                        selectedPolarButton = selectedPolarButton,
                        padding = elementsPadding
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
                        underPolarColor = underPolarColor,
                        padding = elementsPadding
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
                        underPolarColor = underPolarColor,
                        padding = elementsPadding
                    )
                }
            }

            // Малюємо центральну кнопку
            drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())

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
                style = Stroke(width = elementsPadding),
                blendMode = BlendMode.Clear
            )
            drawContext.canvas.restore()

            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    this.textAlign = Paint.Align.CENTER
                    this.textSize = 40f
                    this.isFakeBoldText = true
                    this.isAntiAlias = true
                }
                this.drawText(centerLabel, centerX, centerY + 15f, paint)
            }
        }
    }
}
