package com.cliche.app.ui.home

import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.cliche.app.models.TimelinePost
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.cliche.app.R

/**
 * Unit tests for [PostsAdapter] data class.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PostsAdapterTest {
    val timelinePostDefault = TimelinePost(
        id = 1,
        created_at = "",
        caption = null,
        owner_id = "",
        owner_username = null,
        owner_avatar_url = null,
        media_paths = listOf(""),
        likes_count = 0,
        comments_count = 0,
        liked_by_user = false,
        bookmarked_by_user = false
    )

    val timelinePost2likes = timelinePostDefault.copy(likes_count = 2)
    val timelinePostId100 = timelinePostDefault.copy(id = 100)

    @Test
    fun testRemoveById() {
        val postsAdapter = PostsAdapter(
            items = mutableListOf(timelinePostDefault)
        )

        TestCase.assertEquals(true, postsAdapter.removeById(1))
        TestCase.assertEquals(0, postsAdapter.itemCount)
    }

    @Test
    fun testRemoveByIdNonExistent() {
        val postsAdapter = PostsAdapter(
            items = mutableListOf(timelinePostDefault)
        )

        TestCase.assertEquals(false, postsAdapter.removeById(2))
        TestCase.assertEquals(1, postsAdapter.itemCount)
    }

    @Test
    fun testUpdateItem() {
        val postsAdapter = PostsAdapter(
            items = mutableListOf(timelinePostDefault)
        )

        TestCase.assertEquals(true, postsAdapter.updateItem(timelinePost2likes))
        TestCase.assertEquals(timelinePost2likes, postsAdapter.getItems()[0])
    }

    @Test
    fun testUpdateItemIdNotExist() {
        val postsAdapter = PostsAdapter(
            items = mutableListOf(timelinePostDefault)
        )

        TestCase.assertEquals(false, postsAdapter.updateItem(timelinePostId100))
        TestCase.assertEquals(timelinePostDefault, postsAdapter.getItems()[0])
    }

    @Test
    fun testAddAll() {
        val postsAdapter = PostsAdapter(items = mutableListOf())
        postsAdapter.addAll(listOf(timelinePostDefault, timelinePost2likes))
        TestCase.assertEquals(2, postsAdapter.itemCount)
    }

    @Test
    fun testClear() {
        val postsAdapter = PostsAdapter(items = mutableListOf(timelinePostDefault, timelinePost2likes))
        postsAdapter.clear()
        TestCase.assertEquals(0, postsAdapter.itemCount)
    }

    @Test
    fun testGetItemCount() {
        val postsAdapter = PostsAdapter(items = mutableListOf(timelinePostDefault, timelinePost2likes))
        TestCase.assertEquals(2, postsAdapter.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.setTheme(R.style.Theme_Cliché)
        val parent = FrameLayout(context)

        val postsAdapter = PostsAdapter(items = mutableListOf(timelinePostDefault))
        val viewHolder = postsAdapter.onCreateViewHolder(parent, 0)

        TestCase.assertNotNull(viewHolder.tvUsername)
        TestCase.assertNotNull(viewHolder.ivAvatar)
    }

//    @Test
//    fun testOnBindViewHolder() {
//        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
//        context.setTheme(R.style.Theme_Cliché)
//        val parent = FrameLayout(context)
//
//        val postsAdapter = PostsAdapter(items = mutableListOf(timelinePostDefault))
//        val viewHolder = postsAdapter.onCreateViewHolder(parent, 0)
//
//        postsAdapter.onBindViewHolder(viewHolder, 0)
//
//        TestCase.assertEquals("1234", viewHolder.tvUsername.text.toString())
//        TestCase.assertEquals("0 likes", viewHolder.tvLikes.text.toString())
//    }

}