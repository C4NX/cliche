package com.cliche.app.models

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [Post] data class.
 */
class PostTest {

    @Test
    fun testPostCreation() {
        val post = Post(
            id = 1,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner = Profile("user1", "username1"),
            media_paths = listOf("path1", "path2"),
            latitude = 45.0,
            longitude = -73.0
        )

        assertEquals(1, post.id)
        assertEquals("2024-01-01T12:00:00Z", post.created_at)
        assertEquals("Test caption", post.caption)
        assertEquals("user1", post.owner?.id)
        assertEquals(listOf("path1", "path2"), post.media_paths)
        assertEquals(45.0, post.latitude ?: 0.0, 0.001)
        assertEquals(-73.0, post.longitude ?: 0.0, 0.001)
    }

    @Test
    fun testPostWithNullFields() {
        val post = Post(
            id = 2,
            created_at = "2024-01-02T12:00:00Z",
            caption = null,
            owner = null,
            media_paths = null,
            latitude = null,
            longitude = null
        )

        assertEquals(2, post.id)
        assertNull(post.caption)
        assertNull(post.owner)
        assertNull(post.media_paths)
        assertNull(post.latitude)
        assertNull(post.longitude)
    }

    @Test
    fun testPostEqualsAndHashCode() {
        val post1 = Post(
            id = 1,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner = Profile("user1", "username1"),
            media_paths = listOf("path1"),
            latitude = 45.0,
            longitude = -73.0
        )

        val post2 = Post(
            id = 1,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner = Profile("user1", "username1"),
            media_paths = listOf("path1"),
            latitude = 45.0,
            longitude = -73.0
        )

        val post3 = Post(
            id = 2,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner = Profile("user1", "username1"),
            media_paths = listOf("path1"),
            latitude = 45.0,
            longitude = -73.0
        )

        assertEquals(post1, post2)
        assertEquals(post1.hashCode(), post2.hashCode())
        assertNotEquals(post1, post3)
    }

    @Test
    fun testPostCopy() {
        val original = Post(
            id = 1,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Original caption",
            owner = Profile("user1", "username1"),
            media_paths = listOf("path1"),
            latitude = 45.0,
            longitude = -73.0
        )

        val copied = original.copy(caption = "Modified caption")

        assertEquals(original.id, copied.id)
        assertEquals(original.created_at, copied.created_at)
        assertEquals("Modified caption", copied.caption)
        assertEquals(original.owner, copied.owner)
        assertEquals(original.media_paths, copied.media_paths)
        assertEquals(original.latitude ?: 0.0, copied.latitude ?: 0.0, 0.001)
        assertEquals(original.longitude ?: 0.0, copied.longitude ?: 0.0, 0.001)
    }

    @Test
    fun testPostToString() {
        val post = Post(
            id = 1,
            created_at = "2024-01-01T12:00:00Z",
            caption = "Test caption",
            owner = null,
            media_paths = null
        )

        val string = post.toString()
        assertTrue(string.contains("id=1"))
        assertTrue(string.contains("created_at=2024-01-01T12:00:00Z"))
        assertTrue(string.contains("caption=Test caption"))
    }
}
