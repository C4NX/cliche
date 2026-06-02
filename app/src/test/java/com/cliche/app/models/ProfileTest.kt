package com.cliche.app.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [Profile] data class.
 */
class ProfileTest {

    @Test
    fun testProfileCreation() {
        val profile = Profile(
            id = "user123",
            username = "testuser",
            bio = "Test bio",
            avatar_url = "http://example.com/avatar.jpg"
        )

        assertEquals("user123", profile.id)
        assertEquals("testuser", profile.username)
        assertEquals("Test bio", profile.bio)
        assertEquals("http://example.com/avatar.jpg", profile.avatar_url)
    }

    @Test
    fun testProfileWithNullFields() {
        val profile = Profile(
            id = "user456",
            username = "anotheruser",
            bio = null,
            avatar_url = null
        )

        assertEquals("user456", profile.id)
        assertEquals("anotheruser", profile.username)
        assertNull(profile.bio)
        assertNull(profile.avatar_url)
    }

    @Test
    fun testProfileWithDefaultValues() {
        val profile = Profile(
            id = "user789",
            username = "defaultuser"
        )

        assertEquals("user789", profile.id)
        assertEquals("defaultuser", profile.username)
        assertNull(profile.bio)
        assertNull(profile.avatar_url)
    }

    @Test
    fun testProfileEqualsAndHashCode() {
        val profile1 = Profile(
            id = "user1",
            username = "testuser",
            bio = "Test bio",
            avatar_url = "http://example.com/avatar.jpg"
        )

        val profile2 = Profile(
            id = "user1",
            username = "testuser",
            bio = "Test bio",
            avatar_url = "http://example.com/avatar.jpg"
        )

        val profile3 = Profile(
            id = "user2",
            username = "testuser",
            bio = "Test bio",
            avatar_url = "http://example.com/avatar.jpg"
        )

        assertEquals(profile1, profile2)
        assertEquals(profile1.hashCode(), profile2.hashCode())
        assertNotEquals(profile1, profile3)
    }

    @Test
    fun testProfileCopy() {
        val original = Profile(
            id = "user1",
            username = "originaluser",
            bio = "Original bio",
            avatar_url = "http://example.com/original.jpg"
        )

        val copied = original.copy(username = "modifieduser")

        assertEquals(original.id, copied.id)
        assertEquals("modifieduser", copied.username)
        assertEquals(original.bio, copied.bio)
        assertEquals(original.avatar_url, copied.avatar_url)
    }

    @Test
    fun testProfileToString() {
        val profile = Profile(
            id = "user1",
            username = "testuser"
        )

        val string = profile.toString()
        assert(string.contains("id=user1"))
        assert(string.contains("username=testuser"))
    }

    @Test
    fun testProfileWithEmptyStrings() {
        val profile = Profile(
            id = "user1",
            username = "testuser",
            bio = "",
            avatar_url = ""
        )

        assertEquals("", profile.bio)
        assertEquals("", profile.avatar_url)
    }
}
