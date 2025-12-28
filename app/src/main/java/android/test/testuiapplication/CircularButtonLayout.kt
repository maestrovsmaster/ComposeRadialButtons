package android.test.testuiapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

data class ButtonShape(
    val path: Path,
    val bounds: Rect,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun CircularButtonLayout(
    modifier: Modifier = Modifier,
    centerRadius: Float = 100f,
    outerRadius: Float = 250f,
    buttonCount: Int = 3,
    onCenterClick: () -> Unit = {},
    onButtonClick: (Int) -> Unit = {}
) {
    var selectedButton by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(600.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)

                        // Перевіряємо клік по центральній кнопці
                        if (distance <= centerRadius) {
                            onCenterClick()
                        } else {
                            // Перевіряємо клік по бічних кнопках
                            val angle = atan2(dy, dx)
                            val normalizedAngle = if (angle < 0) angle + 2 * PI else angle

                            if (distance >= centerRadius && distance <= outerRadius) {
                                val buttonIndex = getButtonIndex(
                                    normalizedAngle.toFloat(),
                                    buttonCount
                                )
                                if (buttonIndex != -1) {
                                    selectedButton = buttonIndex
                                    onButtonClick(buttonIndex)
                                }
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Малюємо бічні кнопки
            drawSideButtons(
                centerX = centerX,
                centerY = centerY,
                centerRadius = centerRadius,
                outerRadius = outerRadius,
                buttonCount = buttonCount,
                selectedButton = selectedButton
            )

            // Малюємо центральну кнопку
            drawCircle(
                color = Color(0xFF4CAF50),
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
        }

        // Текст в центрі
        Text(
            text = "Центр",
            color = Color.White
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSideButtons(
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    outerRadius: Float,
    buttonCount: Int,
    selectedButton: Int?
) {
    // Малюємо верхні кнопки
    val topButtonsCount = (buttonCount + 1) / 2
    val topAngleStep = PI / (topButtonsCount + 1)

    for (i in 0 until topButtonsCount) {
        val angleCenter = PI - (i + 1) * topAngleStep
        val angleStart = angleCenter - topAngleStep / 2
        val angleEnd = angleCenter + topAngleStep / 2

        val path = createButtonPath(
            centerX = centerX,
            centerY = centerY,
            centerRadius = centerRadius,
            outerRadius = outerRadius,
            startAngle = Math.toDegrees(angleStart).toFloat(),
            endAngle = Math.toDegrees(angleEnd).toFloat()
        )

        val color = if (selectedButton == i) Color(0xFF2196F3) else Color(0xFF607D8B)

        drawPath(
            path = path,
            color = color,
            style = Fill
        )
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 2f)
        )
    }

    // Малюємо нижні кнопки
    val bottomButtonsCount = buttonCount / 2
    val bottomAngleStep = PI / (bottomButtonsCount + 1)

    for (i in 0 until bottomButtonsCount) {
        val angleCenter = (i + 1) * bottomAngleStep
        val angleStart = angleCenter - bottomAngleStep / 2
        val angleEnd = angleCenter + bottomAngleStep / 2

        val path = createButtonPath(
            centerX = centerX,
            centerY = centerY,
            centerRadius = centerRadius,
            outerRadius = outerRadius,
            startAngle = Math.toDegrees(angleStart).toFloat(),
            endAngle = Math.toDegrees(angleEnd).toFloat()
        )

        val buttonIndex = topButtonsCount + i
        val color = if (selectedButton == buttonIndex) Color(0xFF2196F3) else Color(0xFF607D8B)

        drawPath(
            path = path,
            color = color,
            style = Fill
        )
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 2f)
        )
    }
}

private fun createButtonPath(
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    outerRadius: Float,
    startAngle: Float,
    endAngle: Float
): Path {
    return Path().apply {
        // Початкова точка на внутрішньому колі
        val startX1 = centerX + centerRadius * cos(Math.toRadians(startAngle.toDouble())).toFloat()
        val startY1 = centerY + centerRadius * sin(Math.toRadians(startAngle.toDouble())).toFloat()
        moveTo(startX1, startY1)

        // Дуга по внутрішньому колу (навколо центральної кнопки)
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

        // Лінія до зовнішнього кола
        val endX1 = centerX + outerRadius * cos(Math.toRadians(endAngle.toDouble())).toFloat()
        val endY1 = centerY + outerRadius * sin(Math.toRadians(endAngle.toDouble())).toFloat()
        lineTo(endX1, endY1)

        // Дуга по зовнішньому колу
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

        // Закриваємо контур
        close()
    }
}

private fun getButtonIndex(angle: Float, buttonCount: Int): Int {
    val topButtonsCount = (buttonCount + 1) / 2
    val bottomButtonsCount = buttonCount / 2

    // Верхні кнопки (від π до 2π або від 180° до 360°)
    if (angle >= PI) {
        val topAngleStep = PI / (topButtonsCount + 1)
        val relativeAngle = angle - PI
        val index = (relativeAngle / topAngleStep).toInt()
        if (index > 0 && index <= topButtonsCount) {
            return index - 1
        }
    }

    // Нижні кнопки (від 0 до π або від 0° до 180°)
    if (angle < PI) {
        val bottomAngleStep = PI / (bottomButtonsCount + 1)
        val index = (angle / bottomAngleStep).toInt()
        if (index > 0 && index <= bottomButtonsCount) {
            return topButtonsCount + index - 1
        }
    }

    return -1
}
