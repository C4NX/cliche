package com.cliche.app.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [Like] data class.
 */
class LikeTest {

    @Test
    fun testLikeCreation() {
        val like = Like(
            id = 1L,
            post_id = 100L,
            user_id = "user123"
        )

        assertEquals(1L, like.id)
        assertEquals(100L, like.post_id)
        assertEquals("user123", like.user_id)
    }

    @Test
    fun testLikeWithNullFields() {
        val like = Like(
            id = null,
            post_id = 200L,
            user_id = null
        )

        assertNull(like.id)
        assertEquals(200L, like.post_id)
        assertNull(like.user_id)
    }

    @Test
    fun testLikeWithDefaultValues() {
        val like = Like(
            post_id = 300L
        )

        assertNull(like.id)
        assertEquals(300L, like.post_id)
        assertNull(like.user_id)
    }

    @Test
    fun testLikeEqualsAndHashCode() {
        val like1 = Like(
            id = 1L,
            post_id = 100L,
            user_id = "user1"
        )

        val like2 = Like(
            id = 1L,
            post_id = 100L,
            user_id = "user1"
        )

        val like3 = Like(
            id = 2L,
            post_id = 100L,
            user_id = "user1"
        )

        assertEquals(like1, like2)
        assertEquals(like1.hashCode(), like2.hashCode())
        assertNotEquals(like1, like3)
    }

    @Test
    fun testLikeCopy() {
        val original = Like(
            id = 1L,
            post_id = 100L,
            user_id = "user1"
        )

        val copied = original.copy(post_id = 200L)

        assertEquals(original.id, copied.id)
        assertEquals(200L, copied.post_id)
        assertEquals(original.user_id, copied.user_id)
    }

    @Test
    fun testLikeToString() {
        val like = Like(
            id = 1L,
            post_id = 100L,
            user_id = "user1"
        )

        val string = like.toString()
        assert(string.contains("id=1"))
        assert(string.contains("post_id=100"))
        assert(string.contains("user_id=user1"))
    }
}
