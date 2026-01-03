package com.radialbuttons.circularbuttonlayout

import android.graphics.Paint
import com.radialbuttons.circularbuttonlayout.data.CircularButtonData
import com.radialbuttons.circularbuttonlayout.data.Side
import com.radialbuttons.circularbuttonlayout.data.PolarButtonGroup
import com.radialbuttons.circularbuttonlayout.data.PolarSide
import com.radialbuttons.circularbuttonlayout.data.PolarZone
import com.radialbuttons.circularbuttonlayout.data.CircularLayoutTheme
import com.radialbuttons.circularbuttonlayout.data.UnderPolarZone
import com.radialbuttons.circularbuttonlayout.drawing.drawDualSegmentButtons
import com.radialbuttons.circularbuttonlayout.drawing.drawPolarButtonGroup
import com.radialbuttons.circularbuttonlayout.opengl.SphereGLSurfaceView
import com.radialbuttons.circularbuttonlayout.touch.isPointInButton
import com.radialbuttons.circularbuttonlayout.touch.isPointInPolarZone
import com.radialbuttons.circularbuttonlayout.touch.isPointInIconSegment
import com.radialbuttons.circularbuttonlayout.opengl.SphereView
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
 * Enhanced Horizontal Circular Button Layout with icons
 * Uses single Canvas for all buttons
 */
