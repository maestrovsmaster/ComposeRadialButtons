package android.test.testuiapplication

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * Приклад 1: Базовий layout з 3 кнопками з кожного боку
 */
@Composable
fun Example1_Basic() {
    val leftButtons = listOf(
        ButtonData("L1") { Log.d("Example1", "Left 1") },
        ButtonData("L2") { Log.d("Example1", "Left 2") },
        ButtonData("L3") { Log.d("Example1", "Left 3") }
    )

    val rightButtons = listOf(
        ButtonData("R1") { Log.d("Example1", "Right 1") },
        ButtonData("R2") { Log.d("Example1", "Right 2") },
        ButtonData("R3") { Log.d("Example1", "Right 3") }
    )

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "Menu",
        centerRadiusRatio = 0.5f,
        outerRadiusRatio = 1.0f
    )
}

/**
 * Приклад 2: Асиметричний - різна кількість кнопок
 */
@Composable
fun Example2_Asymmetric() {
    val leftButtons = listOf(
        ButtonData("L1") { },
        ButtonData("L2") { }
    )

    val rightButtons = listOf(
        ButtonData("R1") { },
        ButtonData("R2") { },
        ButtonData("R3") { },
        ButtonData("R4") { }
    )

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        centerRadiusRatio = 0.4f,
        outerRadiusRatio = 0.8f,
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "⚙",
        centerColor = Color(0xFF9C27B0)
    )
}

/**
 * Приклад 3: Медіа-плеєр контроли
 */
@Composable
fun Example3_MediaPlayer() {
    val leftButtons = listOf(
        ButtonData("⏮") { Log.d("MediaPlayer", "Previous") },
        ButtonData("⏪") { Log.d("MediaPlayer", "Rewind") },
        ButtonData("⏸") { Log.d("MediaPlayer", "Pause") }
    )

    val rightButtons = listOf(
        ButtonData("▶") { Log.d("MediaPlayer", "Play") },
        ButtonData("⏩") { Log.d("MediaPlayer", "Fast Forward") },
        ButtonData("⏭") { Log.d("MediaPlayer", "Next") }
    )

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        centerRadiusRatio = 0.5f,
        outerRadiusRatio = 1.0f,
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "🎵",
        centerColor = Color(0xFFE91E63),
        buttonColor = Color(0xFF37474F),
        selectedButtonColor = Color(0xFFFF5722)
    )
}

/**
 * Приклад 4: Навігаційні контроли
 */
@Composable
fun Example4_Navigation() {
    val leftButtons = listOf(
        ButtonData("↖") { Log.d("Navigation", "NW") },
        ButtonData("←") { Log.d("Navigation", "W") },
        ButtonData("↙") { Log.d("Navigation", "SW") }
    )

    val rightButtons = listOf(
        ButtonData("↗") { Log.d("Navigation", "NE") },
        ButtonData("→") { Log.d("Navigation", "E") },
        ButtonData("↘") { Log.d("Navigation", "SE") }
    )

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        centerRadiusRatio = 0.5f,
        outerRadiusRatio = 1.1f,
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "⚔",
        centerColor = Color(0xFF4CAF50),
        buttonColor = Color(0xFF546E7A),
        selectedButtonColor = Color(0xFFFFEB3B)
    )
}

/**
 * Приклад 5: Компактний layout для маленького екрану
 */
@Composable
fun Example5_Compact() {
    val leftButtons = listOf(
        ButtonData("1") { },
        ButtonData("2") { }
    )

    val rightButtons = listOf(
        ButtonData("3") { },
        ButtonData("4") { }
    )

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        centerRadiusRatio = 0.4f,
        outerRadiusRatio = 0.8f,
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "OK",
        centerColor = Color(0xFF607D8B)
    )
}

/**
 * Приклад 6: Великий layout для планшету
 */
@Composable
fun Example6_Large() {
    val leftButtons = listOf(
        ButtonData("Home") { },
        /*ButtonData("Search") { },
        ButtonData("Profile") { },
        ButtonData("Messages") { }*/
    )

    val rightButtons = listOf(
        ButtonData("Settings") { },
      //  ButtonData("Stats") { },
       // ButtonData("Help") { },
       // ButtonData("Logout") { }
    )

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        centerRadiusRatio = 0.5f,
        outerRadiusRatio = 0.65f,
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "Menu",
        centerColor = Color(0xFF3F51B5),
        buttonColor = Color(0xFF455A64),
        selectedButtonColor = Color(0xFFFF9800)
    )
}

/**
 * Приклад 7: Динамічна зміна кнопок
 */
@Composable
fun Example7_Dynamic() {
    var leftCount by remember { mutableStateOf(3) }
    var rightCount by remember { mutableStateOf(3) }

    val leftButtons = remember(leftCount) {
        List(leftCount) { index ->
            ButtonData("L${index + 1}") {
                Log.d("Dynamic", "Left ${index + 1}")
            }
        }
    }

    val rightButtons = remember(rightCount) {
        List(rightCount) { index ->
            ButtonData("R${index + 1}") {
                Log.d("Dynamic", "Right ${index + 1}")
            }
        }
    }

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        centerRadiusRatio = 0.5f,
        outerRadiusRatio = 1.0f,
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "⚙",
        onCenterClick = {
            // Змінюємо кількість кнопок при кліку
            leftCount = if (leftCount < 6) leftCount + 1 else 2
            rightCount = if (rightCount < 6) rightCount + 1 else 2
        },
        centerColor = Color(0xFF00BCD4)
    )
}

// ===== PREVIEWS =====

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun Example1Preview() {
    Example1_Basic()
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun Example2Preview() {
    Example2_Asymmetric()
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun Example3Preview() {
    Example3_MediaPlayer()
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun Example4Preview() {
    Example4_Navigation()
}

@Preview(showBackground = true, widthDp = 600, heightDp = 300)
@Composable
fun Example5Preview() {
    Example5_Compact()
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun Example6Preview() {
    Example6_Large()
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun Example7Preview() {
    Example7_Dynamic()
}
