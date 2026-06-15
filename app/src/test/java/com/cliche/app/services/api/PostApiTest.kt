package com.cliche.app.services.api

import com.cliche.app.services.auth.AuthManager
import com.cliche.app.tests.SupabaseTestBase
import com.cliche.app.tests.SupabaseTestClientProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PostApiTest : SupabaseTestBase() {
    @Test
    fun `should return posts from seed when fetching timeline as anonymous`() {
        runBlocking {
            val supabaseClient = SupabaseTestClientProvider.asAnon()
            val postApi = PostApi(supabaseClient, AuthManager(supabaseClient))
            val result = postApi.fetchTimeline(0, 0)
            Assert.assertEquals(1, result.size) // from seed.sql
        }
    }

    @Test
    fun `should return post with matching id and owner when fetching by id as anonymous`() {
        runBlocking {
            val supabaseClient = SupabaseTestClientProvider.asAnon()
            val postApi = PostApi(supabaseClient, AuthManager(supabaseClient))
            val result = postApi.fetchPostById(1)
            Assert.assertNotNull(result)
            Assert.assertEquals(1, result!!.id)
            Assert.assertEquals("11111111-1111-1111-1111-111111111111", result.owner_id) // from seed.sql
        }
    }

    @Test
    fun `should return null when fetching non-existing post as anonymous`() {
        runBlocking {
            val supabaseClient = SupabaseTestClientProvider.asAnon()
            val postApi = PostApi(supabaseClient, AuthManager(supabaseClient))
            val result = postApi.fetchPostById(1000)
            Assert.assertNull(result)
        }
    }

    @Test
    fun `should return empty list when fetching bookmarked as anonymous`() {
        runBlocking {
            val supabaseClient = SupabaseTestClientProvider.asAnon()
            val postApi = PostApi(supabaseClient, AuthManager(supabaseClient))
            val result = postApi.fetchBookmarked(0, 0)
            Assert.assertEquals(0, result.size) // anon always return 0
        }
    }

    @Test
    fun `should return bookmarked post when bookmarking and fetching bookmarked`() = runBlocking {
        withNewAuthenticatedUser { supabaseClient ->
            val postApi = PostApi(supabaseClient, AuthManager(supabaseClient))
            postApi.bookmark(1)
            val result = postApi.fetchBookmarked(0, 0)
            Assert.assertEquals(1, result.size)
        }
    }
}