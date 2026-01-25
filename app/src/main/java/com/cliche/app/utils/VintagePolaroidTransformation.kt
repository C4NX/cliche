package com.cliche.app.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.min
import kotlin.random.Random

/**
 * Transformation Coil pour donner un effet vintage "Polaroid".
 */
class VintagePolaroidTransformation : Transformation {
    override val cacheKey: String = "VintagePolaroidTransformation_v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val width = input.width
        val height = input.height

        val output = createBitmap(width, height)
        val canvas = Canvas(output)

        val colorMatrix = ColorMatrix().apply {
            setSaturation(0.75f)

            postConcat(
                ColorMatrix(
                    floatArrayOf(
                        1.1f, 0f, 0f, 0f, 10f,
                        0f, 1.05f, 0f, 0f, 5f,
                        0f, 0f, 0.95f, 0f, -5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        canvas.drawBitmap(input, 0f, 0f, paint)
        canvas.drawRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            Paint().apply {
                color = Color.WHITE
                alpha = 25
            }
        )

        val vignettePaint = Paint().apply {
            shader = RadialGradient(
                width / 2f,
                height / 2f,
                min(width, height) * 0.75f,
                intArrayOf(0x00000000, 0x55000000),
                floatArrayOf(0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)

        val grainPaint = Paint()
        repeat((width * height) / 80) {
            val x = Random.nextInt(width)
            val y = Random.nextInt(height)
            grainPaint.color = Color.argb(
                Random.nextInt(10, 30),
                0, 0, 0
            )
            canvas.drawPoint(x.toFloat(), y.toFloat(), grainPaint)
        }

        return output
    }
}