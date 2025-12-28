# Horizontal Circular Button Layout - Документація

Кастомний UI компонент для Jetpack Compose з горизонтальним розташуванням кнопок.

## Опис

Компонент створює круговий layout для **горизонтальної орієнтації екрану** з центральною кнопкою та обтікаючими бічними кнопками ліворуч і праворуч.

### Ключові особливості геометрії:

✅ **Горизонтальна орієнтація** - екран у landscape режимі
✅ **Горизонтальні краї** - верхній і нижній краї кнопок паралельні осі X
✅ **Дугоподібні бічні сторони** - огинають внутрішнє (R1) та зовнішнє (R2) кола
✅ **Висота групи кнопок** - H = 2 * R1 (діаметр внутрішнього кола)

## Геометрія

```
┌─────────────────────────────────────────┐
│                                         │
│  ╭──╮  ╭──╮  ╭──╮       ╭──╮  ╭──╮  ╭──╮ │
│  │L3│  │L2│  │L1│  ⭕  │R1│  │R2│  │R3│ │
│  ╰──╯  ╰──╯  ╰──╯       ╰──╯  ╰──╯  ╰──╯ │
│                                         │
└─────────────────────────────────────────┘
     └──────┬──────┘         └──────┬──────┘
      Ліві кнопки           Праві кнопки
```

### Математична модель:

**Кола:**
- R1 - радіус центральної кнопки (centerRadius)
- R2 - радіус зовнішнього кола (outerRadius)
- Центр: (centerX, centerY)

**Кожна кнопка:**
- **Верхній край**: горизонтальна лінія Y = Y_top
- **Нижній край**: горизонтальна лінія Y = Y_bottom
- **Внутрішня сторона** (біля центру): дуга кола радіусу R1
- **Зовнішня сторона**: дуга кола радіусу R2

**Висоти:**
- H = 2 * R1 (загальна висота групи кнопок)
- h = H / N (висота однієї кнопки, де N - кількість кнопок)

### Обчислення X-координат на колі:

Рівняння кола: `(x - centerX)² + (y - centerY)² = R²`

При заданому Y:
```
x = centerX ± sqrt(R² - (y - centerY)²)
```

Для **лівих кнопок**: `x = centerX - sqrt(...)`
Для **правих кнопок**: `x = centerX + sqrt(...)`

## Алгоритм побудови Path

### Ліва кнопка:

```kotlin
1. Стартуємо з верхньої правої точки (на внутрішньому колі R1)
2. arcTo() - малюємо дугу по внутрішньому колю ВНИЗ
3. lineTo() - горизонтальна лінія ВЛІВО до зовнішнього кола
4. arcTo() - малюємо дугу по зовнішньому колю ВГОРУ
5. close() - замикаємо контур (автоматична лінія вправо)
```

### Права кнопка:

```kotlin
1. Стартуємо з верхньої лівої точки (на внутрішньому колі R1)
2. arcTo() - малюємо дугу по внутрішньому колю ВНИЗ
3. lineTo() - горизонтальна лінія ВПРАВО до зовнішнього кола
4. arcTo() - малюємо дугу по зовнішньому колю ВГОРУ
5. close() - замикаємо контур (автоматична лінія вліво)
```

## Використання

### Базовий приклад:

```kotlin
@Composable
fun MyScreen() {
    val leftButtons = listOf(
        ButtonData("Action 1") { /* дія 1 */ },
        ButtonData("Action 2") { /* дія 2 */ },
        ButtonData("Action 3") { /* дія 3 */ }
    )

    val rightButtons = listOf(
        ButtonData("Option 1") { /* опція 1 */ },
        ButtonData("Option 2") { /* опція 2 */ },
        ButtonData("Option 3") { /* опція 3 */ }
    )

    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        centerRadius = 100f,
        outerRadius = 250f,
        leftButtons = leftButtons,
        rightButtons = rightButtons,
        centerLabel = "Menu",
        onCenterClick = { /* клік по центру */ }
    )
}
```

### Повний приклад з кастомізацією:

```kotlin
@Composable
fun CustomExample() {
    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        centerRadius = 120f,
        outerRadius = 280f,
        leftButtons = listOf(
            ButtonData("🏠") { navigateHome() },
            ButtonData("🔍") { openSearch() },
            ButtonData("👤") { openProfile() }
        ),
        rightButtons = listOf(
            ButtonData("⚙") { openSettings() },
            ButtonData("📊") { openStats() },
            ButtonData("❤") { openFavorites() }
        ),
        centerLabel = "⭐",
        onCenterClick = { toggleMenu() },
        centerColor = Color(0xFF9C27B0),
        buttonColor = Color(0xFF455A64),
        selectedButtonColor = Color(0xFFFF5722)
    )
}
```

### Асиметричні кнопки:

