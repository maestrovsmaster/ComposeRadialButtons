package android.test.testuiapplication.circularbuttonlayout.data

import androidx.compose.ui.graphics.Color

/**
 * Дані для однієї кнопки з іконкою та текстом
 */
data class CircularButtonData(
    val icon: String,           // Іконка (або emoji)
    val text: String,           // Текст
    val onClick: () -> Unit,
    val iconColor: Color = Color(0xFFFF9800),  // Помаранчевий за замовчуванням
    val textColor: Color = Color.White
)
