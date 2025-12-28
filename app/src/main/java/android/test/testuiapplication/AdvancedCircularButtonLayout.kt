package android.test.testuiapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.*

/**
 * Розширений компонент кругового меню з текстовими мітками
 *
 * Геометрія:
 * - Центральне коло радіусу R1 (centerRadius)
 * - Зовнішнє коло радіусу R2 (outerRadius)
 * - Кожна бічна кнопка - це сегмент кільця між R1 та R2
 * - Внутрішній край обтікає центральну кнопку (дуга радіусу R1)
 * - Зовнішній край обтікає велике коло (дуга радіусу R2)
 */
@Composable
fun AdvancedCircularButtonLayout(
    modifier: Modifier = Modifier,
    centerRadius: Float = 300f,
    outerRadius: Float = 350f,
    buttons: List<ButtonData>,
    centerLabel: String = "Центр",
    onCenterClick: () -> Unit = {},
    centerColor: Color = Color(0xFF4CAF50),
    buttonColor: Color = Color(0xFF607D8B),
    selectedButtonColor: Color = Color(0xFF2196F3)
) {
    var selectedButton by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    val distance = sqrt(dx * dx + dy * dy)

                    if (distance <= centerRadius) {
                        onCenterClick()
                    } else if (distance >= centerRadius && distance <= outerRadius) {
                        val angle = atan2(dy, dx).let { if (it < 0) it + 2 * PI else it }

                        buttons.forEachIndexed { index, button ->
                            val angleInfo = calculateButtonAngle(index, buttons.size)
                            val startAngleRad = Math.toRadians(angleInfo.startAngle.toDouble())
                            val endAngleRad = Math.toRadians(angleInfo.endAngle.toDouble())

                            if (isAngleInRange(angle.toFloat(), startAngleRad.toFloat(), endAngleRad.toFloat())) {
                                selectedButton = index
                                button.onClick()
                            }
                        }
                    }
                }
            }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Малюємо бічні кнопки
        buttons.forEachIndexed { index, button ->
            val angleInfo = calculateButtonAngle(index, buttons.size)

            val path = createButtonPath(
                centerX = centerX,
                centerY = centerY,
                centerRadius = centerRadius,
                outerRadius = outerRadius,
                startAngle = angleInfo.startAngle,
                endAngle = angleInfo.endAngle
            )

            val color = if (selectedButton == index) selectedButtonColor else buttonColor

            // Заповнення
            drawPath(path = path, color = color, style = Fill)
            // Обводка
            drawPath(path = path, color = Color.White, style = Stroke(width = 2f))

            // Текст на кнопці
            val midAngle = Math.toRadians(((angleInfo.startAngle + angleInfo.endAngle) / 2).toDouble())
            val textRadius = (centerRadius + outerRadius) / 2
            val textX = centerX + textRadius * cos(midAngle).toFloat()
            val textY = centerY + textRadius * sin(midAngle).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 32f
                    isFakeBoldText = true
                }
                drawText(button.label, textX, textY + 12f, paint)
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

        // Текст в центрі
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 40f
                isFakeBoldText = true
            }
            drawText(centerLabel, centerX, centerY + 15f, paint)
        }
    }
}

data class ButtonData(
    val label: String,
    val onClick: () -> Unit
)

private data class AngleInfo(
    val startAngle: Float,
    val endAngle: Float
)

/**
 * Обчислює кути початку та кінця для кнопки
 *
 * Кнопки розподіляються рівномірно навколо кола:
 * - Верхні кнопки: від 180° до 360° (або від π до 2π)
 * - Нижні кнопки: від 0° до 180° (або від 0 до π)
 */
private fun calculateButtonAngle(index: Int, totalButtons: Int): AngleInfo {
    val topButtonsCount = (totalButtons + 1) / 2
    val bottomButtonsCount = totalButtons / 2

    return if (index < topButtonsCount) {
        // Верхні кнопки
        val topAngleStep = 180.0 / (topButtonsCount + 1)
        val angleCenter = 180.0 + (index + 1) * topAngleStep
        val startAngle = (angleCenter - topAngleStep / 2).toFloat()
        val endAngle = (angleCenter + topAngleStep / 2).toFloat()
        AngleInfo(startAngle, endAngle)
    } else {
        // Нижні кнопки
        val bottomIndex = index - topButtonsCount
        val bottomAngleStep = 180.0 / (bottomButtonsCount + 1)
        val angleCenter = (bottomIndex + 1) * bottomAngleStep
        val startAngle = (angleCenter - bottomAngleStep / 2).toFloat()
        val endAngle = (angleCenter + bottomAngleStep / 2).toFloat()
        AngleInfo(startAngle, endAngle)
    }
}

/**
 * Створює Path для сегмента кільця
 *
 * Алгоритм:
 * 1. Починаємо з точки на внутрішньому колі (R1) під кутом startAngle
 * 2. Малюємо дугу по внутрішньому колу від startAngle до endAngle
 * 3. Проводимо лінію до зовнішнього кола (R2) під кутом endAngle
 * 4. Малюємо дугу по зовнішньому колу від endAngle до startAngle (у зворотньому напрямку)
 * 5. Закриваємо контур
 */
private fun createButtonPath(
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    outerRadius: Float,
    startAngle: Float,
    endAngle: Float
): Path {
    return Path().apply {
        // 1. Початкова точка на внутрішньому колі
        val startX1 = centerX + centerRadius * cos(Math.toRadians(startAngle.toDouble())).toFloat()
        val startY1 = centerY + centerRadius * sin(Math.toRadians(startAngle.toDouble())).toFloat()
        moveTo(startX1, startY1)

        // 2. Дуга по внутрішньому колю (огинає центральну кнопку)
        arcTo(
            rect = Rect(
                left = centerX - centerRadius,
                top = centerY - centerRadius,
                right = centerX + centerRadius,
                bottom = centerY + centerRadius
            ),
            startAngleDegrees = startAngle,
            sweepAngleDegrees = endAngle - startAngle,
            forceMoveTo = false
        )

        // 3. Лінія до зовнішнього кола
        val endX1 = centerX + outerRadius * cos(Math.toRadians(endAngle.toDouble())).toFloat()
        val endY1 = centerY + outerRadius * sin(Math.toRadians(endAngle.toDouble())).toFloat()
        lineTo(endX1, endY1)

        // 4. Дуга по зовнішньому колю (у зворотньому напрямку)
        arcTo(
            rect = Rect(
                left = centerX - outerRadius,
                top = centerY - outerRadius,
                right = centerX + outerRadius,
                bottom = centerY + outerRadius
            ),
            startAngleDegrees = endAngle,
            sweepAngleDegrees = -(endAngle - startAngle),
            forceMoveTo = false
        )

        // 5. Закриваємо контур (автоматично з'єднає з початковою точкою)
        close()
    }
}

private fun isAngleInRange(angle: Float, startAngle: Float, endAngle: Float): Boolean {
    val normalizedAngle = Math.toDegrees(angle.toDouble()).toFloat()
    var start = startAngle
    var end = endAngle

    if (start > end) {
        end += 360f
    }

    var testAngle = normalizedAngle
    if (testAngle < start) {
        testAngle += 360f
    }

    return testAngle >= start && testAngle <= end
}
