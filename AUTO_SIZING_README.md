# Auto-Sizing Horizontal Circular Button Layout

## Що нового

✅ **Автоматичне вписування в батьківський контейнер**
- Компонент автоматично розраховує радіуси на основі розміру контейнера
- Більше не потрібно задавати фіксовані centerRadius та outerRadius

✅ **Виправлено touch detection**
- Кліки тепер працюють правильно для лівих та правих кнопок
- Точна перевірка меж кожної кнопки з урахуванням дугоподібної форми

## Як працює автоматичне розмірювання

### Концепція:

```
Висота контейнера (менша сторона в landscape) визначає радіуси:
- R1 = containerHeight * centerRadiusRatio
- R2 = containerHeight * outerRadiusRatio
```

### Параметри:

```kotlin
centerRadiusRatio: Float = 0.5f  // За замовчуванням 50% від висоти
outerRadiusRatio: Float = 1.0f   // За замовчуванням 100% від висоти
```

### Приклади:

**Стандартний (за замовчуванням):**
```kotlin
HorizontalCircularButtonLayout(
    leftButtons = leftButtons,
    rightButtons = rightButtons,
    // centerRadiusRatio = 0.5f (50% висоти контейнера = R1)
    // outerRadiusRatio = 1.0f (100% висоти контейнера = R2)
)
```

Якщо контейнер має висоту 400px:
- R1 = 400 * 0.5 = 200px (діаметр внутрішнього кола = висоті контейнера)
- R2 = 400 * 1.0 = 400px (бічні кнопки займають всю висоту)

**Компактний layout:**
```kotlin
HorizontalCircularButtonLayout(
    leftButtons = leftButtons,
    rightButtons = rightButtons,
    centerRadiusRatio = 0.4f,  // 40% висоти
    outerRadiusRatio = 0.8f    // 80% висоти
)
```

**Розширений layout:**
```kotlin
HorizontalCircularButtonLayout(
    leftButtons = leftButtons,
    rightButtons = rightButtons,
    centerRadiusRatio = 0.5f,   // 50% висоти
    outerRadiusRatio = 1.2f     // 120% висоти (виходить за межі)
)
```

## Виправлення touch detection

### Що було виправлено:

1. **Точна перевірка меж кнопки** - для кожної точки кліка обчислюються X-координати на внутрішньому та зовнішньому колах
2. **Правильна логіка для лівих/правих кнопок**:
   - Ліва кнопка: `xOuter <= point.x <= xInner`
   - Права кнопка: `xInner <= point.x <= xOuter`

### Код:

```kotlin
private fun isPointInButton(
    point: Offset,
    buttonIndex: Int,
    totalButtons: Int,
    centerX: Float,
    centerY: Float,
    centerRadius: Float,
    outerRadius: Float,
    side: Side
): Boolean {
    val totalHeight = 2 * centerRadius
    val buttonHeight = totalHeight / totalButtons

    val yTop = centerY - centerRadius + buttonIndex * buttonHeight
    val yBottom = yTop + buttonHeight

    // Перевірка Y-координати
    if (point.y < yTop || point.y > yBottom) return false

    // Обчислюємо X-координати меж кнопки на даному Y
    val xInner = getXOnCircle(centerX, centerY, centerRadius, point.y, side)
    val xOuter = getXOnCircle(centerX, centerY, outerRadius, point.y, side)

    // Перевірка для лівої/правої сторони
    return when (side) {
        Side.LEFT -> point.x >= xOuter && point.x <= xInner
        Side.RIGHT -> point.x >= xInner && point.x <= xOuter
    }
}
```

## Використання

### Базовий приклад (автоматичний розмір):

```kotlin
@Composable
fun MyScreen() {
    HorizontalCircularButtonLayout(
        modifier = Modifier.fillMaxSize(),
        leftButtons = listOf(
            ButtonData("L1") { },
            ButtonData("L2") { },
            ButtonData("L3") { }
        ),
        rightButtons = listOf(
            ButtonData("R1") { },
            ButtonData("R2") { },
            ButtonData("R3") { }
        ),
        centerLabel = "Menu"
        // Радіуси розрахуються автоматично!
    )
}
```

### З кастомними пропорціями:

```kotlin
HorizontalCircularButtonLayout(
    modifier = Modifier.fillMaxSize(),
    leftButtons = leftButtons,
    rightButtons = rightButtons,
    centerRadiusRatio = 0.45f,  // Трохи менше центр
    outerRadiusRatio = 0.95f    // Трохи менші бічні кнопки
)
```

