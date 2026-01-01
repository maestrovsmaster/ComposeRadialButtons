package android.test.testuiapplication.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Composable функція для відображення 3D сфери
 */
@Composable
fun SphereView(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF2C3E50),  // Темно-синій за замовчуванням
    alpha: Float = 1.0f,
    onViewCreated: ((SphereGLSurfaceView) -> Unit)? = null  // Callback для отримання reference на view
) {
    val context = LocalContext.current

    val glSurfaceView = remember {
        val view = SphereGLSurfaceView(context, color)
        onViewCreated?.invoke(view)
        view
    }

    AndroidView(
        factory = { glSurfaceView },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            glSurfaceView.onPause()
        }
    }
}

/**
 * Створює GLSurfaceView з налаштованим рендерером
 */
private fun createGLSurfaceView(
    context: Context,
    color: Color,
    alpha: Float
): GLSurfaceView {
    return GLSurfaceView(context).apply {
        // Використовувати OpenGL ES 2.0
        setEGLContextClientVersion(2)

        // Встановити прозорий фон
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)

        // Media overlay дозволяє GLSurfaceView бути між шарами Canvas
        setZOrderMediaOverlay(true)

        // Конвертувати Color в RGB для шейдера
        val argb = color.toArgb()
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f

        // Створити рендерер
        val renderer = SphereRenderer(
            baseColor = floatArrayOf(r * alpha, g * alpha, b * alpha)
        )
        setRenderer(renderer)

        // Рендерити тільки коли є зміни (для анімації - RENDERMODE_CONTINUOUSLY)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}

/**
 * Кастомний GLSurfaceView з керуванням lifecycle
 */
class SphereGLSurfaceView(
    context: Context,
    color: Color = Color(0xFF2C3E50)
) : GLSurfaceView(context) {

    private val renderer: SphereRenderer

    init {
        setEGLContextClientVersion(2)

        // Прозорий фон
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)

        // Конвертувати колір
        val argb = color.toArgb()
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f

        renderer = SphereRenderer(floatArrayOf(r, g, b))
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    /**
     * Змінити колір сфери
     */
    fun setSphereColor(color: Color) {
        val argb = color.toArgb()
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        renderer.setColor(r, g, b)
    }

    /**
     * Запустити анімацію обертання світла навколо сфери
     */
    fun triggerLightAnimation() {
        queueEvent {
            renderer.triggerLightAnimation()
        }
    }

    /**
     * Запустити анімацію обертання сфери (для зміни тексту)
     */
    fun triggerSphereRotation() {
        queueEvent {
            renderer.triggerSphereRotation()
        }
    }

    /**
     * Отримати прогрес обертання сфери
     */
    fun getSphereRotationProgress(): Float = renderer.getSphereRotationProgress()

    /**
     * Встановити колір контенту для рефлексії
     */
    fun setContentColor(color: Color) {
        val argb = color.toArgb()
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        queueEvent {
            renderer.setContentColor(r, g, b)
        }
    }
}
