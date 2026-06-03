package com.cliche.app.ui

import com.cliche.app.models.TimelinePost
import com.cliche.app.ui.home.PostsAdapter
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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

    @Test
    fun testRemoveById() {
        val postsAdapter = PostsAdapter(
            items = mutableListOf(timelinePostDefault)
        )

        assertEquals(true, postsAdapter.removeById(1))
        assertEquals(0, postsAdapter.itemCount)
    }

    @Test
    fun testRemoveByIdNonExistent() {
        val postsAdapter = PostsAdapter(
            items = mutableListOf(timelinePostDefault)
        )

        assertEquals(false, postsAdapter.removeById(2))
        assertEquals(1, postsAdapter.itemCount)
    }

    @Test
    fun testUpdateItem() {
        val postsAdapter = PostsAdapter(
            items = mutableListOf(timelinePostDefault)
        )

        assertEquals(true, postsAdapter.updateItem(timelinePost2likes))
        assertEquals(timelinePost2likes, postsAdapter.getItems()[0])
    }

    fun testAddAll() {

    }

    fun testClear() {

    }

    fun testGetItemCount() {

    }

    fun testOnBindViewHolder() {
        // inflate des vues, charge des images
    }

    fun testOnCreateViewHolder() {
        // inflate un layout XML
    }

}