@Composable
fun EnhancedCircularButtonLayout(
    modifier: Modifier = Modifier,
    leftButtons: List<CircularButtonData>,
    rightButtons: List<CircularButtonData>,
    topPolarButtonGroup: PolarButtonGroup? = null,      // Top polar button group
    bottomPolarButtonGroup: PolarButtonGroup? = null,   // Bottom polar button group
    topLeftUnderPolarZone: UnderPolarZone? = null,      // Top left under-polar zone
    topRightUnderPolarZone: UnderPolarZone? = null,     // Top right under-polar zone
    bottomLeftUnderPolarZone: UnderPolarZone? = null,   // Bottom left under-polar zone
    bottomRightUnderPolarZone: UnderPolarZone? = null,  // Bottom right under-polar zone
    centerLabel: String = "Menu",
    onCenterClick: () -> Unit = {},
    onButtonClick: ((CircularButtonData) -> @Composable () -> Unit)? = null,  // Callback for custom content on button click
    theme: CircularLayoutTheme = CircularLayoutTheme.default(),  // Theme for entire layout
    centerRadiusRatio: Float = 0.5f,
    iconSegmentRadiusRatio: Float = 1.2f, // Icon segment radius in % of center circle radius (1.0 = 100%, 1.2 = 120%)
    outerRadiusRatio: Float = 1.0f,       // Outer radius (button width) relative to baseDimension
    buttonsPaddingRatio: Float = 0.0f,    // Padding top/bottom for side buttons (0.0 - 0.5)
    textRadialLayout: Boolean = false,    // true = radial text layout, false = vertical
    circlePaddingRatio: Float = 0.0f ,     // Padding for center circle (0.0 - 0.5)
    elementsPadding: Float = 8f,  // Paddings between elements (0.0 - 20.0)
    showSphere: Boolean = false,  // Show 3D sphere in center
    enableHapticFeedback: Boolean = true  // Enable haptic feedback on button press
) {
    // Haptic feedback
    val hapticFeedback = LocalHapticFeedback.current

    // State for pressed button (only during press)
    var pressedSideButton by remember { mutableStateOf<Pair<Side, Int>?>(null) }
    var pressedSideButtonIcon by remember { mutableStateOf<Pair<Side, Int>?>(null) }
    var pressedPolarButton by remember { mutableStateOf<Triple<PolarZone, PolarSide, Boolean>?>(null) } // (zone, side, isTop)

    // State for radio groups: Map<radioGroupId, Pair<Side, buttonIndex>>
    var selectedRadioButtons by remember { mutableStateOf<Map<String, Pair<Side, Int>>>(emptyMap()) }

    // Reference to SphereGLSurfaceView for light animation
    var sphereView by remember { mutableStateOf<SphereGLSurfaceView?>(null) }

    // State for content inside sphere (Composable)
    var sphereContent by remember { mutableStateOf<@Composable () -> Unit>({
        NeonText(
            text = "TRIAL",
            color = Color(0xFFFFD700),
            modifier = Modifier.fillMaxSize()
        )
    }) }
    var nextSphereContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    var contentAlpha by remember { mutableStateOf(1.0f) }  // Alpha for fade effect

    // Synchronize fade effect with sphere rotation
    LaunchedEffect(showSphere) {
        if (showSphere) {
            while (true) {
                kotlinx.coroutines.delay(16)  // ~60 FPS
                val progress = sphereView?.getSphereRotationProgress() ?: 0f

                if (progress > 0f) {
                    // Fade out on first half (0.0 -> 0.5)
                    // Fade in on second half (0.5 -> 1.0)
                    contentAlpha = if (progress < 0.5f) {
                        // Fade out: 1.0 -> 0.0
                        1.0f - (progress * 2f)
                    } else {
                        // At halfway point - change content
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
        // Circle diameter = Smallest_screen_side - 2 * circle_padding
        val circlePadding = min(widthPx, heightPx) * circlePaddingRatio
        val baseDimension = min(widthPx, heightPx) - 2 * circlePadding

        val centerRadius = baseDimension * centerRadiusRatio
        val middleRadius = centerRadius * iconSegmentRadiusRatio  // Relative to center circle radius
        val outerRadius = min(widthPx, heightPx) * outerRadiusRatio

        // Padding for side buttons
        val buttonsPadding = baseDimension * buttonsPaddingRatio

        // Height of side buttons area = diameter - 2*padding
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

                            // Check central button
                            if (distance <= centerRadius) {
                                tryAwaitRelease()
                                sphereView?.triggerLightAnimation()  // Light animation

                                // Prepare new content
                                nextSphereContent = {
                                    NeonText(
                                        text = centerLabel,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                sphereView?.setContentColor(Color(0xFFFFD700))  // Gold color
                                sphereView?.triggerSphereRotation()

                                // Haptic feedback
                                if (enableHapticFeedback) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }

                                onCenterClick()
                                return@detectTapGestures
                            }

                            // Check polar zones (if padding exists)
                            if (buttonsPadding > 0) {
                                // Top polar zone
                                topPolarButtonGroup?.let { group ->
                                    val polarSide = isPointInPolarZone(
                                        offset, true, centerX, centerY,
                                        centerRadius, middleRadius, buttonsPadding
                                    )
                                    if (polarSide != null) {
                                        pressedPolarButton = Triple(PolarZone.POLAR, polarSide, true)
                                        tryAwaitRelease()
                                        pressedPolarButton = null
                                        sphereView?.triggerLightAnimation()  // Light animation

                                        // Prepare new content
                                        nextSphereContent = {
                                            NeonText(
                                                text = group.title ?: "",
                                                color = Color(0xFFFF6B6B),
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        sphereView?.setContentColor(Color(0xFFFF6B6B))  // Red color
                                        sphereView?.triggerSphereRotation()

                                        // Haptic feedback
                                        if (enableHapticFeedback) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }

                                        if (polarSide == PolarSide.LEFT) {
                                            group.leftButton.onClick()
                                        } else {
                                            group.rightButton.onClick()
                                        }
                                        return@detectTapGestures
                                    }
                                }

                                // Bottom polar zone
                                bottomPolarButtonGroup?.let { group ->
                                    val polarSide = isPointInPolarZone(
                                        offset, false, centerX, centerY,
                                        centerRadius, middleRadius, buttonsPadding
                                    )
                                    if (polarSide != null) {
                                        pressedPolarButton = Triple(PolarZone.POLAR, polarSide, false)
                                        tryAwaitRelease()
                                        pressedPolarButton = null
                                        sphereView?.triggerLightAnimation()  // Light animation

                                        // Prepare new content
                                        nextSphereContent = {
                                            NeonText(
                                                text = group.title ?: "",
                                                color = Color(0xFFFF6B6B),
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        sphereView?.setContentColor(Color(0xFFFF6B6B))  // Red color
                                        sphereView?.triggerSphereRotation()

                                        // Haptic feedback
                                        if (enableHapticFeedback) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }

                                        if (polarSide == PolarSide.LEFT) {
                                            group.leftButton.onClick()
                                        } else {
                                            group.rightButton.onClick()
                                        }
                                        return@detectTapGestures
                                    }
                                }
                            }

                            // Check side buttons
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

                                        // Use callback for custom content or standard neon
                                        nextSphereContent = onButtonClick?.invoke(button) ?: {
                                            NeonText(
                                                text = button.text,
                                                color = Color(0xFFFFD700),
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        sphereView?.setContentColor(Color(0xFFFFD700))  // Default gold
                                        sphereView?.triggerSphereRotation()

                                        // If button is in radio group - save it as selected
                                        button.radioGroupId?.let { groupId ->
                                            selectedRadioButtons = selectedRadioButtons + (groupId to (Side.LEFT to index))
                                        }

                                        // Haptic feedback
                                        if (enableHapticFeedback) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }

                                        // Check if click was on icon segment and call appropriate callback
                                        val clickedOnIcon = isPointInIconSegment(
                                            offset, centerX, centerY, centerRadius, middleRadius
                                        )
                                        if (clickedOnIcon && button.onIconClick != null) {
                                            button.onIconClick.invoke()
                                        } else {
                                            button.onClick()
                                        }
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

                                        // Use callback for custom content or standard neon
                                        nextSphereContent = onButtonClick?.invoke(button) ?: {
                                            NeonText(
                                                text = button.text,
                                                color = Color(0xFFFFD700),
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        sphereView?.setContentColor(Color(0xFFFFD700))  // Default gold
                                        sphereView?.triggerSphereRotation()

                                        // If button is in radio group - save it as selected
                                        button.radioGroupId?.let { groupId ->
                                            selectedRadioButtons = selectedRadioButtons + (groupId to (Side.RIGHT to index))
                                        }

                                        // Haptic feedback
                                        if (enableHapticFeedback) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }

                                        // Check if click was on icon segment and call appropriate callback
                                        val clickedOnIcon = isPointInIconSegment(
                                            offset, centerX, centerY, centerRadius, middleRadius
                                        )
                                        if (clickedOnIcon && button.onIconClick != null) {
                                            button.onIconClick.invoke()
                                        } else {
                                            button.onClick()
                                        }
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

            // Draw left buttons
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
                padding = elementsPadding,
            )

            // Draw right buttons
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

            // Draw top polar zone (if padding exists)
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

            // Draw bottom polar zone (if padding exists)
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

            // Draw background for top under-polar zones (if padding exists)
            if (buttonsPadding > 0 && (topLeftUnderPolarZone != null || topRightUnderPolarZone != null)) {
                val yTop = max(0f, centerY - outerRadius)  // Clamp to screen bounds
                val yBottom = centerY - centerRadius + buttonsPadding

                val angleTopInner = asin(max(-1f, min(1f, (yTop - centerY) / middleRadius)))
                val angleBottomInner = asin(max(-1f, min(1f, (yBottom - centerY) / middleRadius)))
                val angleTopOuter = asin(max(-1f, min(1f, (yTop - centerY) / outerRadius)))
                val angleBottomOuter = asin(max(-1f, min(1f, (yBottom - centerY) / outerRadius)))

                // Left zone
                if (topLeftUnderPolarZone != null) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val xLeftOuterTop = centerX - sqrt(max(0f, outerRadius * outerRadius - (yTop - centerY) * (yTop - centerY)))
                        val xLeftOuterBottom = centerX - sqrt(max(0f, outerRadius * outerRadius - (yBottom - centerY) * (yBottom - centerY)))
                        val xLeftInnerTop = centerX - sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
                        val xLeftInnerBottom = centerX - sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))

                        moveTo(xLeftOuterTop, yTop)
                        lineTo(centerX, yTop)
                        lineTo(centerX, yBottom)
                        lineTo(xLeftOuterBottom, yBottom)

                        arcTo(
                            androidx.compose.ui.geometry.Rect(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius),
                            180f - Math.toDegrees(angleBottomOuter.toDouble()).toFloat(),
                            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
                            false
                        )
                        close()

                        moveTo(xLeftInnerTop, yTop)
                        lineTo(centerX, yTop)
                        lineTo(centerX, yBottom)
                        lineTo(xLeftInnerBottom, yBottom)

                        arcTo(
                            androidx.compose.ui.geometry.Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
                            180f - Math.toDegrees(angleBottomInner.toDouble()).toFloat(),
                            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
                            false
                        )
                        close()
                        fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                    }

                    drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())
                    drawPath(path = path, color = theme.underPolar.backgroundColor, style = Fill)
                    drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = elementsPadding), blendMode = BlendMode.Clear)
                    drawContext.canvas.restore()
                }

                // Right zone
                if (topRightUnderPolarZone != null) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val xRightOuterTop = centerX + sqrt(max(0f, outerRadius * outerRadius - (yTop - centerY) * (yTop - centerY)))
                        val xRightOuterBottom = centerX + sqrt(max(0f, outerRadius * outerRadius - (yBottom - centerY) * (yBottom - centerY)))
                        val xRightInnerTop = centerX + sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
                        val xRightInnerBottom = centerX + sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))

                        moveTo(centerX, yTop)
                        lineTo(xRightOuterTop, yTop)

                        arcTo(
                            androidx.compose.ui.geometry.Rect(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius),
                            Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
                            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
                            false
                        )

                        lineTo(centerX, yBottom)
                        close()

                        moveTo(centerX, yTop)
                        lineTo(xRightInnerTop, yTop)

                        arcTo(
                            androidx.compose.ui.geometry.Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
                            Math.toDegrees(angleTopInner.toDouble()).toFloat(),
                            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
                            false
                        )

                        lineTo(centerX, yBottom)
                        close()
                        fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                    }

                    drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())
                    drawPath(path = path, color = theme.underPolar.backgroundColor, style = Fill)
                    drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = elementsPadding), blendMode = BlendMode.Clear)
                    drawContext.canvas.restore()
                }
            }

            // Draw background for bottom under-polar zones (if padding exists)
            if (buttonsPadding > 0 && (bottomLeftUnderPolarZone != null || bottomRightUnderPolarZone != null)) {
                val yTop = centerY + centerRadius - buttonsPadding
                val yBottom = min(heightPx, centerY + outerRadius)  // Clamp to screen bounds

                val angleTopInner = asin(max(-1f, min(1f, (yTop - centerY) / middleRadius)))
                val angleBottomInner = asin(max(-1f, min(1f, (yBottom - centerY) / middleRadius)))
                val angleTopOuter = asin(max(-1f, min(1f, (yTop - centerY) / outerRadius)))
                val angleBottomOuter = asin(max(-1f, min(1f, (yBottom - centerY) / outerRadius)))

                // Left zone
                if (bottomLeftUnderPolarZone != null) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val xLeftOuterTop = centerX - sqrt(max(0f, outerRadius * outerRadius - (yTop - centerY) * (yTop - centerY)))
                        val xLeftOuterBottom = centerX - sqrt(max(0f, outerRadius * outerRadius - (yBottom - centerY) * (yBottom - centerY)))
                        val xLeftInnerTop = centerX - sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
                        val xLeftInnerBottom = centerX - sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))

                        moveTo(xLeftOuterTop, yTop)
                        lineTo(centerX, yTop)
                        lineTo(centerX, yBottom)
                        lineTo(xLeftOuterBottom, yBottom)

                        arcTo(
                            androidx.compose.ui.geometry.Rect(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius),
                            180f - Math.toDegrees(angleBottomOuter.toDouble()).toFloat(),
                            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
                            false
                        )
                        close()

                        moveTo(xLeftInnerTop, yTop)
                        lineTo(centerX, yTop)
                        lineTo(centerX, yBottom)
                        lineTo(xLeftInnerBottom, yBottom)

                        arcTo(
                            androidx.compose.ui.geometry.Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
                            180f - Math.toDegrees(angleBottomInner.toDouble()).toFloat(),
                            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
                            false
                        )
                        close()
                        fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                    }

                    drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())
                    drawPath(path = path, color = theme.underPolar.backgroundColor, style = Fill)
                    drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = elementsPadding), blendMode = BlendMode.Clear)
                    drawContext.canvas.restore()
                }

                // Right zone
                if (bottomRightUnderPolarZone != null) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val xRightOuterTop = centerX + sqrt(max(0f, outerRadius * outerRadius - (yTop - centerY) * (yTop - centerY)))
                        val xRightOuterBottom = centerX + sqrt(max(0f, outerRadius * outerRadius - (yBottom - centerY) * (yBottom - centerY)))
                        val xRightInnerTop = centerX + sqrt(max(0f, middleRadius * middleRadius - (yTop - centerY) * (yTop - centerY)))
                        val xRightInnerBottom = centerX + sqrt(max(0f, middleRadius * middleRadius - (yBottom - centerY) * (yBottom - centerY)))

                        moveTo(centerX, yTop)
                        lineTo(xRightOuterTop, yTop)

                        arcTo(
                            androidx.compose.ui.geometry.Rect(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius),
                            Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
                            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
                            false
                        )

                        lineTo(centerX, yBottom)
                        close()

                        moveTo(centerX, yTop)
                        lineTo(xRightInnerTop, yTop)

                        arcTo(
                            androidx.compose.ui.geometry.Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
                            Math.toDegrees(angleTopInner.toDouble()).toFloat(),
                            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
                            false
                        )

                        lineTo(centerX, yBottom)
                        close()
                        fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                    }

                    drawContext.canvas.saveLayer(size.toRect(), androidx.compose.ui.graphics.Paint())
                    drawPath(path = path, color = theme.underPolar.backgroundColor, style = Fill)
                    drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = elementsPadding), blendMode = BlendMode.Clear)
                    drawContext.canvas.restore()
                }
            }

            // Draw center button (if no sphere - regular background, if sphere - semi-transparent)
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
                // Only border for sphere
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

        // Add 3D sphere in center (if showSphere == true)
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
                    .clip(CircleShape)  // Clip to circle shape
            ) {
                SphereView(
                    modifier = Modifier.fillMaxSize(),
                    color = theme.centerButton.backgroundColor,
                    alpha = 1.0f,
                    onViewCreated = { view ->
                        sphereView = view  // Save reference for animation
                        // Set initial reflection color (gold for TRIAL)
                        view.setContentColor(Color(0xFFFFD700))
                    }
                )

                // Arbitrary content inside sphere
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = contentAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    sphereContent()
                }
            }

            // Render Composable children for under polar zones

            // Top left zone
            topLeftUnderPolarZone?.let { zone ->
                if (buttonsPadding > 0 && zone.children.isNotEmpty()) {
                    val yTop = with(density) { max(0f, heightPx / 2f - outerRadius).toDp() }  // Clamp to screen bounds
                    val yBottom = with(density) { (heightPx / 2f - centerRadius + buttonsPadding).toDp() }
                    val xLeft = with(density) { 0.dp }
                    val xRight = with(density) { (widthPx / 2f).toDp() }

                    Box(
                        modifier = Modifier
                            .offset(xLeft, yTop)
                            .width(xRight)
                            .height(yBottom - yTop),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            zone.children.forEach { child ->
                                child()
                            }
                        }
                    }
                }
            }

            // Top right zone
            topRightUnderPolarZone?.let { zone ->
                if (buttonsPadding > 0 && zone.children.isNotEmpty()) {
                    val yTop = with(density) { max(0f, heightPx / 2f - outerRadius).toDp() }  // Clamp to screen bounds
                    val yBottom = with(density) { (heightPx / 2f - centerRadius + buttonsPadding).toDp() }
                    val xLeft = with(density) { (widthPx / 2f).toDp() }
                    val xRight = with(density) { widthPx.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(xLeft, yTop)
                            .width(xRight - xLeft)
                            .height(yBottom - yTop),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            zone.children.forEach { child ->
                                child()
                            }
                        }
                    }
                }
            }

            // Bottom left zone
            bottomLeftUnderPolarZone?.let { zone ->
                if (buttonsPadding > 0 && zone.children.isNotEmpty()) {
                    val yTop = with(density) { (heightPx / 2f + centerRadius - buttonsPadding).toDp() }
                    val yBottom = with(density) { min(heightPx, heightPx / 2f + outerRadius).toDp() }  // Clamp to screen bounds
                    val xLeft = with(density) { 0.dp }
                    val xRight = with(density) { (widthPx / 2f).toDp() }

                    Box(
                        modifier = Modifier
                            .offset(xLeft, yTop)
                            .width(xRight)
                            .height(yBottom - yTop),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            zone.children.forEach { child ->
                                child()
                            }
                        }
                    }
                }
            }

            // Bottom right zone
            bottomRightUnderPolarZone?.let { zone ->
                if (buttonsPadding > 0 && zone.children.isNotEmpty()) {
                    val yTop = with(density) { (heightPx / 2f + centerRadius - buttonsPadding).toDp() }
                    val yBottom = with(density) { min(heightPx, heightPx / 2f + outerRadius).toDp() }  // Clamp to screen bounds
                    val xLeft = with(density) { (widthPx / 2f).toDp() }
                    val xRight = with(density) { widthPx.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(xLeft, yTop)
                            .width(xRight - xLeft)
                            .height(yBottom - yTop),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            zone.children.forEach { child ->
                                child()
                            }
                        }
                    }
                }
            }

            // Add semi-transparent circle over sphere for better integration
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                // Semi-transparent darkening gradient at edges for smooth transition
                drawCircle(
                    color = theme.centerButton.backgroundColor.copy(alpha = 0.15f),
                    radius = centerRadius,
                    center = Offset(centerX, centerY),
                    style = Fill
                )

                // Border on top of everything
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
