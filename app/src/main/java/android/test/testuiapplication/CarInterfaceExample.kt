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
            )
        )
    }

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        leftButtons = leftButtons.map { ButtonData(it.text, it.onClick) },
        rightButtons = rightButtons.map { ButtonData(it.text, it.onClick) },
        centerLabel = "TRIAL",
        centerColor = Color(0xFF2C3E50),
        buttonColor = Color(0xFF37474F),
        selectedButtonColor = Color(0xFFFF9800),
        centerRadiusRatio = 0.5f,
        outerRadiusRatio = 0.65f
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
