# Circular Button Layout - Документація

Кастомний UI компонент для Jetpack Compose з круговим розташуванням кнопок.

## Опис

Компонент створює кругову кнопкову панель з центральною кнопкою та обтікаючими бічними кнопками. Бічні кнопки мають форму сегментів кільця:
- **Внутрішній край** - огинає центральну кнопку (дуга радіусу R1)
- **Зовнішній край** - огинає зовнішнє коло (дуга радіусу R2)

## Геометрія

### Математична модель:
```
R1 - радіус центральної кнопки (centerRadius)
R2 - радіус зовнішнього кола (outerRadius)
```

### Алгоритм побудови кожної кнопки:
1. Стартуємо з точки на внутрішньому колі під кутом `startAngle`
2. **arcTo()** - малюємо дугу по внутрішньому колу від `startAngle` до `endAngle`
3. **lineTo()** - проводимо лінію до зовнішнього кола
4. **arcTo()** - малюємо дугу по зовнішньому колю у зворотному напрямку
5. **close()** - замикаємо контур

### Розподіл кнопок:
- Кнопки автоматично розподіляються навколо центру
- Верхні кнопки: від 180° до 360°
- Нижні кнопки: від 0° до 180°
- Рівномірний кутовий крок між кнопками

## Компоненти

### 1. CircularButtonLayout (базовий)

Простий варіант без текстових міток:

```kotlin
CircularButtonLayout(
    centerRadius = 80f,
    outerRadius = 200f,
    buttonCount = 6,
    onCenterClick = {
        println("Центр!")
    },
    onButtonClick = { index ->
        println("Кнопка $index")
    }
)
```

**Параметри:**
- `centerRadius: Float` - радіус центральної кнопки (за замовчуванням 100f)
- `outerRadius: Float` - радіус зовнішнього кола (за замовчуванням 250f)
- `buttonCount: Int` - кількість бічних кнопок (за замовчуванням 3)
- `onCenterClick: () -> Unit` - callback для центральної кнопки
- `onButtonClick: (Int) -> Unit` - callback для бічних кнопок (отримує індекс)

### 2. AdvancedCircularButtonLayout (розширений)

Повнофункціональний варіант з кастомізацією:

```kotlin
val buttons = listOf(
    ButtonData("Action 1") { /* дія 1 */ },
    ButtonData("Action 2") { /* дія 2 */ },
    ButtonData("Action 3") { /* дія 3 */ }
)

AdvancedCircularButtonLayout(
    modifier = Modifier.fillMaxSize(),
    centerRadius = 100f,
    outerRadius = 250f,
    buttons = buttons,
    centerLabel = "Menu",
    onCenterClick = { /* клік по центру */ },
    centerColor = Color(0xFF4CAF50),
    buttonColor = Color(0xFF607D8B),
    selectedButtonColor = Color(0xFF2196F3)
)
```

**Параметри:**
- `modifier: Modifier` - модифікатор Compose
- `centerRadius: Float` - радіус центральної кнопки
- `outerRadius: Float` - радіус зовнішнього кола
- `buttons: List<ButtonData>` - список кнопок з мітками та діями
- `centerLabel: String` - текст на центральній кнопці
- `onCenterClick: () -> Unit` - callback для центральної кнопки
- `centerColor: Color` - колір центральної кнопки
- `buttonColor: Color` - колір бічних кнопок
- `selectedButtonColor: Color` - колір вибраної кнопки

### ButtonData

```kotlin
data class ButtonData(
    val label: String,
    val onClick: () -> Unit
)
```

## Приклади використання

### Простий приклад

```kotlin
@Composable
fun SimpleExample() {
    CircularButtonLayout(
        centerRadius = 80f,
        outerRadius = 200f,
        buttonCount = 6,
        onCenterClick = {
            Log.d("Menu", "Center clicked")
        },
        onButtonClick = { index ->
            Log.d("Menu", "Button $index clicked")
        }
    )
}
```

### Кастомізований приклад

```kotlin
@Composable
fun CustomExample() {
    val buttons = remember {
        listOf(
            ButtonData("Home") { navigateToHome() },
            ButtonData("Search") { openSearch() },
            ButtonData("Profile") { openProfile() },
            ButtonData("Settings") { openSettings() }
        )
    }

    AdvancedCircularButtonLayout(
        centerRadius = 120f,
        outerRadius = 280f,
        buttons = buttons,
        centerLabel = "⚙",
        centerColor = Color(0xFF9C27B0),
        buttonColor = Color(0xFF455A64),
        selectedButtonColor = Color(0xFFFF5722)
    )
}
```

### Динамічна зміна кількості кнопок

```kotlin
@Composable
fun DynamicExample() {
    var buttonCount by remember { mutableStateOf(4) }

    val buttons = remember(buttonCount) {
        List(buttonCount) { index ->
            ButtonData("B${index + 1}") {
                println("Button ${index + 1}")
            }
        }
    }

    AdvancedCircularButtonLayout(
        buttons = buttons,
        onCenterClick = {
            // Збільшуємо кількість кнопок при кліку
            buttonCount = if (buttonCount < 8) buttonCount + 1 else 3
        }
    )
}
```

## Touch Detection

Компонент автоматично визначає:
1. **Центральна область** - відстань від центру <= centerRadius
2. **Бічні кнопки** - відстань від centerRadius до outerRadius
3. **Кутовий сектор** - визначення конкретної кнопки за кутом

## Налаштування

### Розміри
- Для маленьких екранів: `centerRadius = 60f, outerRadius = 150f`
- Для середніх екранів: `centerRadius = 100f, outerRadius = 250f`
- Для великих екранів: `centerRadius = 150f, outerRadius = 350f`

### Кількість кнопок
- Мінімум: 2 кнопки
- Рекомендовано: 4-8 кнопок
- Максимум: необмежено (але візуально краще не більше 12)

## Технічні деталі

### Файли
- `CircularButtonLayout.kt` - базовий компонент
- `AdvancedCircularButtonLayout.kt` - розширений компонент
- `MainActivity.kt` - приклад інтеграції

### Використані технології
- Jetpack Compose Canvas API
- Path API для створення складних форм
- arcTo() для малювання дуг
- Touch gesture detection

### Підтримувані Android версії
- minSdk: згідно з налаштуваннями проекту
- targetSdk: згідно з налаштуваннями проекту

## Примітки

- Компонент є повністю responsive
- Підтримує будь-яку кількість кнопок
- Flexible параметри для кастомізації
- Оптимізований для performance
- Підтримує preview в Android Studio
