package android.test.testuiapplication.circularbuttonlayout

import android.graphics.Paint
import android.test.testuiapplication.circularbuttonlayout.data.CircularButtonData
import android.test.testuiapplication.circularbuttonlayout.data.Side
import android.test.testuiapplication.circularbuttonlayout.data.PolarButtonGroup
import android.test.testuiapplication.circularbuttonlayout.data.PolarSide
import android.test.testuiapplication.circularbuttonlayout.data.PolarZone
import android.test.testuiapplication.circularbuttonlayout.data.CircularLayoutTheme
import android.test.testuiapplication.circularbuttonlayout.drawing.drawDualSegmentButtons
import android.test.testuiapplication.circularbuttonlayout.drawing.drawPolarButtonGroup
import android.test.testuiapplication.circularbuttonlayout.drawing.drawUnderPolarZone
import android.test.testuiapplication.circularbuttonlayout.touch.isPointInButton
import android.test.testuiapplication.circularbuttonlayout.touch.isPointInPolarZone
import android.test.testuiapplication.opengl.SphereView
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
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
    theme: CircularLayoutTheme = CircularLayoutTheme.default(),  // Тема для всього layout
    centerRadiusRatio: Float = 0.5f,
    iconSegmentRadiusRatio: Float = 1.2f, // Радіус секції іконок у % від радіуса центрального кола (1.0 = 100%, 1.2 = 120%)
    outerRadiusRatio: Float = 1.0f,       // Зовнішній радіус (ширина кнопок) відносно baseDimension
    buttonsPaddingRatio: Float = 0.0f,    // Padding зверху/знизу для бічних кнопок (0.0 - 0.5)
    textRadialLayout: Boolean = false,    // true = радіальне розташування тексту, false = вертикальне
    circlePaddingRatio: Float = 0.0f ,     // Padding для центрального кола (0.0 - 0.5)
    elementsPadding: Float = 8f,  // Paddings between elements (0.0 - 20.0)
    showSphere: Boolean = false  // Показати 3D сферу в центрі
) {
    // State для натиснутої кнопки (тільки під час натискання)
    var pressedSideButton by remember { mutableStateOf<Pair<Side, Int>?>(null) }
    var pressedPolarButton by remember { mutableStateOf<Triple<PolarZone, PolarSide, Boolean>?>(null) } // (зона, сторона, isTop)

    // State для радіогруп: Map<radioGroupId, Pair<Side, buttonIndex>>
    var selectedRadioButtons by remember { mutableStateOf<Map<String, Pair<Side, Int>>>(emptyMap()) }

    // Reference на SphereGLSurfaceView для анімації світла
    var sphereView by remember { mutableStateOf<android.test.testuiapplication.opengl.SphereGLSurfaceView?>(null) }

    // State для контенту всередині сфери (Composable)
    var sphereContent by remember { mutableStateOf<@Composable () -> Unit>({
        NeonText(
            text = "TRIAL",
            color = Color(0xFFFFD700),
            modifier = Modifier.fillMaxSize()
        )
    }) }
    var nextSphereContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    var contentAlpha by remember { mutableStateOf(1.0f) }  // Альфа для fade ефекту

    // Синхронізація fade ефекту з обертанням сфери
    LaunchedEffect(showSphere) {
        if (showSphere) {
            while (true) {
                kotlinx.coroutines.delay(16)  // ~60 FPS
                val progress = sphereView?.getSphereRotationProgress() ?: 0f

                if (progress > 0f) {
                    // Fade out на першій половині (0.0 -> 0.5)
                    // Fade in на другій половині (0.5 -> 1.0)
                    contentAlpha = if (progress < 0.5f) {
                        // Fade out: 1.0 -> 0.0
                        1.0f - (progress * 2f)
                    } else {
                        // На половині - змінюємо контент
                        if (nextSphereContent != null && progress >= 0.5f && contentAlpha < 0.1f) {
                            sphereContent = nextSphereContent!!
                            nextSphereContent = null
                        }
                        // Fade in: 0.0 -> 1.0
                        (progress - 0.5f) * 2f
                    }
                } else {
                    contentAlpha = 1.0f
                }
            }
        }
    }

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
                    detectTapGestures(
                        onPress = { offset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            val dx = offset.x - centerX
                            val dy = offset.y - centerY
                            val distance = sqrt(dx * dx + dy * dy)

                            // Перевірка центральної кнопки
                            if (distance <= centerRadius) {
                                tryAwaitRelease()
                                sphereView?.triggerLightAnimation()  // Анімація світла

                                // Підготувати новий контент
                                nextSphereContent = {
                                    NeonText(
                                        text = centerLabel,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                sphereView?.triggerSphereRotation()

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
                                        pressedPolarButton = Triple(PolarZone.POLAR, polarSide, true)
                                        tryAwaitRelease()
                                        pressedPolarButton = null
                                        sphereView?.triggerLightAnimation()  // Анімація світла

                                        // Підготувати новий контент
                                        nextSphereContent = {
                                            NeonText(
                                                text = group.title ?: "",
                                                color = Color(0xFFFF6B6B),
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        sphereView?.triggerSphereRotation()

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
                                        pressedPolarButton = Triple(PolarZone.POLAR, polarSide, false)
                                        tryAwaitRelease()
                                        pressedPolarButton = null
                                        sphereView?.triggerLightAnimation()  // Анімація світла

                                        // Підготувати новий контент
                                        nextSphereContent = {
                                            NeonText(
                                                text = group.title ?: "",
                                                color = Color(0xFFFF6B6B),
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        sphereView?.triggerSphereRotation()

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
                                        pressedSideButton = Side.LEFT to index
                                        tryAwaitRelease()
                                        pressedSideButton = null

                                        sphereView?.triggerLightAnimation()  // Анімація світла

                                        // Підготувати новий контент в залежності від кнопки
                                        nextSphereContent = when (button.text) {
                                            "MUSIC" -> {
                                                {
                                                    // Блакитний прямокутний баннер
                                                    Box(
                                                        modifier = Modifier
                                                            .size(200.dp, 100.dp)
                                                            .background(
                                                                Color(0xFF1E88E5),
                                                                RoundedCornerShape(12.dp)
                                                            )
                                                            .padding(16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "Музика",
                                                            color = Color.White,
                                                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                                                        )
                                                    }
                                                }
                                            }
                                            "RADIO" -> {
                                                {
                                                    // Зелений квадратний баннер
                                                    Box(
                                                        modifier = Modifier
                                                            .size(150.dp, 150.dp)
                                                            .background(
                                                                Color(0xFF43A047),
                                                                RoundedCornerShape(12.dp)
                                                            )
                                                            .padding(16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "Радіо",
                                                            color = Color.White,
                                                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                                                        )
                                                    }
                                                }
                                            }
                                            else -> {
                                                {
                                                    // За замовчуванням - неоновий текст
                                                    NeonText(
                                                        text = button.text,
                                                        color = Color(0xFFFFD700),
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                        sphereView?.triggerSphereRotation()

                                        // Якщо кнопка в радіогрупі - зберігаємо її як вибрану
                                        button.radioGroupId?.let { groupId ->
                                            selectedRadioButtons = selectedRadioButtons + (groupId to (Side.LEFT to index))
                                        }

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
                                        pressedSideButton = Side.RIGHT to index
                                        tryAwaitRelease()
                                        pressedSideButton = null

                                        sphereView?.triggerLightAnimation()  // Анімація світла

                                        // Підготувати новий контент (для правих кнопок - стандартний неон)
                                        nextSphereContent = {
                                            NeonText(
                                                text = button.text,
                                                color = Color(0xFFFFD700),
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        sphereView?.triggerSphereRotation()

                                        // Якщо кнопка в радіогрупі - зберігаємо її як вибрану
                                        button.radioGroupId?.let { groupId ->
                                            selectedRadioButtons = selectedRadioButtons + (groupId to (Side.RIGHT to index))
                                        }

                                        button.onClick()
                                        return@detectTapGestures
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Малюємо ліві кнопки
            drawDualSegmentButtons(
                buttons = leftButtons,
                side = Side.LEFT,
                selectedButton = pressedSideButton,
                selectedRadioButtons = selectedRadioButtons,
                centerX = centerX,
                centerY = centerY,
                centerRadius = centerRadius,
                middleRadius = middleRadius,
                outerRadius = outerRadius,
                buttonsPadding = buttonsPadding,
                buttonColor = theme.mainButtons.backgroundColor,
                selectedButtonColor = theme.mainButtons.activeBackgroundColor,
                notchColor = theme.mainButtons.notchColor,
                activeNotchColor = theme.mainButtons.activeNotchColor,
                textColor = theme.mainButtons.textColor,
                activeTextColor = theme.mainButtons.activeTextColor,
                iconColor = theme.mainButtons.iconColor,
                activeIconColor = theme.mainButtons.activeIconColor,
                textRadialLayout = textRadialLayout,
                padding = elementsPadding
            )

            // Малюємо праві кнопки
            drawDualSegmentButtons(
                buttons = rightButtons,
                side = Side.RIGHT,
                selectedButton = pressedSideButton,
                selectedRadioButtons = selectedRadioButtons,
                centerX = centerX,
                centerY = centerY,
                centerRadius = centerRadius,
                middleRadius = middleRadius,
                outerRadius = outerRadius,
                buttonsPadding = buttonsPadding,
                buttonColor = theme.mainButtons.backgroundColor,
                selectedButtonColor = theme.mainButtons.activeBackgroundColor,
                notchColor = theme.mainButtons.notchColor,
                activeNotchColor = theme.mainButtons.activeNotchColor,
                textColor = theme.mainButtons.textColor,
                activeTextColor = theme.mainButtons.activeTextColor,
                iconColor = theme.mainButtons.iconColor,
                activeIconColor = theme.mainButtons.activeIconColor,
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
                        buttonColor = theme.iconButtons.backgroundColor,
                        selectedButtonColor = theme.iconButtons.activeBackgroundColor,
                        selectedPolarButton = pressedPolarButton,
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
                        buttonColor = theme.iconButtons.backgroundColor,
                        selectedButtonColor = theme.iconButtons.activeBackgroundColor,
                        selectedPolarButton = pressedPolarButton,
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
                        underPolarColor = theme.underPolar.backgroundColor,
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
                        underPolarColor = theme.underPolar.backgroundColor,
                        padding = elementsPadding
                    )
                }
            }

            // Малюємо центральну кнопку (якщо немає сфери - звичайний фон, якщо є - напівпрозорий)
            if (!showSphere) {
                drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())

                drawCircle(
                    color = theme.centerButton.backgroundColor,
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
                        this.color = theme.centerButton.textColor.toArgb()
                        this.textAlign = Paint.Align.CENTER
                        this.textSize = theme.centerButton.textSize
                        this.isFakeBoldText = true
                        this.isAntiAlias = true
                    }
                    this.drawText(centerLabel, centerX, centerY + 15f, paint)
                }
            } else {
                // Тільки border для сфери
                drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())
                drawCircle(
                    color = Color.White,
                    radius = centerRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = elementsPadding),
                    blendMode = BlendMode.Clear
                )
                drawContext.canvas.restore()
            }
        }

        // Додаємо 3D сферу в центр (якщо showSphere == true)
        if (showSphere) {
            val density = LocalDensity.current
            val sphereDiameter = with(density) { (centerRadius * 2).toDp() }
            val sphereOffset = with(density) {
                val centerX = widthPx / 2f
                val centerY = heightPx / 2f
                androidx.compose.ui.unit.DpOffset(
                    (centerX - centerRadius).toDp(),
                    (centerY - centerRadius).toDp()
                )
            }

            Box(
                modifier = Modifier
                    .size(sphereDiameter)
                    .offset(sphereOffset.x, sphereOffset.y)
                    .clip(CircleShape)  // Обрізаємо до круглої форми
            ) {
                SphereView(
                    modifier = Modifier.fillMaxSize(),
                    color = theme.centerButton.backgroundColor,
                    alpha = 1.0f,
                    onViewCreated = { view ->
                        sphereView = view  // Зберігаємо reference для анімації
                    }
                )

                // Довільний контент всередині сфери
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = contentAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    sphereContent()
                }
            }

            // Додаємо напівпрозоре коло поверх сфери для кращої інтеграції
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                // Напівпрозорий затемнюючий градієнт по краях для плавного переходу
                drawCircle(
                    color = theme.centerButton.backgroundColor.copy(alpha = 0.15f),
                    radius = centerRadius,
                    center = Offset(centerX, centerY),
                    style = Fill
                )

                // Border поверх всього
                drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())
                drawCircle(
                    color = Color.White,
                    radius = centerRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = elementsPadding),
                    blendMode = BlendMode.Clear
                )
                drawContext.canvas.restore()
            }
        }
    }
}
