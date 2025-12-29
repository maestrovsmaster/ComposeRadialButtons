package android.test.testuiapplication.circularbuttonlayout.data

import androidx.compose.ui.graphics.Color

/**
 * Група полярних кнопок (верхня або нижня зона)
 * Візуально виглядає як одна область, але містить дві окремі кнопки
 */
data class PolarButtonGroup(
    val leftButton: CircularButtonData,   // Ліва половина
    val rightButton: CircularButtonData,  // Права половина
    val title: String? = null,            // Заголовок по центру
    val subtitle: String? = null,         // Підзаголовок по центру
    val titleSize: Float = 32f,           // Розмір тексту заголовка
    val titleColor: Color = Color.White,  // Колір тексту заголовка
    val subtitleSize: Float = 24f,        // Розмір тексту підзаголовка
    val subtitleColor: Color = Color.White.copy(alpha = 0.7f), // Колір тексту підзаголовка
    val iconOffsetFromEdge: Float = 0.12f // Відступ іконок від краю компонента (0.0 - 0.5)
)
