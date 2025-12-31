package android.test.testuiapplication.circularbuttonlayout.data

import androidx.compose.ui.graphics.Color

/**
 * Тема для центральної кнопки
 */
data class CenterButtonTheme(
    val backgroundColor: Color = Color(0xFF2C3E50),
    val borderColor: Color = Color.White,
    val borderWidth: Float = 3f,
    val textColor: Color = Color.White,
    val textSize: Float = 40f
)

/**
 * Тема для основних кнопок з текстом (бічні кнопки)
 */
data class MainButtonTheme(
    val backgroundColor: Color = Color(0xFF37474F),
    val activeBackgroundColor: Color = Color(0xFFFF9800),
    val notchColor: Color = Color(0xFFBDBDBD),          // Колір notch для неактивної кнопки
    val activeNotchColor: Color = Color(0xFFFFEB3B),    // Колір notch для активної кнопки
    val textColor: Color = Color(0xFF888888),           // Сірий текст для неактивної кнопки
    val activeTextColor: Color = Color(0xFFFFCC99),     // Теплий підсвічений текст для активної
    val iconColor: Color = Color(0xFF666666),           // Сіра іконка для неактивної кнопки
    val activeIconColor: Color = Color(0xFFFF9966)      // Теплий помаранчевий для активної іконки
)

/**
 * Тема для кнопок з іконками (внутрішній сегмент) та polar buttons
 */
data class IconButtonTheme(
    val backgroundColor: Color = Color(0xFF2C3E50),
    val activeBackgroundColor: Color = Color(0xFFFF6B6B),
    val iconColor: Color = Color(0xFFFF9800),
    val activeIconColor: Color = Color(0xFFFFFFFF)
)

/**
 * Тема для under-polar елементів
 */
data class UnderPolarTheme(
    val backgroundColor: Color = Color(0xFF334233)
)

/**
 * Головна тема для всього circular layout
 */
data class CircularLayoutTheme(
    val centerButton: CenterButtonTheme = CenterButtonTheme(),
    val mainButtons: MainButtonTheme = MainButtonTheme(),
    val iconButtons: IconButtonTheme = IconButtonTheme(),
    val underPolar: UnderPolarTheme = UnderPolarTheme()
) {
    companion object {
        /**
         * Тема за замовчуванням
         */
        fun default() = CircularLayoutTheme()

        /**
         * Темна тема
         */
        fun dark() = CircularLayoutTheme(
            centerButton = CenterButtonTheme(
                backgroundColor = Color(0xFF1A1A1A),
                borderColor = Color(0xFF444444),
                textColor = Color.White
            ),
            mainButtons = MainButtonTheme(
                backgroundColor = Color(0xFF2C2C2C),
                activeBackgroundColor = Color(0xFFFF9800),
                notchColor = Color(0xFF666666),
                activeNotchColor = Color(0xFFFFEB3B)
            ),
            iconButtons = IconButtonTheme(
                backgroundColor = Color(0xFF1A1A1A),
                activeBackgroundColor = Color(0xFFFF6B6B)
            ),
            underPolar = UnderPolarTheme(
                backgroundColor = Color(0xFF1F1F1F)
            )
        )
    }
}
