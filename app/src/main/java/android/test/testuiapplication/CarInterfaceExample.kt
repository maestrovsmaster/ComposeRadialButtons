package android.test.testuiapplication

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * Приклад car interface як на скріншоті
 */
@Composable
fun CarInterfaceExample() {
    val leftButtons = remember {
        listOf(
            CircularButtonData(
                icon = "📻",  // Radio icon
                text = "RADIO",
                onClick = { Log.d("CarInterface", "RADIO clicked") },
                iconColor = Color(0xFFFF9800)
            ),
            CircularButtonData(
                icon = "▲",  // Navigation triangle
                text = "NAVI",
                onClick = { Log.d("CarInterface", "NAVI clicked") },
                iconColor = Color(0xFFFF9800)
            ),
            CircularButtonData(
                icon = "♫",  // Music note
                text = "MUSIC",
                onClick = { Log.d("CarInterface", "MUSIC clicked") },
                iconColor = Color(0xFFFF9800)
            )
        )
    }

    val rightButtons = remember {
        listOf(
            CircularButtonData(
                icon = "📞",  // Phone icon
                text = "PHONE",
                onClick = { Log.d("CarInterface", "PHONE clicked") },
                iconColor = Color(0xFFFF9800)
            ),
            CircularButtonData(
                icon = "🌐",  // Globe/Internet icon
                text = "INTERNET",
                onClick = { Log.d("CarInterface", "INTERNET clicked") },
                iconColor = Color(0xFFFF9800)
            ),
            CircularButtonData(
                icon = "⋮⋮⋮",  // Apps grid icon
                text = "APPS",
                onClick = { Log.d("CarInterface", "APPS clicked") },
                iconColor = Color(0xFFFF9800)
            ),
            CircularButtonData(
                icon = "⋮⋮⋮",  // Apps grid icon
                text = "APPS2",
                onClick = { Log.d("CarInterface", "APPS clicked") },
                iconColor = Color(0xFFFF9800)
            )
        )
    }

    // Верхня полярна група кнопок (ліва половина - мінус, права - плюс)
    val topPolarButtonGroup = PolarButtonGroup(
        leftButton = CircularButtonData(
            icon = "-",
            text = "",
            onClick = { Log.d("CarInterface", "TOP LEFT (minus) clicked") },
            iconColor = Color(0xFFFF6B6B)  // Червоний для мінуса
        ),
        rightButton = CircularButtonData(
            icon = "+",
            text = "",
            onClick = { Log.d("CarInterface", "TOP RIGHT (plus) clicked") },
            iconColor = Color(0xFFFF6B6B)  // Червоний для плюса
        ),
        title = "Title",
        subtitle = null,
        titleSize = 32f,
        titleColor = Color.White,
        iconOffsetFromEdge = 0.12f  // 12% від краю
    )

    // Нижня полярна група кнопок (ліва - стрілка вліво, права - стрілка вправо)
    val bottomPolarButtonGroup = PolarButtonGroup(
        leftButton = CircularButtonData(
            icon = "◀",
            text = "",
            onClick = { Log.d("CarInterface", "BOTTOM LEFT (prev) clicked") },
            iconColor = Color.White
        ),
        rightButton = CircularButtonData(
            icon = "▶",
            text = "",
            onClick = { Log.d("CarInterface", "BOTTOM RIGHT (next) clicked") },
            iconColor = Color.White
        ),
        title = "Music Player",
        subtitle = "No name",
        titleSize = 36f,
        titleColor = Color.White,
        subtitleSize = 24f,
        subtitleColor = Color(0xFFFF9800),  // Помаранчевий для підзаголовка
        iconOffsetFromEdge = 0.12f  // 12% від краю
    )

    val topUnderPolarButton = CircularButtonData(
        icon = "⬆",
        text = "UP",
        onClick = { Log.d("CarInterface", "TOP under-polar clicked") },
        iconColor = Color(0xFFFFFFFF)
    )

    val bottomUnderPolarButton = CircularButtonData(
        icon = "⬇",
        text = "DOWN",
        onClick = { Log.d("CarInterface", "BOTTOM under-polar clicked") },
        iconColor = Color(0xFFFFFFFF)
    )

    EnhancedCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        topPolarButtonGroup = topPolarButtonGroup,
        bottomPolarButtonGroup = bottomPolarButtonGroup,
        topUnderPolarButton = topUnderPolarButton,
        bottomUnderPolarButton = bottomUnderPolarButton,
        centerLabel = "TRIAL",
        centerColor = Color(0xFF2C3E50),
        buttonColor = Color(0xFF37474F),
        selectedButtonColor = Color(0xFFFF9800),
        polarButtonColor = Color(0xFF37474F),  // Колір для полярних зон
        underPolarColor = Color(0xFF4CAF50),   // Зелений колір для under-polar зон
        centerRadiusRatio = 0.5f,
        iconSegmentRadiusRatio = 1.6f,  // Радіус секції іконок = 160% від радіуса центрального кола
        outerRadiusRatio = 0.96f,
        buttonsPaddingRatio = 0.01f,    // 1% padding зверху/знизу
        textRadialLayout = true,        // Радіальне розташування тексту
        circlePaddingRatio = 0.15f      // 15% padding для центрального кола
    )
}

/**
 * Приклад з використанням CircularButton компонента напряму
 */
@Composable
fun DirectCircularButtonExample() {
    val buttonData = CircularButtonData(
        icon = "📻",
        text = "RADIO",
        onClick = { Log.d("Test", "Button clicked!") },
        iconColor = Color(0xFFFF9800),
        textColor = Color.White
    )

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularButton(
            modifier = Modifier,
            buttonData = buttonData,
            buttonIndex = 0,
            totalButtons = 3,
            centerX = 500f,
            centerY = 500f,
            innerRadius = 200f,
            middleRadius = 250f,
            outerRadius = 300f,
            side = Side.LEFT,
            isSelected = false,
            buttonColor = Color(0xFF455A64),
            selectedButtonColor = Color(0xFFFF9800),
            iconSegmentColor = Color(0xFF2C3E50)
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun CarInterfacePreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CarInterfaceExample()
        }
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 600)
@Composable
fun DirectButtonPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DirectCircularButtonExample()
        }
    }
}
