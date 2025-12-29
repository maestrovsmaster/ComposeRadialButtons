# Circular Button Layout Library

Бібліотека для створення кругових інтерфейсів з радіальними кнопками для Android Jetpack Compose.

## Структура пакету

```
circularbuttonlayout/
├── 📦 data/                    # Публічні дата класи
│   ├── CircularButtonData.kt   # Конфігурація кнопки (іконка, текст, onClick)
│   ├── Side.kt                 # Enum: LEFT, RIGHT
│   ├── PolarButtonGroup.kt     # Група полярних кнопок (2 кнопки + заголовок)
│   └── Enums.kt                # PolarZone, PolarSide
│
├── 🎨 components/              # Публічні composable компоненти
│   └── CircularButton.kt       # Окрема кругова кнопка з анімаціями
│
├── 🖌️ drawing/                 # Внутрішні функції малювання
│   ├── DualSegmentButtonDrawing.kt   # Малювання dual-segment кнопок
│   ├── PolarButtonDrawing.kt         # Малювання полярних зон
│   └── UnderPolarDrawing.kt          # Малювання under-polar зон
│
├── 📐 geometry/                # Внутрішні геометричні розрахунки
│   ├── PathCreation.kt         # Створення Path для кнопок
│   └── GeometryUtils.kt        # Утиліти (кути, позиції на колі)
│
├── 👆 touch/                   # Внутрішня логіка визначення дотиків
│   └── TouchDetection.kt       # isPointInButton, isPointInPolarZone
│
├── 🛠️ utils/                   # Внутрішні утиліти
│   ├── PositionCalculations.kt # Розрахунок позицій тексту/іконок
│   └── CanvasExtensions.kt     # Extension функції для Canvas
│
├── 📋 EnhancedCircularButtonLayout.kt  # ГОЛОВНИЙ ПУБЛІЧНИЙ API
├── 📋 CircularButtonLayoutApi.kt       # Re-export всіх публічних компонентів
└── 📖 README.md                        # Ця документація
```

## Публічний API

### 1. EnhancedCircularButtonLayout
Головний composable компонент, що містить:
- Центральну кругову кнопку
- Бічні радіальні кнопки (ліворуч і праворуч)
- Полярні групи кнопок (верхня і нижня)
- Under-polar зони для додаткових віджетів

### 2. CircularButton
Окремий composable для однієї кругової кнопки:
- Dual-segment дизайн (іконка + текст)
- Анімації при натисканні
- Ripple ефект

### 3. Дата класи

#### CircularButtonData
```kotlin
data class CircularButtonData(
    val icon: String,           // Emoji або текст іконки
    val text: String,           // Текст кнопки
    val onClick: () -> Unit,    // Обробник кліку
    val iconColor: Color,       // Колір іконки
    val textColor: Color        // Колір тексту
)
```

#### PolarButtonGroup
```kotlin
data class PolarButtonGroup(
    val leftButton: CircularButtonData,   // Ліва половина
    val rightButton: CircularButtonData,  // Права половина
    val title: String?,                   // Заголовок по центру
    val subtitle: String?,                // Підзаголовок
    val titleSize: Float,                 // Розмір заголовка
    val titleColor: Color,                // Колір заголовка
    val subtitleSize: Float,              // Розмір підзаголовка
    val subtitleColor: Color,             // Колір підзаголовка
    val iconOffsetFromEdge: Float         // Відступ іконок від краю
)
```

## Приклад використання

```kotlin
import android.test.testuiapplication.circularbuttonlayout.EnhancedCircularButtonLayout
import android.test.testuiapplication.circularbuttonlayout.data.*

@Composable
fun MyCircularInterface() {
    val leftButtons = listOf(
        CircularButtonData(
            icon = "📻",
            text = "RADIO",
            onClick = { /* ... */ }
        ),
        // ...
    )

    val topPolarGroup = PolarButtonGroup(
        leftButton = CircularButtonData(icon = "-", text = "", onClick = {}),
        rightButton = CircularButtonData(icon = "+", text = "", onClick = {}),
        title = "Volume",
        iconOffsetFromEdge = 0.12f
    )

    EnhancedCircularButtonLayout(
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        topPolarButtonGroup = topPolarGroup,
        centerLabel = "MENU",
        centerColor = Color(0xFF4CAF50)
    )
}
```

## Підготовка до gradle library

Вся внутрішня логіка (drawing, geometry, touch, utils) є приватною і не експортується.
Публічний API чітко визначений через:
- `EnhancedCircularButtonLayout` - головний компонент
- `CircularButton` - окрема кнопка
- Дата класи в `data/`

Для створення gradle library:
1. Перемістити `circularbuttonlayout/` в окремий модуль
2. Налаштувати `build.gradle` для публікації
3. Всі внутрішні класи позначити як `internal`
4. Експортувати тільки публічний API
