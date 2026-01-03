# Enhanced Circular Button Layout

Universal UI module for Android Jetpack Compose with circular button layout and 3D sphere in center.

## Features

- ✅ Circular button layout with radial segments (icon + text)
- ✅ 3D sphere in center with OpenGL (optional)
- ✅ Polar zones at top/bottom for additional buttons
- ✅ 4 Under-polar zones for custom Compose content
- ✅ Radio groups for mutually exclusive buttons
- ✅ Custom theme support
- ✅ Animations and ripple effects
- ✅ Callback for custom content on button click

## Quick Start

### 1. Basic Usage

```kotlin
EnhancedCircularButtonLayout(
    modifier = Modifier.fillMaxSize(),
    leftButtons = listOf(
        CircularButtonData(
            icon = "📻",
            text = "RADIO",
            onClick = { /* action */ }
        )
    ),
    rightButtons = listOf(
        CircularButtonData(
            icon = "📞",
            text = "PHONE",
            onClick = { /* action */ }
        )
    ),
    centerLabel = "MENU"
)
```

### 2. Adding Polar Buttons

```kotlin
val topPolarButtonGroup = PolarButtonGroup(
    leftButton = CircularButtonData(
        icon = "-",
        text = "",
        onClick = { /* minus action */ }
    ),
    rightButton = CircularButtonData(
        icon = "+",
        text = "",
        onClick = { /* plus action */ }
    ),
    title = "Volume",
    subtitle = "Level 5"
)

EnhancedCircularButtonLayout(
    // ... other parameters
    topPolarButtonGroup = topPolarButtonGroup,
    bottomPolarButtonGroup = bottomPolarButtonGroup
)
```

### 3. Under-polar Zones with Custom Content

```kotlin
val topLeftUnderPolarZone = UnderPolarZone(
    children = listOf(
        {
            Text(text = "Vol", color = Color.White, fontSize = 14.sp)
        },
        {
            Text(text = "↑", color = Color(0xFFFF9800), fontSize = 24.sp)
        }
    )
)

EnhancedCircularButtonLayout(
    // ... other parameters
    topLeftUnderPolarZone = topLeftUnderPolarZone,
    topRightUnderPolarZone = topRightUnderPolarZone,
    bottomLeftUnderPolarZone = bottomLeftUnderPolarZone,
    bottomRightUnderPolarZone = bottomRightUnderPolarZone
)
```

### 4. Custom Content on Button Click

```kotlin
EnhancedCircularButtonLayout(
    // ... other parameters
    onButtonClick = { button ->
        when (button.text) {
            "MUSIC" -> {
                {
                    Box(
                        modifier = Modifier
                            .size(200.dp, 100.dp)
                            .background(Color(0xFF1E88E5), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Music", color = Color.White)
                    }
                }
            }
            else -> {
                { NeonText(text = button.text, color = Color(0xFFFFD700)) }
            }
        }
    }
)
```

### 5. Separate onClick for Icon

```kotlin
CircularButtonData(
    icon = "🔊",
    text = "AUDIO",
    onClick = { /* click on entire button or text segment */ },
    onIconClick = { /* click only on icon segment */ }  // optional - if null, onClick is used
)
```

**How it works:**
- When user clicks on the **icon segment** (inner radius to middle radius) and `onIconClick` is provided, it will be called
- When user clicks on the **text segment** (middle radius to outer radius) or icon segment without `onIconClick`, `onClick` will be called
- This allows different actions for quick icon clicks vs. full button selection

### 6. Radio Groups (Mutually Exclusive Buttons)

```kotlin
val leftButtons = listOf(
    CircularButtonData(
        icon = "📻",
        text = "RADIO",
        onClick = { /* action */ },
        radioGroupId = "media"  // both buttons in same group
    ),
    CircularButtonData(
        icon = "♫",
        text = "MUSIC",
        onClick = { /* action */ },
        radioGroupId = "media"
    )
)
```

### 7. Custom Theme

```kotlin
val theme = CircularLayoutTheme(
    centerButton = CenterButtonTheme(
        backgroundColor = Color(0xC91A1C1F),
        textColor = Color.White,
        textSize = 40f
    ),
    mainButtons = MainButtonTheme(
        backgroundColor = Color(0xFF37474F),
        activeBackgroundColor = Color(0xFFFF9800),
        notchColor = Color(0xFF4A4A4A),
        activeNotchColor = Color(0xFFFF6B35)
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
    // ... other parameters
    theme = theme
)
```

