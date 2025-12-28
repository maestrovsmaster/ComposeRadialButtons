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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.*

/* ---------- PUBLIC API ---------- */

@Composable
fun HorizontalCircularButtonLayout(
    modifier: Modifier = Modifier,
    leftButtons: List<ButtonData>,
    rightButtons: List<ButtonData>,
    centerLabel: String = "Центр",
    onCenterClick: () -> Unit = {},
    centerColor: Color = Color(0xFF4CAF50),
    buttonColor: Color = Color(0xFF607D8B),
    selectedButtonColor: Color = Color(0xFF2196F3),
    centerRadiusRatio: Float = 0.5f,
    outerRadiusRatio: Float = 1.0f
) {
    var selectedButton by remember { mutableStateOf<Pair<Side, Int>?>(null) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val centerRadius = heightPx * centerRadiusRatio
        val outerRadius = heightPx * outerRadiusRatio

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

                        Log.d("TAG", "detectTapGestures: offset.x = ${offset.x}, centerX = $centerX")

                        if (offset.x < centerX) {
                            Log.d("TAG", "detectTapGestures left")
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

            Log.d("TAG", "Canvas: width=${size.width}, height=${size.height}, centerX=$centerX, centerY=$centerY, centerRadius=$centerRadius, outerRadius=$outerRadius")

            drawButtons(
                buttons = leftButtons,
                side = Side.LEFT,
                selectedButton = selectedButton,
                centerX = centerX,
                centerY = centerY,
                centerRadius = centerRadius,
                outerRadius = outerRadius,
                buttonColor = buttonColor,
                selectedButtonColor = selectedButtonColor
            )

            drawButtons(
                buttons = rightButtons,
                side = Side.RIGHT,
                selectedButton = selectedButton,
                centerX = centerX,
                centerY = centerY,
                centerRadius = centerRadius,
                outerRadius = outerRadius,
                buttonColor = buttonColor,
                selectedButtonColor = selectedButtonColor
            )

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

            drawContext.canvas.nativeCanvas.drawCenteredText(
                centerLabel,
                centerX,
                centerY + 15f,
                40f
            )
        }
    }
}

/* ---------- DRAWING ---------- */

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawButtons(
    buttons: List<ButtonData>,
    side: Side,
    selectedButton: Pair<Side, Int>?,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    outerRadius: Float,
    buttonColor: Color,
    selectedButtonColor: Color
) {
    buttons.forEachIndexed { index, button ->
        val path = createButtonPath(
            index, buttons.size,
            centerX, centerY,
            centerRadius, outerRadius,
            side
        )

        val isSelected = selectedButton == side to index
        val color = if (isSelected) selectedButtonColor else buttonColor

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

        val textPos = getButtonTextPosition(
            index, buttons.size,
            centerX, centerY,
            centerRadius, outerRadius,
            side
        )

        Log.d("TAG", "drawButtons: textPos = $textPos centerY = $centerY" +
                " centerRadius = $centerRadius" +
                "label = ${button.label}")

        drawContext.canvas.nativeCanvas.drawCenteredText(
            button.label,
            textPos.x,
            textPos.y,
            48f
        )
    }
}

/* ---------- GEOMETRY ---------- */

enum class Side { LEFT, RIGHT }

private fun visualIndex(index: Int, total: Int, side: Side): Int =
    if (side == Side.RIGHT) total - 1 - index else index

private fun createButtonPath(
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    outerRadius: Float,
    side: Side
): Path {
    val vIndex = visualIndex(index, total, side)
    val buttonHeight = (2 * centerRadius) / total

    val yTop = centerY - centerRadius + vIndex * buttonHeight
    val yBottom = yTop + buttonHeight

    val angleTopInner = asin((yTop - centerY) / centerRadius)
    val angleBottomInner = asin((yBottom - centerY) / centerRadius)
    val angleTopOuter = asin((yTop - centerY) / outerRadius)
    val angleBottomOuter = asin((yBottom - centerY) / outerRadius)

    return Path().apply {
        val startX = getXOnCircle(centerX, centerY, centerRadius, yTop, side)
        moveTo(startX, yTop)

        arcTo(
            Rect(
                centerX - centerRadius,
                centerY - centerRadius,
                centerX + centerRadius,
                centerY + centerRadius
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

/* ---------- HIT TEST ---------- */

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

/* ---------- HELPERS ---------- */

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

private fun getButtonTextPosition(
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    outerRadius: Float,
    side: Side
): Offset {
    val vIndex = visualIndex(index, total, side)
    val h = (2 * centerRadius) / total
    val y = centerY - centerRadius + vIndex * h + h / 2
    val x = if (side == Side.LEFT)
        centerX - (centerRadius + outerRadius) / 2
    else
        centerX + (centerRadius + outerRadius) / 2

    Log.d("TAG", "getButtonTextPosition: side=$side, centerX=$centerX, centerRadius=$centerRadius, outerRadius=$outerRadius, calculated x=$x")
    return Offset(x, y + 12f)
}

/* ---------- ANGLES ---------- */

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

/* ---------- TEXT ---------- */

private fun android.graphics.Canvas.drawCenteredText(
    text: String,
    x: Float,
    y: Float,
    size: Float
) {
    // Спочатку малюємо обводку (чорну)
    val strokePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = size
        isFakeBoldText = true
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
    }
    drawText(text, x, y, strokePaint)

    // Потім білий текст зверху
    val fillPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = size
        isFakeBoldText = true
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    drawText(text, x, y, fillPaint)
}
