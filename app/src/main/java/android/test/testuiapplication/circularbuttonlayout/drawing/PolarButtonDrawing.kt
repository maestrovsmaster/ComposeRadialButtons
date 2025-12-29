package android.test.testuiapplication.circularbuttonlayout.drawing

import android.test.testuiapplication.circularbuttonlayout.data.PolarButtonGroup
import android.test.testuiapplication.circularbuttonlayout.data.PolarSide
import android.test.testuiapplication.circularbuttonlayout.data.PolarZone
import android.test.testuiapplication.circularbuttonlayout.utils.drawCenteredText
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.*

/**
 * Малює полярну групу кнопок (верхню або нижню) - дві половини з іконками по боках і текстом по центру
 */
fun DrawScope.drawPolarButtonGroup(
    buttonGroup: PolarButtonGroup,
    isTop: Boolean,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    middleRadius: Float,
    componentWidth: Float,
    heightPx: Float,
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



    Log.d("coords","yTop: $yTop, yBottom: $yBottom isTop = $isTop")
    // Центр зони для тексту і іконок (yTop + yBottom) / 2
    val zoneCenterY = if(isTop) {(yBottom) / 2 }else{
       yTop + Math.abs(heightPx - yTop)/2 // (yTop + yBottom) // 2
    }

    // Позиції іконок - радіально на колі, але ближче до країв
    // Використовуємо радіус між centerRadius та middleRadius
    val iconRadius = (centerRadius + middleRadius) / 2

    // Обчислюємо кут для позиції іконки на основі Y
    val dy = zoneCenterY - centerY
    val angle = if (abs(dy) > iconRadius) {
        if (dy > 0) Math.PI / 2 else -Math.PI / 2
    } else {
        asin((dy / iconRadius).toDouble())
    }

    // Обчислюємо базову позицію на колі
    val baseIconDx = (iconRadius * cos(angle)).toFloat()

    // Збільшуємо відступ від центру для іконок
    val iconDistanceMultiplier = 1.0f + buttonGroup.iconOffsetFromEdge
    val leftIconX = centerX - baseIconDx * iconDistanceMultiplier
    val rightIconX = centerX + baseIconDx * iconDistanceMultiplier
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
fun DrawScope.drawPolarHalf(
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

    // Малюємо тільки зовнішні дуги (без горизонтальних і вертикальних ліній)
    val outerArcPath = Path()
    val innerArcPath = Path()

    if (isLeft) {
        // Зовнішня дуга (ліва сторона)
        outerArcPath.moveTo(xLeftOuterBottom, yBottom)
        outerArcPath.arcTo(
            Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
            180f - Math.toDegrees(angleBottomOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )

        // Внутрішня дуга (ліва сторона)
        innerArcPath.moveTo(xLeftInnerBottom, yBottom)
        innerArcPath.arcTo(
            Rect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius, centerY + centerRadius),
            180f - Math.toDegrees(angleBottomInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )
    } else {
        // Зовнішня дуга (права сторона)
        outerArcPath.moveTo(xRightOuterTop, yTop)
        outerArcPath.arcTo(
            Rect(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius),
            Math.toDegrees(angleTopOuter.toDouble()).toFloat(),
            Math.toDegrees((angleBottomOuter - angleTopOuter).toDouble()).toFloat(),
            false
        )

        // Внутрішня дуга (права сторона)
        innerArcPath.moveTo(xRightInnerTop, yTop)
        innerArcPath.arcTo(
            Rect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius, centerY + centerRadius),
            Math.toDegrees(angleTopInner.toDouble()).toFloat(),
            Math.toDegrees((angleBottomInner - angleTopInner).toDouble()).toFloat(),
            false
        )
    }

    drawPath(
        path = outerArcPath,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = 1f)
    )

    drawPath(
        path = innerArcPath,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = 1f)
    )
}
