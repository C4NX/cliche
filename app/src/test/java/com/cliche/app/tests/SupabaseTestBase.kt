package com.cliche.app.tests

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.AfterClass
import org.junit.BeforeClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.test.resetMain
import java.util.UUID

abstract class SupabaseTestBase {
    companion object {
        @OptIn(ExperimentalCoroutinesApi::class)
        @BeforeClass
        @JvmStatic
        fun setupAll() {
            Dispatchers.setMain(StandardTestDispatcher())
            SupabaseTestClientProvider.initFromBuildConfig()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @AfterClass
        @JvmStatic
        fun tearDownAll() {
            Dispatchers.resetMain();
            SupabaseTestClientProvider.reset()
        }
    }

    /**
     * Helper function to create a temporary user, sign in, run a test block, and clean up.
     */
    suspend fun withNewAuthenticatedUser(
        email: String = "test-${UUID.randomUUID()}@junit.test",
        password: String = "SecureTestPassword123!",
        block: suspend (client: SupabaseClient) -> Unit
    ) {
        val serviceSupabase = SupabaseTestClientProvider.asService()
        val supabaseClient = SupabaseTestClientProvider.asAnon()

        val tempUser = serviceSupabase.auth.admin.createUserWithEmail {
            this.email = email
            this.autoConfirm = true
            this.password = password
        }

        try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            block(supabaseClient)
        } finally {
            try { supabaseClient.auth.signOut() } catch (_: Exception) { /* Ignore sign out errors */ }

            try {
                serviceSupabase.auth.admin.deleteUser(tempUser.id)
            } catch (e: Exception) {
                println("Warning: Could not delete user ${tempUser.id} during cleanup: ${e.message}")
            }
        }
    }
}