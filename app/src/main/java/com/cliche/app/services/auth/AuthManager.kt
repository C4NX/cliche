package com.cliche.app.services.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.cliche.app.LoginActivity
import com.cliche.app.modules.supabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

/**
 * Manages user authentication.
 */
object AuthManager {
    /**
     * Performs classic email/password login.
     */
    suspend fun signInWithEmail(email: String, password: String) {
        Log.d("AuthManager", "Attempting to login with email: $email")
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val currentUser = supabaseClient.auth.currentUserOrNull()
        if (currentUser != null) {
            Log.d("AuthManager", "Login successful for user id: ${currentUser.id}")
        } else {
            Log.e("AuthManager", "Login failed: currentUser is null after signInWithEmail")
            throw Exception("Login failed: unknown error occurred.")
        }
    }

    /**
     * Signs out the currently logged-in user.
     */
    suspend fun signOut(context: Context) {
        supabaseClient.auth.signOut()
        Log.d("AuthManager", "User signed out")
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    /**
     * Waits for the Supabase auth system to be initialized.
     */
    suspend fun waitToBeReady() = supabaseClient.auth.awaitInitialization()

    /**
     * Returns the currently logged-in user, or null if no user is logged in.
     */
    fun getUserOrNull() = supabaseClient.auth.currentUserOrNull()
}