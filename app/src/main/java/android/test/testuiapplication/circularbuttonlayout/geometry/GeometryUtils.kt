package android.test.testuiapplication.circularbuttonlayout.geometry

import android.test.testuiapplication.circularbuttonlayout.data.Side
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Повертає візуальний індекс кнопки залежно від сторони
 * Для правої сторони кнопки розташовуються у зворотньому порядку
 */
fun visualIndex(index: Int, total: Int, side: Side): Int =
    if (side == Side.RIGHT) total - 1 - index else index

/**
 * Обчислює X координату точки на колі з заданим радіусом та Y координатою
 */
fun getXOnCircle(
    cx: Float,
    cy: Float,
    r: Float,
    y: Float,
    side: Side
): Float {
    val dx = sqrt(r * r - (y - cy).pow(2))
    return if (side == Side.LEFT) cx - dx else cx + dx
}

/**
 * Обчислює початковий кут для внутрішньої дуги кнопки
 */
fun innerStartAngle(a: Float, side: Side) =
    if (side == Side.LEFT) 180f - Math.toDegrees(a.toDouble()).toFloat()
    else Math.toDegrees(a.toDouble()).toFloat()

/**
 * Обчислює кут повороту для внутрішньої дуги кнопки
 */
fun innerSweep(a1: Float, a2: Float, side: Side) =
    Math.toDegrees((a2 - a1).toDouble()).toFloat() * if (side == Side.LEFT) -1 else 1

/**
 * Обчислює початковий кут для зовнішньої дуги кнопки
 */
fun outerStartAngle(a: Float, side: Side) =
    if (side == Side.LEFT) 180f - Math.toDegrees(a.toDouble()).toFloat()
    else Math.toDegrees(a.toDouble()).toFloat()

/**
 * Обчислює кут повороту для зовнішньої дуги кнопки
 */
fun outerSweep(a1: Float, a2: Float, side: Side) =
    Math.toDegrees((a1 - a2).toDouble()).toFloat() * if (side == Side.LEFT) -1 else 1
