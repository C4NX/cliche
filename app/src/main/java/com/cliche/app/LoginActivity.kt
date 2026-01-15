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
import com.cliche.app.utils.setVisible
import kotlinx.coroutines.launch

class LoginActivity: AppCompatActivity() {

    private val loginBtn: Button by lazy { findViewById(R.id.loginBtn) }
    private val registerBtn: Button by lazy { findViewById(R.id.registerBtn) }
    private val emailInput: EditText by lazy { findViewById(R.id.emailInput) }
    private val passwordInput: EditText by lazy { findViewById(R.id.passwordInput) }
    private val loadingScreen: View by lazy { findViewById(R.id.loadingScreen) }
    private val loginForm: View by lazy { findViewById(R.id.loginForm) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

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
                loadingScreen.setVisible(false)
                loginForm.setVisible(true)
            }
        }

        registerBtn.setOnClickListener {
            setFormLoadingState(true)

            lifecycleScope.launch {
                try {
                    val email = emailInput.text.toString()
                    val password = passwordInput.text.toString()

                    AuthManager.registerWithEmail(email, password)
                    startMainActivity()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    setFormLoadingState(false)
                }
            }
        }

        // In second, set up login button
        loginBtn.setOnClickListener {
            setFormLoadingState(true)

            lifecycleScope.launch {
                try {
                    val email = emailInput.text.toString()
                    val password = passwordInput.text.toString()

                    AuthManager.signInWithEmail(email, password)
                    startMainActivity()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    setFormLoadingState(false)
                }
            }
        }
    }

    /**
     * Sets the loading state of the login form.
     * When loading, shows the loading screen and hides the login form.
     * When not loading, shows the login form and hides the loading screen.
     *
     * @param isLoading Boolean indicating whether the form is in loading state.
     *
     * @return Unit
     */
    private fun setFormLoadingState(isLoading: Boolean) {
        if (isLoading) {
            loadingScreen.setVisible(true)
            loginForm.setVisible(false)
        } else {
            loadingScreen.setVisible(false)
            loginForm.setVisible(true)
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
