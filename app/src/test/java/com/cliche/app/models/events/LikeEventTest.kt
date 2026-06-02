package com.cliche.app.models.events

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [LikeEvent] data class.
 */
class LikeEventTest {

    @Test
    fun testLikeEventCreation() {
        val event = LikeEvent(
            id = 1L,
            post_id = 100L,
            user_id = "user123"
        )

        assertEquals(1L, event.id)
        assertEquals(100L, event.post_id)
        assertEquals("user123", event.user_id)
    }

    @Test
    fun testLikeEventWithNullFields() {
        val event = LikeEvent(
            id = null,
            post_id = 200L,
            user_id = null
        )

        assertNull(event.id)
        assertEquals(200L, event.post_id)
        assertNull(event.user_id)
    }

    @Test
    fun testLikeEventWithDefaultValues() {
        val event = LikeEvent(
            post_id = 300L
        )

        assertNull(event.id)
        assertEquals(300L, event.post_id)
        assertNull(event.user_id)
    }

    @Test
    fun testLikeEventEqualsAndHashCode() {
        val event1 = LikeEvent(
            id = 1L,
            post_id = 100L,
            user_id = "user1"
        )

        val event2 = LikeEvent(
            id = 1L,
            post_id = 100L,
            user_id = "user1"
        )

        val event3 = LikeEvent(
            id = 2L,
            post_id = 100L,
            user_id = "user1"
        )

        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
        assertNotEquals(event1, event3)
    }

    @Test
    fun testLikeEventCopy() {
        val original = LikeEvent(
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
    fun testLikeEventToString() {
        val event = LikeEvent(
            id = 1L,
            post_id = 100L,
            user_id = "user1"
        )

        val string = event.toString()
        assert(string.contains("id=1"))
        assert(string.contains("post_id=100"))
        assert(string.contains("user_id=user1"))
    }

    @Test
    fun testEventNameConstant() {
        assertEquals("insert", LikeEvent.EVENT_NAME)
    }
}
