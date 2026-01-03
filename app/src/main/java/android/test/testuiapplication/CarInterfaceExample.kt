package android.test.testuiapplication

import com.radialbuttons.circularbuttonlayout.EnhancedCircularButtonLayout
import com.radialbuttons.circularbuttonlayout.data.CircularButtonData
import com.radialbuttons.circularbuttonlayout.data.CircularLayoutTheme
import com.radialbuttons.circularbuttonlayout.data.CenterButtonTheme
import com.radialbuttons.circularbuttonlayout.data.MainButtonTheme
import com.radialbuttons.circularbuttonlayout.data.IconButtonTheme
import com.radialbuttons.circularbuttonlayout.data.UnderPolarTheme
import com.radialbuttons.circularbuttonlayout.data.PolarButtonGroup
import com.radialbuttons.circularbuttonlayout.data.Side
import com.radialbuttons.circularbuttonlayout.data.UnderPolarZone
import com.radialbuttons.circularbuttonlayout.components.CircularButton
import com.radialbuttons.circularbuttonlayout.NeonText
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Car interface example as shown in screenshot
 */
@Composable
fun CarInterfaceExample() {
    val leftButtons = remember {
        listOf(
            CircularButtonData(
                icon = "📻",  // Radio icon
                text = "RADIO",
                onClick = { Log.d("CarInterface", "RADIO button clicked") },
                onIconClick = { Log.d("CarInterface", "RADIO icon clicked - quick tune!") },  // Separate icon click
                iconColor = Color(0xFFFF9800),
                radioGroupId = "media"  // RADIO and MUSIC in same radio group
            ),
            CircularButtonData(
                icon = "♫",  // Music note
                text = "MUSIC",
                onClick = { Log.d("CarInterface", "MUSIC button clicked") },
                onIconClick = { Log.d("CarInterface", "MUSIC icon clicked - quick play/pause!") },  // Separate icon click
                iconColor = Color(0xFFFF9800),
                radioGroupId = "media"  // RADIO and MUSIC in same radio group
            ),
            CircularButtonData(
                icon = "▲",  // Navigation triangle
                text = "NAVI",
                onClick = { Log.d("CarInterface", "NAVI button clicked") },
                onIconClick = { Log.d("CarInterface", "NAVI icon clicked - recenter map!") },  // Separate icon click
                iconColor = Color(0xFFFF9800),
                radioGroupId = "media"
                // Without radioGroupId - regular button
            ),

        )
    }

    val rightButtons = remember {
        listOf(
            CircularButtonData(
                icon = "📞",  // Phone icon
                text = "PHONE",
                onClick = { Log.d("CarInterface", "PHONE button clicked - open contacts") },
                onIconClick = { Log.d("CarInterface", "PHONE icon clicked - quick dial!") },  // Separate icon click
                iconColor = Color(0xFFFF9800)
            ),
            CircularButtonData(
                icon = "🌐",  // Globe/Internet icon
                text = "INTERNET",
                onClick = { Log.d("CarInterface", "INTERNET button clicked") },
                onIconClick = { Log.d("CarInterface", "INTERNET icon clicked - quick search!") },  // Separate icon click
                iconColor = Color(0xFFFF9800)
            ),
            CircularButtonData(
                icon = "⋮⋮⋮",  // Apps grid icon
                text = "APPS",
                onClick = { Log.d("CarInterface", "APPS button clicked - show all apps") },
                onIconClick = { Log.d("CarInterface", "APPS icon clicked - quick launcher!") },  // Separate icon click
                iconColor = Color(0xFFFF9800)
            ),
            CircularButtonData(
                icon = "⋮⋮⋮",  // Apps grid icon
                text = "APPS2",
                onClick = { Log.d("CarInterface", "APPS2 button clicked") },
                onIconClick = { Log.d("CarInterface", "APPS2 icon clicked - favorites!") },  // Separate icon click
                iconColor = Color(0xFFFF9800)
            )
        )
    }

    // Top polar button group (left half - minus, right - plus)
    val topPolarButtonGroup = PolarButtonGroup(
        leftButton = CircularButtonData(
            icon = "-",
            text = "",
            onClick = { Log.d("CarInterface", "TOP LEFT (minus) clicked") },
            iconColor = Color(0xFFFF6B6B)  // Red for minus
        ),
        rightButton = CircularButtonData(
            icon = "+",
            text = "",
            onClick = { Log.d("CarInterface", "TOP RIGHT (plus) clicked") },
            iconColor = Color(0xFFFF6B6B)  // Red for plus
        ),
        title = "Title",
        subtitle = null,
        titleSize = 32f,
        titleColor = Color.White,
        iconOffsetFromEdge = 0.12f  // 12% from edge
    )

    // Bottom polar button group (left - arrow left, right - arrow right)
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
        subtitleColor = Color(0xFFFF9800),  // Orange for subtitle
        iconOffsetFromEdge = 0.12f  // 12% from edge
    )

    // Under polar zones with custom content (icons + text examples)
    val topLeftUnderPolarZone = UnderPolarZone(
        children = listOf(
            {
                Text(
                    text = "🔊",  // Speaker icon
                    color = Color(0xFFFF9800),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            },
            {
                Text(
                    text = "Vol",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            {
                Text(
                    text = "↑",  // Up arrow
                    color = Color(0xFF4CAF50),  // Green
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        )
    )

    val topRightUnderPolarZone = UnderPolarZone(
        children = listOf(
            {
                Text(
                    text = "⚙",  // Settings icon
                    color = Color(0xFFFF9800),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            },
            {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            {
                Text(
                    text = "▶",  // Right arrow
                    color = Color(0xFF2196F3),  // Blue
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        )
    )

    val bottomLeftUnderPolarZone = UnderPolarZone(
        children = listOf(
            {
                Text(
                    text = "🔉",  // Speaker low icon
                    color = Color(0xFFFF9800),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            },
            {
                Text(
                    text = "Vol",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            {
                Text(
                    text = "↓",  // Down arrow
                    color = Color(0xFFF44336),  // Red
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        )
    )

    val bottomRightUnderPolarZone = UnderPolarZone(
        children = listOf(
            {
                Text(
                    text = "🎵",  // Music note icon
                    color = Color(0xFFFF9800),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            },
            {
                Text(
                    text = "Audio",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            {
                Text(
                    text = "🎧",  // Headphones icon
                    color = Color(0xFF9C27B0),  // Purple
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        )
    )

    // Create UI theme
    val theme = CircularLayoutTheme(
        centerButton = CenterButtonTheme(
            backgroundColor = Color(0xC91A1C1F),
            textColor = Color.White,
            textSize = 40f
        ),
        mainButtons = MainButtonTheme(
            backgroundColor = Color(0xFF37474F),
            activeBackgroundColor = Color(0xFFFF9800),
            notchColor = Color(0xFF4A4A4A),          // Dark gray for inactive
            activeNotchColor = Color(0xFFFF6B35)     // Warm red-orange for active
        ),
        iconButtons = IconButtonTheme(
            backgroundColor = Color(0xFF37474F),
            activeBackgroundColor = Color(0xFFFF6B6B)
        ),
        underPolar = UnderPolarTheme(
            backgroundColor = Color(0xAB1A1A1C)
        )
    )

    EnhancedCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        topPolarButtonGroup = topPolarButtonGroup,
        bottomPolarButtonGroup = bottomPolarButtonGroup,
        topLeftUnderPolarZone = topLeftUnderPolarZone,
        topRightUnderPolarZone = topRightUnderPolarZone,
        bottomLeftUnderPolarZone = bottomLeftUnderPolarZone,
        bottomRightUnderPolarZone = bottomRightUnderPolarZone,
        centerLabel = "TRIAL",
        onButtonClick = { button ->
            // Custom content for each button
            when (button.text) {
                "MUSIC" -> {
                    {
                        // Blue rectangular banner
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
                                text = "Music",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
                "RADIO" -> {
                    {
                        // Green square banner
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
                                text = "Radio",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
                "NAVI" -> {
                    {
                        // Red banner
                        Box(
                            modifier = Modifier
                                .size(180.dp, 120.dp)
                                .background(
                                    Color(0xFFE53935),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Navigation",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
                else -> {
                    {
                        // Default - neon text
                        NeonText(
                            text = button.text,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        },
        theme = theme,
        centerRadiusRatio = 0.5f,
        iconSegmentRadiusRatio = 1.6f,  // Icon segment radius = 160% of center circle radius
        outerRadiusRatio = 0.96f,
        buttonsPaddingRatio = 0.01f,    // 1% padding top/bottom
        textRadialLayout = true,        // Radial text layout
        circlePaddingRatio = 0.15f,     // 15% padding for center circle
        showSphere = true               // Show 3D sphere in center
    )
}

/**
 * Example using CircularButton component directly
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
            Box(modifier = Modifier.fillMaxSize()) {
                // Фонове зображення
                Image(
                    painter = painterResource(id = R.drawable.car_lesser),
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Чорний напівпрозорий шар поверх фону
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                )
                // UI поверх всього
                CarInterfaceExample()
            }
        }
    }
}
/*
@Preview(showBackground = true, widthDp = 600, heightDp = 600)
@Composable
fun DirectButtonPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DirectCircularButtonExample()
        }
    }
}*/
