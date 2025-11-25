package com.cliche.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cliche.app.services.auth.AuthManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput);
        val loadingScreen = findViewById<View>(R.id.loadingScreen)
        val loginForm = findViewById<View>(R.id.loginForm)

        // First, wait for auth system to be ready and check if user is already logged in
        lifecycleScope.launch {
            AuthManager.waitToBeReady()

            val restoredUser = AuthManager.getUserOrNull()
            if(restoredUser != null){
                Log.d("LoginActivity", "Auth state restored for user id: ${restoredUser.id}")
                startMainActivity()
            } else {
                Log.d("LoginActivity", "No user restored after auth initialization.")

                // So show login form
                loadingScreen.visibility = View.GONE
                loginForm.visibility = View.VISIBLE
            }
        }

        // In second, set up login button
        loginBtn.setOnClickListener {
            loginBtn.isEnabled = false
            emailInput.isEnabled = false
            passwordInput.isEnabled = false

            lifecycleScope.launch {
                try {
                    val email = emailInput.text.toString()
                    val password = passwordInput.text.toString()

                    AuthManager.signInWithEmail(email, password)
                    startMainActivity()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    loginBtn.isEnabled = true
                    emailInput.isEnabled = true
                    passwordInput.isEnabled = true
                }
            }
        }
    }

    /**
     * Starts the MainActivity and finishes the current LoginActivity.
     */
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // prevents returning to login screen with back button
    }
}
