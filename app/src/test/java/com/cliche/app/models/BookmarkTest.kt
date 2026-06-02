package com.cliche.app.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [Bookmark] data class.
 */
class BookmarkTest {

    @Test
    fun testBookmarkCreation() {
        val bookmark = Bookmark(
            id = 1L,
            post_id = 100L,
            profile_id = "user123"
        )

        assertEquals(1L, bookmark.id)
        assertEquals(100L, bookmark.post_id)
        assertEquals("user123", bookmark.profile_id)
    }

    @Test
    fun testBookmarkWithNullFields() {
        val bookmark = Bookmark(
            id = null,
            post_id = 200L,
            profile_id = null
        )

        assertNull(bookmark.id)
        assertEquals(200L, bookmark.post_id)
        assertNull(bookmark.profile_id)
    }

    @Test
    fun testBookmarkWithDefaultValues() {
        val bookmark = Bookmark(
            post_id = 300L
        )

        assertNull(bookmark.id)
        assertEquals(300L, bookmark.post_id)
        assertNull(bookmark.profile_id)
    }

    @Test
    fun testBookmarkEqualsAndHashCode() {
        val bookmark1 = Bookmark(
            id = 1L,
            post_id = 100L,
            profile_id = "user1"
        )

        val bookmark2 = Bookmark(
            id = 1L,
            post_id = 100L,
            profile_id = "user1"
        )

        val bookmark3 = Bookmark(
            id = 2L,
            post_id = 100L,
            profile_id = "user1"
        )

        assertEquals(bookmark1, bookmark2)
        assertEquals(bookmark1.hashCode(), bookmark2.hashCode())
        assertNotEquals(bookmark1, bookmark3)
    }

    @Test
    fun testBookmarkCopy() {
        val original = Bookmark(
            id = 1L,
            post_id = 100L,
            profile_id = "user1"
        )

        val copied = original.copy(post_id = 200L)

        assertEquals(original.id, copied.id)
        assertEquals(200L, copied.post_id)
        assertEquals(original.profile_id, copied.profile_id)
    }

    @Test
    fun testBookmarkToString() {
        val bookmark = Bookmark(
            id = 1L,
            post_id = 100L,
            profile_id = "user1"
        )

        val string = bookmark.toString()
        assert(string.contains("id=1"))
        assert(string.contains("post_id=100"))
        assert(string.contains("profile_id=user1"))
    }
}
