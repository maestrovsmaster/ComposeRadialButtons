package android.test.testuiapplication

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity

/**
 * Enhanced Horizontal Circular Button Layout з іконками та анімаціями
 */
@Composable
fun EnhancedCircularButtonLayout(
    modifier: Modifier = Modifier,
    leftButtons: List<CircularButtonData>,
    rightButtons: List<CircularButtonData>,
    centerLabel: String = "Menu",
    onCenterClick: () -> Unit = {},
    centerColor: Color = Color(0xFF4CAF50),
    buttonColor: Color = Color(0xFF455A64),
    selectedButtonColor: Color = Color(0xFFFF9800),
    iconSegmentColor: Color = Color(0xFF2C3E50),
    centerRadiusRatio: Float = 0.5f,
    middleRadiusRatio: Float = 0.55f,  // Радіус між іконкою та текстом
    outerRadiusRatio: Float = 0.65f
) {
    var selectedButton by remember { mutableStateOf<Pair<Side, Int>?>(null) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val heightPx = with(density) { maxHeight.toPx() }

        val centerRadius = heightPx * centerRadiusRatio
        val middleRadius = heightPx * middleRadiusRatio
        val outerRadius = heightPx * outerRadiusRatio

        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Малюємо ліві кнопки
            leftButtons.forEachIndexed { index, buttonData ->
                // Тут можна викликати функцію малювання або використати окремі Canvas
            }

            // Малюємо центральну кнопку
            drawCircle(
                color = centerColor,
                radius = centerRadius,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )

            drawCircle(
                color = Color.White,
                radius = centerRadius,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.textSize = 40f
                    this.isFakeBoldText = true
                }
                this.drawText(centerLabel, centerX, centerY + 15f, paint)
            }
        }
    }
}