```kotlin
// Різна кількість кнопок зліва та справа
HorizontalCircularButtonLayout(
    leftButtons = listOf(
        ButtonData("L1") { },
        ButtonData("L2") { }
    ),
    rightButtons = listOf(
        ButtonData("R1") { },
        ButtonData("R2") { },
        ButtonData("R3") { },
        ButtonData("R4") { }
    ),
    // ... інші параметри
)
```

## Параметри

### Обов'язкові:
- `leftButtons: List<ButtonData>` - кнопки ліворуч від центру
- `rightButtons: List<ButtonData>` - кнопки праворуч від центру

### Опціональні:
- `modifier: Modifier` - модифікатор Compose (за замовчуванням: Modifier)
- `centerRadius: Float` - радіус центральної кнопки (за замовчуванням: 100f)
- `outerRadius: Float` - радіус зовнішнього кола (за замовчуванням: 250f)
- `centerLabel: String` - текст на центральній кнопці (за замовчуванням: "Центр")
- `onCenterClick: () -> Unit` - callback для центральної кнопки
- `centerColor: Color` - колір центральної кнопки
- `buttonColor: Color` - колір бічних кнопок
- `selectedButtonColor: Color` - колір вибраної кнопки

### ButtonData:

```kotlin
data class ButtonData(
    val label: String,      // Текст на кнопці
    val onClick: () -> Unit // Callback при кліку
)
```

## Touch Detection

Компонент автоматично визначає:

1. **Центральна кнопка** - відстань від центру <= centerRadius
2. **Ліві кнопки**:
   - X < centerX (ліворуч від центру)
   - centerRadius <= відстань <= outerRadius
   - Y в межах кнопки
3. **Праві кнопки**:
   - X > centerX (праворуч від центру)
   - centerRadius <= відстань <= outerRadius
   - Y в межах кнопки

## Рекомендації по розмірам

### Для різних екранів:

**Смартфони (landscape):**
```kotlin
centerRadius = 60f
outerRadius = 150f
```

**Планшети (landscape):**
```kotlin
centerRadius = 100f
outerRadius = 250f
```

**Великі планшети:**
```kotlin
centerRadius = 150f
outerRadius = 350f
```

### Кількість кнопок:

- **Мінімум**: 1 кнопка з кожного боку
- **Рекомендовано**: 2-4 кнопки з кожного боку
- **Максимум**: необмежено (але візуально краще не більше 6-8)

## Відмінності від радіального layout

| Особливість | Horizontal Layout | Радіальний Layout |
|-------------|------------------|-------------------|
| Орієнтація | Горизонтальна | Будь-яка |
| Верхній/нижній краї | Горизонтальні лінії | Дуги |
| Бічні сторони | Дуги кіл | Прямі лінії (радіуси) |
| Висота групи | H = 2*R1 | Повне коло (360°) |
| Розташування | Ліворуч/праворуч | Навколо центру |

## Технічні деталі

### Файли:
- `HorizontalCircularButtonLayout.kt` - основний компонент
- `MainActivity.kt` - приклад інтеграції

### Використані API:
- Jetpack Compose Canvas
- Path API з arcTo() для дуг
- Touch gesture detection
- Native Canvas для текстових міток

### Підтримка:
- Android API 21+
- Jetpack Compose BOM версії вашого проекту

## Приклади використання

### Медіа-плеєр:

```kotlin
HorizontalCircularButtonLayout(
    leftButtons = listOf(
        ButtonData("⏮") { previousTrack() },
        ButtonData("⏪") { rewind() },
        ButtonData("⏸") { pause() }
    ),
    rightButtons = listOf(
        ButtonData("▶") { play() },
        ButtonData("⏩") { fastForward() },
        ButtonData("⏭") { nextTrack() }
    ),
    centerLabel = "🎵",
    centerColor = Color(0xFFE91E63)
)
```

### Навігація в грі:

```kotlin
HorizontalCircularButtonLayout(
    leftButtons = listOf(
        ButtonData("↖") { moveNW() },
        ButtonData("←") { moveW() },
        ButtonData("↙") { moveSW() }
    ),
    rightButtons = listOf(
        ButtonData("↗") { moveNE() },
        ButtonData("→") { moveE() },
        ButtonData("↘") { moveSE() }
    ),
    centerLabel = "⚔",
    centerColor = Color(0xFF4CAF50)
)
```

## Налаштування під Android

### AndroidManifest.xml:

Для фіксованої горизонтальної орієнтації:

```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="landscape">
</activity>
```

## Debug

Логування кліків:

```kotlin
leftButtons = listOf(
    ButtonData("L1") { Log.d("Menu", "Left 1 clicked") },
    ButtonData("L2") { Log.d("Menu", "Left 2 clicked") }
)
```

Перегляд у Logcat з фільтром `CircularMenu` або вашим тегом.

## Підсумок

✅ Горизонтальний layout для landscape орієнтації
✅ Кнопки з горизонтальними верхом/низом та дугоподібними боками
✅ Flexible кількість кнопок зліва та справа
✅ Повна кастомізація кольорів та розмірів
✅ Touch detection для всіх кнопок
✅ Текстові мітки на кнопках