### В контейнері з певним розміром:

```kotlin
Box(
    modifier = Modifier
        .width(800.dp)
        .height(400.dp)
) {
    HorizontalCircularButtonLayout(
        leftButtons = leftButtons,
        rightButtons = rightButtons
        // Автоматично підлаштується під 400dp висоти
    )
}
```

## Переваги нового підходу

### 1. Адаптивність

**Старий підхід:**
```kotlin
// Фіксовані розміри - не підходять для різних екранів
centerRadius = 100f
outerRadius = 250f
```

**Новий підхід:**
```kotlin
// Відносні розміри - працюють на будь-якому екрані
centerRadiusRatio = 0.5f
outerRadiusRatio = 1.0f
```

### 2. Простота

**Старий підхід:**
```kotlin
// Потрібно вручну розраховувати для кожного екрану
val screenHeight = ...
val centerRadius = screenHeight / 2
val outerRadius = screenHeight
```

**Новий підхід:**
```kotlin
// Все розраховується автоматично
HorizontalCircularButtonLayout(
    leftButtons = leftButtons,
    rightButtons = rightButtons
)
```

### 3. Консистентність

Незалежно від розміру екрану, пропорції залишаються однаковими:
- Центральна кнопка завжди займає 50% висоти (за замовчуванням)
- Бічні кнопки завжди займають 100% висоти (за замовчуванням)

## Міграція зі старого коду

**Було:**
```kotlin
HorizontalCircularButtonLayout(
    centerRadius = 100f,
    outerRadius = 250f,
    ...
)
```

**Стало:**
```kotlin
HorizontalCircularButtonLayout(
    // centerRadius та outerRadius більше не існують
    centerRadiusRatio = 0.5f,   // За замовчуванням
    outerRadiusRatio = 1.0f,    // За замовчуванням
    ...
)
```

## Налаштування під різні сценарії

### Медіа-плеєр (компактний):

```kotlin
HorizontalCircularButtonLayout(
    leftButtons = playbackButtons,
    rightButtons = controlButtons,
    centerRadiusRatio = 0.4f,
    outerRadiusRatio = 0.85f
)
```

### Навігаційне меню (широке):

```kotlin
HorizontalCircularButtonLayout(
    leftButtons = navigationLeft,
    rightButtons = navigationRight,
    centerRadiusRatio = 0.5f,
    outerRadiusRatio = 1.1f
)
```

### Повноекранний режим:

```kotlin
HorizontalCircularButtonLayout(
    modifier = Modifier.fillMaxSize(),
    leftButtons = leftButtons,
    rightButtons = rightButtons,
    centerRadiusRatio = 0.5f,
    outerRadiusRatio = 1.0f
)
```

## Технічні деталі

### BoxWithConstraints

Компонент використовує `BoxWithConstraints` для отримання розмірів контейнера:

```kotlin
BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val density = LocalDensity.current
    val containerHeight = with(density) { maxHeight.toPx() }

    val centerRadius = containerHeight * centerRadiusRatio
    val outerRadius = containerHeight * outerRadiusRatio

    // Малювання з розрахованими радіусами
}
```

### Touch Detection

Виправлена логіка враховує дугоподібну форму кнопок:

1. Перевірка Y-координати (в межах кнопки по вертикалі)
2. Обчислення X-координат на внутрішньому та зовнішньому колах для даного Y
3. Перевірка X-координати точки кліка між цими межами

## Тестування

Запустіть примі з файлу `Examples.kt`:
- Example1: Базовий (0.5 / 1.0)
- Example2: Асиметричний (0.4 / 0.8)
- Example3: Медіа-плеєр (0.5 / 1.0)
- Example4: Навігація (0.5 / 1.1)
- Example5: Компактний (0.4 / 0.8)
- Example6: Великий (0.5 / 1.2)
- Example7: Динамічний (0.5 / 1.0)

Всі приклади автоматично адаптуються під розмір preview!

## Підсумок

✅ Автоматичне вписування в батьківський контейнер
✅ Виправлено touch detection для лівих/правих кнопок
✅ Відносні розміри замість абсолютних
✅ Простіше використання
✅ Кращі адаптивність під різні екрани
✅ Збережена геометрія (горизонтальні краї + дугоподібні боки)