### 8. 3D Sphere in Center

```kotlin
EnhancedCircularButtonLayout(
    // ... other parameters
    showSphere = true,              // Show 3D sphere
    centerRadiusRatio = 0.5f,       // Sphere size
    circlePaddingRatio = 0.15f      // Padding from edges
)
```

### 9. Haptic Feedback

```kotlin
EnhancedCircularButtonLayout(
    // ... other parameters
    enableHapticFeedback = true     // Enable vibration feedback on button press (default: true)
)
```

When `enableHapticFeedback` is enabled, all button presses (main buttons, polar buttons, center button) will trigger haptic feedback vibration for better user experience.

## Parameters

| Parameter | Type | Default | Description |
|----------|-----|------------------|------|
| `leftButtons` | `List<CircularButtonData>` | - | Buttons on the left |
| `rightButtons` | `List<CircularButtonData>` | - | Buttons on the right |
| `topPolarButtonGroup` | `PolarButtonGroup?` | `null` | Top polar group |
| `bottomPolarButtonGroup` | `PolarButtonGroup?` | `null` | Bottom polar group |
| `topLeftUnderPolarZone` | `UnderPolarZone?` | `null` | Top left under-polar zone |
| `topRightUnderPolarZone` | `UnderPolarZone?` | `null` | Top right under-polar zone |
| `bottomLeftUnderPolarZone` | `UnderPolarZone?` | `null` | Bottom left under-polar zone |
| `bottomRightUnderPolarZone` | `UnderPolarZone?` | `null` | Bottom right under-polar zone |
| `centerLabel` | `String` | `"Menu"` | Text in center |
| `onCenterClick` | `() -> Unit` | `{}` | Callback on center click |
| `onButtonClick` | `(CircularButtonData) -> @Composable () -> Unit?` | `null` | Callback for custom content |
| `theme` | `CircularLayoutTheme` | `default()` | UI theme |
| `centerRadiusRatio` | `Float` | `0.5f` | Center circle size (0.0-1.0) |
| `iconSegmentRadiusRatio` | `Float` | `1.2f` | Icon segment size relative to center |
| `outerRadiusRatio` | `Float` | `1.0f` | Outer radius of buttons |
| `buttonsPaddingRatio` | `Float` | `0.0f` | Padding top/bottom (0.0-0.5) |
| `textRadialLayout` | `Boolean` | `false` | Radial text layout |
| `circlePaddingRatio` | `Float` | `0.0f` | Padding for center circle |
| `elementsPadding` | `Float` | `8f` | Padding between elements |
| `showSphere` | `Boolean` | `false` | Show 3D sphere in center |
| `enableHapticFeedback` | `Boolean` | `true` | Enable haptic feedback on button press |

## Data Structures

### CircularButtonData
```kotlin
data class CircularButtonData(
    val icon: String,                    // Icon (emoji or unicode)
    val text: String,                    // Text
    val onClick: () -> Unit,             // Callback on click
    val onIconClick: (() -> Unit)? = null,  // Separate click on icon
    val iconColor: Color = Color(0xFFFF9800),
    val textColor: Color = Color.White,
    val radioGroupId: String? = null     // Radio group ID
)
```

### UnderPolarZone
```kotlin
data class UnderPolarZone(
    val children: List<@Composable () -> Unit> = emptyList()
)
```

## Example

See full usage example in `CarInterfaceExample.kt`.

## Project Structure

```
circularbuttonlayout/
├── EnhancedCircularButtonLayout.kt    # Main component
├── components/
│   └── CircularButton.kt              # Individual button component
├── data/
│   ├── CircularButtonData.kt          # Button data model
│   ├── CircularLayoutTheme.kt         # Theme configuration
│   ├── PolarButtonGroup.kt            # Polar button group
│   ├── UnderPolarZone.kt             # Under-polar zone
│   └── ...
├── drawing/
│   ├── DualSegmentButtonDrawing.kt   # Button drawing
│   ├── PolarButtonDrawing.kt         # Polar button drawing
│   └── UnderPolarDrawing.kt          # Under-polar drawing
├── touch/
│   └── TouchDetection.kt             # Touch handling
└── utils/
    └── ...                           # Utility functions
```

## License

Ready for publication on GitHub as a separate module.
