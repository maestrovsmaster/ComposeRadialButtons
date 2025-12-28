package android.test.testuiapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import android.test.testuiapplication.ui.theme.TestUIApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestUIApplicationTheme {
                HorizontalCircularButtonLayoutScreen()
            }
        }
    }
}

@Composable
fun HorizontalCircularButtonLayoutScreen() {
    // Кнопки ліворуч від центру
    val leftButtons = remember {
        listOf(
            ButtonData("L1") { Log.d("CircularMenu", "Left Button 1 clicked") },
            ButtonData("L2") { Log.d("CircularMenu", "Left Button 2 clicked") },
            ButtonData("L3") { Log.d("CircularMenu", "Left Button 3 clicked") }
        )
    }

    // Кнопки праворуч від центру
    val rightButtons = remember {
        listOf(
            ButtonData("R1") { Log.d("CircularMenu", "Right Button 1 clicked") },
            ButtonData("R2") { Log.d("CircularMenu", "Right Button 2 clicked") },
            ButtonData("R3") { Log.d("CircularMenu", "Right Button 3 clicked") }
        )
    }

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "Menu",
        onCenterClick = {
            Log.d("CircularMenu", "Center clicked!")
        },
        centerColor = Color(0xFF4CAF50),
        buttonColor = Color(0xFF607D8B),
        selectedButtonColor = Color(0xFF2196F3),
        // Автоматичний розрахунок:
        // centerRadius = containerHeight * 0.5 (50% висоти = R1)
        // outerRadius = containerHeight * 1.0 (100% висоти = R2)
        centerRadiusRatio = 0.5f,
        outerRadiusRatio = 1.0f
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun HorizontalCircularButtonLayoutScreenPreview() {
    TestUIApplicationTheme {
        HorizontalCircularButtonLayoutScreen()
    }
}