package com.radialbuttons.circularbuttonlayout.data

/**
 * Тип полярної зони
 */
enum class PolarZone {
    POLAR,        // Основна полярна зона (між centerRadius та middleRadius)
    UNDER_POLAR   // Зовнішня полярна зона (між middleRadius та outerRadius)
}

/**
 * Сторона полярної зони
 */
enum class PolarSide {
    LEFT,   // Ліва половина
    RIGHT   // Права половина
}
