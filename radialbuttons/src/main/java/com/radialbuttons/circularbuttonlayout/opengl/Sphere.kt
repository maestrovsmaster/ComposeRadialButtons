package com.radialbuttons.circularbuttonlayout.opengl

import kotlin.math.cos
import kotlin.math.sin

/**
 * Генератор геометрії сфери для OpenGL ES
 */
class Sphere(
    private val radius: Float = 1.0f,
    private val latitudeBands: Int = 30,
    private val longitudeBands: Int = 30
) {
    val vertices: FloatArray
    val normals: FloatArray
    val indices: ShortArray
    val vertexCount: Int

    init {
        val verticesList = mutableListOf<Float>()
        val normalsList = mutableListOf<Float>()
        val indicesList = mutableListOf<Short>()

        // Генеруємо вершини та нормалі
        for (latNumber in 0..latitudeBands) {
            val theta = latNumber * Math.PI / latitudeBands
            val sinTheta = sin(theta).toFloat()
            val cosTheta = cos(theta).toFloat()

            for (longNumber in 0..longitudeBands) {
                val phi = longNumber * 2 * Math.PI / longitudeBands
                val sinPhi = sin(phi).toFloat()
                val cosPhi = cos(phi).toFloat()

                val x = cosPhi * sinTheta
                val y = cosTheta
                val z = sinPhi * sinTheta

                // Нормаль (для освітлення)
                normalsList.add(x)
                normalsList.add(y)
                normalsList.add(z)

                // Позиція вершини
                verticesList.add(radius * x)
                verticesList.add(radius * y)
                verticesList.add(radius * z)
            }
        }

        // Генеруємо індекси для трикутників
        for (latNumber in 0 until latitudeBands) {
            for (longNumber in 0 until longitudeBands) {
                val first = (latNumber * (longitudeBands + 1) + longNumber).toShort()
                val second = (first + longitudeBands + 1).toShort()

                indicesList.add(first)
                indicesList.add(second)
                indicesList.add((first + 1).toShort())

                indicesList.add(second)
                indicesList.add((second + 1).toShort())
                indicesList.add((first + 1).toShort())
            }
        }

        vertices = verticesList.toFloatArray()
        normals = normalsList.toFloatArray()
        indices = indicesList.toShortArray()
        vertexCount = indices.size
    }
}
