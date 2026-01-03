package com.radialbuttons.circularbuttonlayout.opengl

import android.opengl.GLES20

/**
 * Vertex shader - обробляє кожну вершину
 */
const val VERTEX_SHADER_CODE = """
    uniform mat4 uMVPMatrix;
    uniform mat4 uModelMatrix;
    uniform mat4 uNormalMatrix;

    attribute vec4 aPosition;
    attribute vec3 aNormal;

    varying vec3 vNormal;
    varying vec3 vPosition;

    void main() {
        vPosition = vec3(uModelMatrix * aPosition);
        vNormal = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0)));
        gl_Position = uMVPMatrix * aPosition;
    }
"""

/**
 * Fragment shader - обробляє кожен піксель з освітленням Phong
 */
const val FRAGMENT_SHADER_CODE = """
    precision mediump float;

    uniform vec3 uLightPos;
    uniform vec3 uCameraPos;
    uniform vec3 uBaseColor;
    uniform vec3 uContentColor;  // Колір контенту для рефлексії

    varying vec3 vNormal;
    varying vec3 vPosition;

    void main() {
        // Ambient (фонове освітлення)
        float ambientStrength = 0.3;
        vec3 ambient = ambientStrength * uBaseColor;

        // Diffuse (розсіяне освітлення)
        vec3 norm = normalize(vNormal);
        vec3 lightDir = normalize(uLightPos - vPosition);
        float diff = max(dot(norm, lightDir), 0.0);
        vec3 diffuse = diff * uBaseColor;

        // Specular (блиск/відблиск)
        float specularStrength = 0.18;
        vec3 viewDir = normalize(uCameraPos - vPosition);
        vec3 reflectDir = reflect(-lightDir, norm);
        float spec = pow(max(dot(viewDir, reflectDir), 0.76), 0.8);
        // Віддітні кольорів сфери.
        vec3 specular = specularStrength * spec * vec3(1.0, 1.2, 1.3);

        // Внутрішня рефлексія кольору контенту (на нижній лівій частині)
        float innerRimStrength = 0.85;
        float innerRimPower = 3.0;
        // Інвертуємо для внутрішньої рефлексії
        float innerRimFactor = max(dot(viewDir, norm), 0.0);
        innerRimFactor = pow(innerRimFactor, innerRimPower);
        // Більше рефлексії знизу та зліва
        float bottomLeftBias = max(0.0, -norm.y * 0.5 + 0.5) * max(0.0, -norm.x * 0.5 + 0.5);
        vec3 innerReflection = innerRimStrength * innerRimFactor * bottomLeftBias * uContentColor;

        vec3 result = ambient + diffuse + specular + innerReflection;
        // Напівпрозора сфера як новогодня кулька
        gl_FragColor = vec4(result, 0.75);
    }
"""

/**
 * Утиліти для компіляції шейдерів
 */
object ShaderUtils {
    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // Перевірка на помилки компіляції
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Error compiling shader: $error")
        }

        return shader
    }

    fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        // Перевірка на помилки лінкування
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Error linking program: $error")
        }

        return program
    }
}
