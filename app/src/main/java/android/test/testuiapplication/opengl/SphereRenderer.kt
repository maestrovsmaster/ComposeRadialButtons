package android.test.testuiapplication.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Рендерер для 3D сфери з освітленням та обертанням
 */
class SphereRenderer(
    private val baseColor: FloatArray = floatArrayOf(0.2f, 0.3f, 0.5f) // Темно-синій за замовчуванням
) : GLSurfaceView.Renderer {

    private lateinit var sphere: Sphere
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var normalBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer

    private var shaderProgram: Int = 0

    // Handles для shader uniforms/attributes
    private var positionHandle: Int = 0
    private var normalHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var modelMatrixHandle: Int = 0
    private var normalMatrixHandle: Int = 0
    private var lightPosHandle: Int = 0
    private var cameraPosHandle: Int = 0
    private var baseColorHandle: Int = 0
    private var contentColorHandle: Int = 0  // Колір контенту для рефлексії

    // Матриці трансформації
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Колір контенту для рефлексії
    private val contentColor = FloatArray(3) { 1.0f }  // Білий за замовчуванням

    // Кут обертання сфери
    private var rotationAngle = 0f

    // Анімація обертання світла
    private var lightAnimationProgress = 0f  // 0..1
    private var lightAnimationActive = false
    private val lightAnimationDuration = 500f  // 500ms
    private var lightAnimationStartTime = 0L

    // Анімація обертання сфери (для зміни тексту)
    private var sphereRotationProgress = 0f  // 0..1
    private var sphereRotationActive = false
    private val sphereRotationDuration = 600f  // 600ms
    private var sphereRotationStartTime = 0L
    private var sphereRotationStartAngle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Колір фону - прозорий
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        // Увімкнути depth test для правильного рендерингу 3D
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        // Увімкнути blending для прозорості
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Створити сферу (збільшений радіус для повного вписування в коло)
        sphere = Sphere(radius = 1.28f, latitudeBands = 40, longitudeBands = 40)

        // Створити buffers для OpenGL
        vertexBuffer = ByteBuffer.allocateDirect(sphere.vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(sphere.vertices)
                position(0)
            }
        }

        normalBuffer = ByteBuffer.allocateDirect(sphere.normals.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(sphere.normals)
                position(0)
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(sphere.indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(sphere.indices)
                position(0)
            }
        }

        // Компілювати шейдери
        shaderProgram = ShaderUtils.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE)

        // Отримати handles
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
        normalHandle = GLES20.glGetAttribLocation(shaderProgram, "aNormal")
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        modelMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uModelMatrix")
        normalMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uNormalMatrix")
        lightPosHandle = GLES20.glGetUniformLocation(shaderProgram, "uLightPos")
        cameraPosHandle = GLES20.glGetUniformLocation(shaderProgram, "uCameraPos")
        baseColorHandle = GLES20.glGetUniformLocation(shaderProgram, "uBaseColor")
        contentColorHandle = GLES20.glGetUniformLocation(shaderProgram, "uContentColor")

        android.util.Log.d("SphereRenderer", "contentColorHandle = $contentColorHandle (should be >= 0)")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        // Projection matrix
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 10f)

        // View matrix - камера (зменшено відстань для більшої сфери)
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 2.8f,  // Позиція камери - ближче для кращого вписування
            0f, 0f, 0f,    // Дивимось на центр
            0f, 1f, 0f     // Up vector
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Оновити кут обертання сфери
        if (!sphereRotationActive) {
            // Нормальне повільне обертання
            rotationAngle += 0.5f
            if (rotationAngle > 360f) rotationAngle -= 360f
        } else {
            // Анімація швидкого обертання для зміни тексту
            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - sphereRotationStartTime
            sphereRotationProgress = (elapsed / sphereRotationDuration).coerceIn(0f, 1f)

            // Ease-in-out для плавності
            val eased = if (sphereRotationProgress < 0.5f) {
                2f * sphereRotationProgress * sphereRotationProgress
            } else {
                1f - 2f * (1f - sphereRotationProgress) * (1f - sphereRotationProgress)
            }

            // Оберт на 360 градусів
            rotationAngle = sphereRotationStartAngle + eased * 360f

            if (sphereRotationProgress >= 1f) {
                sphereRotationActive = false
                sphereRotationProgress = 0f
                sphereRotationStartAngle = rotationAngle
            }
        }

        // Оновити анімацію світла
        if (lightAnimationActive) {
            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - lightAnimationStartTime
            lightAnimationProgress = (elapsed / lightAnimationDuration).coerceIn(0f, 1f)

            if (lightAnimationProgress >= 1f) {
                lightAnimationActive = false
                lightAnimationProgress = 0f
            }
        }

        // Model matrix - обертання сфери
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, rotationAngle, 0f, 1f, 0f)  // Обертання навколо Y
        Matrix.rotateM(modelMatrix, 0, rotationAngle * 0.5f, 1f, 0f, 0f)  // Трохи навколо X

        // Normal matrix (для правильного освітлення при трансформаціях)
        Matrix.invertM(tempMatrix, 0, modelMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, tempMatrix, 0)

        // MVP matrix
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

        // Використати shader program
        GLES20.glUseProgram(shaderProgram)

        // Передати vertices
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 3,
            GLES20.GL_FLOAT, false,
            12, vertexBuffer
        )

        // Передати normals
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(
            normalHandle, 3,
            GLES20.GL_FLOAT, false,
            12, normalBuffer
        )

        // Передати матриці
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(normalMatrixHandle, 1, false, normalMatrix, 0)

        // Обчислити позицію світла (з анімацією обертання)
        val lightRadius = 6f
        val baseAngle = 45f  // Базовий кут в градусах
        val animationAngle = lightAnimationProgress * 360f  // Повний оберт за анімацію
        val totalAngle = (baseAngle + animationAngle) * (PI / 180f).toFloat()

        val lightX = lightRadius * cos(totalAngle)
        val lightY = 3f  // Фіксована висота
        val lightZ = lightRadius * sin(totalAngle)

        // Передати параметри освітлення
        GLES20.glUniform3f(lightPosHandle, lightX, lightY, lightZ)  // Позиція світла (анімована)
        GLES20.glUniform3f(cameraPosHandle, 0f, 0f, 2.8f)  // Позиція камери (відповідає viewMatrix)
        GLES20.glUniform3f(baseColorHandle, baseColor[0], baseColor[1], baseColor[2])
        GLES20.glUniform3f(contentColorHandle, contentColor[0], contentColor[1], contentColor[2])

        // Намалювати сферу
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            sphere.vertexCount,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        // Вимкнути arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }

    /**
     * Змінити колір сфери динамічно
     */
    fun setColor(r: Float, g: Float, b: Float) {
        baseColor[0] = r
        baseColor[1] = g
        baseColor[2] = b
    }

    /**
     * Запустити анімацію обертання світла (для feedback при кліку)
     */
    fun triggerLightAnimation() {
        lightAnimationActive = true
        lightAnimationProgress = 0f
        lightAnimationStartTime = System.currentTimeMillis()
    }

    /**
     * Запустити анімацію обертання сфери (для зміни тексту)
     */
    fun triggerSphereRotation() {
        sphereRotationActive = true
        sphereRotationProgress = 0f
        sphereRotationStartTime = System.currentTimeMillis()
        sphereRotationStartAngle = rotationAngle
    }

    /**
     * Отримати прогрес обертання сфери (для синхронізації з текстом)
     */
    fun getSphereRotationProgress(): Float = sphereRotationProgress

    /**
     * Встановити колір контенту для рефлексії на внутрішній поверхні сфери
     */
    fun setContentColor(r: Float, g: Float, b: Float) {
        contentColor[0] = r
        contentColor[1] = g
        contentColor[2] = b
        android.util.Log.d("SphereRenderer", "Content color set to RGB($r, $g, $b)")
    }
}
