package com.cliche.app.models

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [TimelinePost] data class.
 */
class TimelinePostTest {

    @Test
    fun testTimelinePostCreation() {
        val post = TimelinePost(
            id = 1L,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner_id = "user1",
            owner_username = "username1",
            owner_avatar_url = "http://example.com/avatar.jpg",
            media_paths = listOf("path1", "path2"),
            likes_count = 10,
            comments_count = 5,
            liked_by_user = true,
            bookmarked_by_user = false,
            latitude = 45.0,
            longitude = -73.0
        )

        assertEquals(1L, post.id)
        assertEquals("2024-01-01T12:00:00Z", post.created_at)
        assertEquals("Test caption", post.caption)
        assertEquals("user1", post.owner_id)
        assertEquals("username1", post.owner_username)
        assertEquals("http://example.com/avatar.jpg", post.owner_avatar_url)
        assertEquals(listOf("path1", "path2"), post.media_paths)
        assertEquals(10, post.likes_count)
        assertEquals(5, post.comments_count)
        assertTrue(post.liked_by_user)
        assertFalse(post.bookmarked_by_user)
        assertEquals(45.0, post.latitude ?: 0.0, 0.001)
        assertEquals(-73.0, post.longitude ?: 0.0, 0.001)
    }

    @Test
    fun testTimelinePostWithNullFields() {
        val post = TimelinePost(
            id = 2L,
            created_at = "2024-01-02T12:00:00Z",
            caption = null,
            owner_id = "user2",
            owner_username = null,
            owner_avatar_url = null,
            media_paths = emptyList(),
            likes_count = 0,
            comments_count = 0,
            liked_by_user = false,
            bookmarked_by_user = false,
            latitude = null,
            longitude = null
        )

        assertEquals(2L, post.id)
        assertEquals("2024-01-02T12:00:00Z", post.created_at)
        assertTrue(post.caption.isNullOrEmpty())
        assertEquals("user2", post.owner_id)
        assertTrue(post.owner_username.isNullOrEmpty())
        assertTrue(post.owner_avatar_url.isNullOrEmpty())
        assertTrue(post.media_paths.isEmpty())
        assertEquals(0, post.likes_count)
        assertEquals(0, post.comments_count)
        assertFalse(post.liked_by_user)
        assertFalse(post.bookmarked_by_user)
        assertTrue(post.latitude == null)
        assertTrue(post.longitude == null)
    }

    @Test
    fun testTimelinePostEqualsAndHashCode() {
        val post1 = TimelinePost(
            id = 1L,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner_id = "user1",
            owner_username = "username1",
            owner_avatar_url = "http://example.com/avatar.jpg",
            media_paths = listOf("path1"),
            likes_count = 10,
            comments_count = 5,
            liked_by_user = true,
            bookmarked_by_user = false
        )

        val post2 = TimelinePost(
            id = 1L,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner_id = "user1",
            owner_username = "username1",
            owner_avatar_url = "http://example.com/avatar.jpg",
            media_paths = listOf("path1"),
            likes_count = 10,
            comments_count = 5,
            liked_by_user = true,
            bookmarked_by_user = false
        )

        val post3 = TimelinePost(
            id = 2L,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner_id = "user1",
            owner_username = "username1",
            owner_avatar_url = "http://example.com/avatar.jpg",
            media_paths = listOf("path1"),
            likes_count = 10,
            comments_count = 5,
            liked_by_user = true,
            bookmarked_by_user = false
        )

        assertEquals(post1, post2)
        assertEquals(post1.hashCode(), post2.hashCode())
        assertNotEquals(post1, post3)
    }

    @Test
    fun testTimelinePostCopy() {
        val original = TimelinePost(
            id = 1L,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Original caption",
            owner_id = "user1",
            owner_username = "username1",
            owner_avatar_url = "http://example.com/avatar.jpg",
            media_paths = listOf("path1"),
            likes_count = 10,
            comments_count = 5,
            liked_by_user = true,
            bookmarked_by_user = false
        )

        val copied = original.copy(caption = "Modified caption", liked_by_user = false)

        assertEquals(original.id, copied.id)
        assertEquals("Modified caption", copied.caption)
        assertEquals(original.owner_id, copied.owner_id)
        assertFalse(copied.liked_by_user)
        assertEquals(original.comments_count, copied.comments_count)
    }

    @Test
    fun testTimelinePostToString() {
        val post = TimelinePost(
            id = 1L,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner_id = "user1",
            owner_username = "username1",
            owner_avatar_url = null,
            media_paths = emptyList(),
            likes_count = 0,
            comments_count = 0,
            liked_by_user = false,
            bookmarked_by_user = false
        )

        val string = post.toString()
        assert(string.contains("id=1"))
        assert(string.contains("created_at=2024-01-01T12:00:00Z"))
        assert(string.contains("owner_id=user1"))
    }
}
