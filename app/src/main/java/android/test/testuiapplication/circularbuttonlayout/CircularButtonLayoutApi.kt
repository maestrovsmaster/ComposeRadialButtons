package android.test.testuiapplication.circularbuttonlayout

/**
 * Публічний API для Circular Button Layout Library
 *
 * Цей файл експортує всі публічні компоненти бібліотеки для зручного використання.
 * Коли це буде окремою gradle library, користувачі зможуть імпортувати все з одного місця.
 *
 * Основні компоненти:
 *
 * 1. EnhancedCircularButtonLayout - головний layout компонент
 *    - Центральна кругова кнопка
 *    - Бічні радіальні кнопки (ліворуч і праворуч)
 *    - Полярні групи кнопок (вгорі і внизу)
 *    - Under-polar зони (зовнішні області)
 *
 * 2. CircularButton - окремий composable для кругової кнопки з анімаціями
 *
 * 3. Дата класи:
 *    - CircularButtonData - конфігурація однієї кнопки
 *    - PolarButtonGroup - конфігурація полярної групи (дві кнопки + заголовок)
 *    - Side - перелічення сторін (LEFT, RIGHT)
 *    - PolarZone - тип полярної зони (POLAR, UNDER_POLAR)
 *    - PolarSide - сторона в полярній зоні (LEFT, RIGHT)
 *
 * Приклад використання:
 * ```kotlin
 * EnhancedCircularButtonLayout(
 *     leftButtons = listOf(...),
 *     rightButtons = listOf(...),
 *     topPolarButtonGroup = PolarButtonGroup(...),
 *     bottomPolarButtonGroup = PolarButtonGroup(...),
 *     centerLabel = "MENU"
 * )
 * ```
 */

// Re-export публічних компонентів для зручного імпорту

// Main components
// Головний layout вже в цьому пакеті: EnhancedCircularButtonLayout

// Data classes
typealias CircularButtonData = android.test.testuiapplication.circularbuttonlayout.data.CircularButtonData
typealias PolarButtonGroup = android.test.testuiapplication.circularbuttonlayout.data.PolarButtonGroup
typealias Side = android.test.testuiapplication.circularbuttonlayout.data.Side
typealias PolarZone = android.test.testuiapplication.circularbuttonlayout.data.PolarZone
typealias PolarSide = android.test.testuiapplication.circularbuttonlayout.data.PolarSide

// Note: CircularButton is a @Composable function in components.CircularButton
// Import it directly: import android.test.testuiapplication.circularbuttonlayout.components.CircularButton
