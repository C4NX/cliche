package com.cliche.app.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.size.Size
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class VintagePolaroidTransformationTest {

    private lateinit var transformation: VintagePolaroidTransformation
    private lateinit var testBitmap: Bitmap

    @Before
    fun setUp() {
        transformation = VintagePolaroidTransformation()
        testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }
    }

    @Test
    fun `cacheKey should be constant`() {
        assertEquals("VintagePolaroidTransformation_v1", transformation.cacheKey)
    }

    @Test
    fun `transform should return non-null bitmap`() = runBlocking {
        val result = transformation.transform(testBitmap, Size(100, 100))
        assertNotNull(result)
    }

    @Test
    fun `transform should preserve dimensions`() = runBlocking {
        val result = transformation.transform(testBitmap, Size(100, 100))
        assertEquals(testBitmap.width, result.width)
        assertEquals(testBitmap.height, result.height)
    }

    @Test
    fun `transform should change input colors`() = runBlocking {
        val redBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }

        val result = transformation.transform(redBitmap, Size(100, 100))
        var differentPixels = 0
        for (x in 0 until result.width) {
            for (y in 0 until result.height) {
                if (result.getPixel(x, y) != redBitmap.getPixel(x, y)) {
                    differentPixels++
                    break
                }
            }
            if (differentPixels > 0) break
        }

        assertTrue("Result should differ from the input red bitmap", differentPixels > 0)
    }
}