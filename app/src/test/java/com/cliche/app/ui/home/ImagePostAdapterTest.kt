package com.cliche.app.ui.home

import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.cliche.app.R
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [ImagePostAdapter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImagePostAdapterTest {

    private val imageUrls = listOf(
        "https://example.com/image1.jpg",
        "https://example.com/image2.jpg",
        "https://example.com/image3.jpg"
    )

    @Test
    fun testGetItemCount() {
        val adapter = ImagePostAdapter(imageUrls)
        TestCase.assertEquals(3, adapter.itemCount)
    }

    @Test
    fun testGetItemCountEmpty() {
        val adapter = ImagePostAdapter(emptyList())
        TestCase.assertEquals(0, adapter.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.setTheme(R.style.Theme_Cliché)
        val parent = FrameLayout(context)

        val adapter = ImagePostAdapter(imageUrls)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        TestCase.assertNotNull(viewHolder.imageView)
    }

    @Test
    fun testOnBindViewHolder() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.setTheme(R.style.Theme_Cliché)
        val parent = FrameLayout(context)

        val adapter = ImagePostAdapter(imageUrls)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Coil charge l'image de façon asynchrone : on vérifie seulement
        // que le binding ne plante pas et que la vue existe bien après bind.
        adapter.onBindViewHolder(viewHolder, 0)

        TestCase.assertNotNull(viewHolder.imageView)
    }
}