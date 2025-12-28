# Enhanced Circular Button Layout - Нові можливості

## 🎨 Що нового

### 1. CircularButton - Окремий компонент кнопки

Кожна кнопка тепер складається з **двох радіальних сегментів**:

```
┌────────────────────────┐
│   Зовнішній сегмент    │ ← Текст ("RADIO", "NAVI", etc.)
├────────────────────────┤
│   Внутрішній сегмент   │ ← Іконка (📻, ▲, ♫, etc.)
└────────────────────────┘
```

#### Геометрія:
- **innerRadius** → **middleRadius**: внутрішній сегмент з іконкою
- **middleRadius** → **outerRadius**: зовнішній сегмент з текстом

### 2. Click Animations ✨

**Spring animation** при натисканні:
- Масштаб: 1.0 → 0.95 (пружна анімація)
- DampingRatio: Medium Bouncy
- Stiffness: Low

```kotlin
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
)
```

### 3. Ripple Effects 🌊

**Ripple effect** з білим кольором при кліку:
- Стартує з точки дотику
- Розширюється від 0 до максимального радіуса
- Плавно зникає (alpha: 1.0 → 0.0)
- Тривалість: 600ms

```kotlin
val rippleAlpha by animateFloatAsState(
    targetValue = if (rippleCenter != null) 0f else 1f,
    animationSpec = tween(durationMillis = 600),
    finishedListener = {
        rippleCenter = null
        rippleRadius = 0f
    }
)
```

## 📦 Нові компоненти

### CircularButtonData

Модель даних для однієї кнопки:

```kotlin
data class CircularButtonData(
    val icon: String,           // Іконка (emoji або Unicode)
    val text: String,           // Текст кнопки
    val onClick: () -> Unit,    // Callback
    val iconColor: Color = Color(0xFFFF9800),  // Помаранчевий
    val textColor: Color = Color.White
)
```

### CircularButton

Візуальний компонент однієї кнопки:

```kotlin
@Composable
fun CircularButton(
    modifier: Modifier = Modifier,
    buttonData: CircularButtonData,
    buttonIndex: Int,
    totalButtons: Int,
    centerX: Float,
    centerY: Float,
    innerRadius: Float,      // Внутрішній радіус (біля центру)
    middleRadius: Float,     // Радіус між іконкою та текстом
    outerRadius: Float,      // Зовнішній радіус
    side: Side,
    isSelected: Boolean = false,
    buttonColor: Color = Color(0xFF455A64),
    selectedButtonColor: Color = Color(0xFFFF9800),
    iconSegmentColor: Color = Color(0xFF2C3E50)
)
```

## 🚗 Car Interface Example

Приклад інтерфейсу автомобіля (як на скріншоті):

```kotlin
@Composable
fun CarInterfaceExample() {
    val leftButtons = listOf(
        CircularButtonData(
            icon = "📻",  // Radio
            text = "RADIO",
            onClick = { /* ... */ }
        ),
        CircularButtonData(
            icon = "▲",   // Navigation
            text = "NAVI",
            onClick = { /* ... */ }
        ),
        CircularButtonData(
            icon = "♫",   // Music
            text = "MUSIC",
            onClick = { /* ... */ }
        )
    )

    val rightButtons = listOf(
        CircularButtonData(
            icon = "📞",  // Phone
            text = "PHONE",
            onClick = { /* ... */ }
        ),
        CircularButtonData(
            icon = "🌐",  // Internet
            text = "INTERNET",
            onClick = { /* ... */ }
        ),
        CircularButtonData(
            icon = "⋮⋮⋮", // Apps
            text = "APPS",
            onClick = { /* ... */ }
        )
    )
}
```

## 🎯 Використання

### Базовий приклад з іконками:

```kotlin
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
```

### Використання CircularButton напряму:

```kotlin
CircularButton(
    buttonData = CircularButtonData(
        icon = "📻",
        text = "RADIO",
        onClick = { }
    ),
    buttonIndex = 0,
    totalButtons = 3,
    centerX = 500f,
    centerY = 500f,
    innerRadius = 200f,
    middleRadius = 250f,
    outerRadius = 300f,
    side = Side.LEFT
)
```

## 🎨 Налаштування кольорів

### Car theme (як на скріні):

```kotlin
centerColor = Color(0xFF2C3E50)           // Темно-синій центр
buttonColor = Color(0xFF37474F)           // Темно-сірі кнопки
selectedButtonColor = Color(0xFFFF9800)   // Помаранчевий для вибраної
iconSegmentColor = Color(0xFF2C3E50)      // Темний для сегмента іконки
iconColor = Color(0xFFFF9800)             // Помаранчева іконка
textColor = Color.White                   // Білий текст
```

### Різні іконки:

#### Emoji:
- 📻 Radio
- 📞 Phone
- 🌐 Globe/Internet
- ♫ Music note
- ▲ Navigation triangle
- ⋮⋮⋮ Apps grid

#### Unicode symbols:
- ☰ Menu
- ⚙ Settings
- ⭐ Star/Favorites
- ◀ ▶ Arrows
- + - Plus/Minus
- ✓ ✗ Check/Cross

## 📐 Радіуси

### Рекомендовані співвідношення:

```kotlin
centerRadiusRatio = 0.5f    // 50% висоти = діаметр центру
middleRadiusRatio = 0.55f   // 55% висоти = межа між іконкою і текстом
outerRadiusRatio = 0.65f    // 65% висоти = зовнішній край
```

### Для різних екранів:

**Компактний** (маленький екран):
```kotlin
centerRadiusRatio = 0.4f
middleRadiusRatio = 0.45f
outerRadiusRatio = 0.55f
```

**Стандартний** (середній екран):
```kotlin
centerRadiusRatio = 0.5f
middleRadiusRatio = 0.55f
outerRadiusRatio = 0.65f
```

**Розширений** (великий екран):
```kotlin
centerRadiusRatio = 0.5f
middleRadiusRatio = 0.6f
outerRadiusRatio = 0.75f
```

## 🔧 Технічні деталі

### Анімації:

1. **Scale animation**: Spring-based (пружна)
2. **Ripple effect**: Alpha fade з tween
3. **Auto-cleanup**: Ripple автоматично очищається після завершення

### Малювання:

1. **drawButtonSegment()**: Малює один радіальний сегмент
2. **createSegmentPath()**: Створює Path для сегмента
3. **drawCenteredText()**: Малює текст з обводкою (stroke + fill)

### Touch detection:

- onPress → запускає анімацію + ripple
- tryAwaitRelease → чекає відпускання
- onTap → викликає onClick callback

## 📁 Файли

Нові файли:
- `CircularButton.kt` - Компонент кнопки з анімаціями
- `EnhancedCircularButtonLayout.kt` - Розширений layout
- `CarInterfaceExample.kt` - Приклади використання

## 🎯 Переваги нового підходу

✅ **Модульність**: Кнопка - окремий компонент
✅ **Анімації**: Spring + Ripple для UX
✅ **Flexibility**: Іконка + текст в окремих сегментах
✅ **Кастомізація**: Повний контроль над кольорами
✅ **Reusability**: Можна використовувати CircularButton окремо

## 🚀 Наступні кроки

Можливі покращення:
- [ ] Градієнти для кнопок
- [ ] Тіні та глибина (elevation)
- [ ] Кастомні іконки через ImageVector
- [ ] Vibration feedback
- [ ] Sound effects
- [ ] Різні типи анімацій (rotate, slide, etc.)
- [ ] Accessibility (TalkBack support)

## 📚 Приклади в коді

Дивіться:
- `CarInterfaceExample.kt` - повний приклад car interface
- `DirectCircularButtonExample()` - приклад використання CircularButton

Обидва мають @Preview для перегляду в Android Studio!
