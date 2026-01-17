package com.cliche.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cliche.app.services.auth.AuthManager
import com.cliche.app.utils.setVisible
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class LoginActivity: AppCompatActivity() {
    val TAG = "LoginActivity"

    private val loginBtn: Button by lazy { findViewById(R.id.loginBtn) }
    private val registerBtn: Button by lazy { findViewById(R.id.registerBtn) }
    private val googleBtn: Button by lazy { findViewById(R.id.googleBtn) }
    private val emailInput: EditText by lazy { findViewById(R.id.emailInput) }
    private val passwordInput: EditText by lazy { findViewById(R.id.passwordInput) }
    private val usernameInput: EditText by lazy { findViewById(R.id.usernameInput) }
    private val loadingScreen: View by lazy { findViewById(R.id.loadingScreen) }
    private val loginForm: View by lazy { findViewById(R.id.loginForm) }
    private val authTabs: TabLayout by lazy { findViewById(R.id.authTabs) }
    private val formProgress: ProgressBar by lazy { findViewById(R.id.formProgress) }
    private val versionTextView: TextView by lazy { findViewById(R.id.appVersion) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        setupAuthTabs()
        setupLoginRegisterForm()
        setupAppVersion()

        // Wait for authentication manager to be ready
        lifecycleScope.launch {
            AuthManager.waitToBeReady()

            val restoredUser = AuthManager.getUserOrNull()
            if(restoredUser != null){
                Log.d(TAG, "Auth state restored for user id: ${restoredUser.id}")
                closeAndStartMainActivity()
            } else {
                Log.d(TAG, "No user restored after auth initialization.")

                // So show login form; hide global loading scrim
                loadingScreen.setVisible(false)
                loginForm.setVisible(true)
            }
        }
    }

    /**
     * Sets up the login and registration form.
     */
    private fun setupLoginRegisterForm() {
        // First, set up register button
        registerBtn.setOnClickListener {
            updateLoadingState(true)

            lifecycleScope.launch {
                try {
                    val email = emailInput.text.toString()
                    val password = passwordInput.text.toString()
                    val username = usernameInput.text.toString()

                    AuthManager.register(email, password, username)
                    closeAndStartMainActivity()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.toast_register_failed, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    updateLoadingState(false)
                }
            }
        }

        // Then, set up login button
        loginBtn.setOnClickListener {
            updateLoadingState(true)

            lifecycleScope.launch {
                try {
                    val email = emailInput.text.toString()
                    val password = passwordInput.text.toString()

                    AuthManager.login(email, password)
                    closeAndStartMainActivity()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.toast_login_failed, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    updateLoadingState(false)
                }
            }
        }

        // Finally, set up Google login button
        googleBtn.setOnClickListener {
            updateLoadingState(true)

            lifecycleScope.launch {
                try {
                    AuthManager.loginWithGoogle(this@LoginActivity)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.toast_google_login_failed, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    updateLoadingState(false)
                }
            }
        }
    }

    /**
     * Sets up the authentication tabs for Login and Register.
     */
    private fun setupAuthTabs() {
        authTabs.addTab(authTabs.newTab().setText(getString(R.string.activity_login_tab_login)))
        authTabs.addTab(authTabs.newTab().setText(getString(R.string.activity_login_tab_register)))

        authTabs.getTabAt(0)?.select()

        usernameInput.visibility = View.GONE
        loginBtn.visibility = View.VISIBLE
        registerBtn.visibility = View.GONE

        authTabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isRegister = (tab?.position == 1)
                usernameInput.visibility = if (isRegister) View.VISIBLE else View.GONE
                registerBtn.visibility = if (isRegister) View.VISIBLE else View.GONE
                loginBtn.visibility = if (isRegister) View.GONE else View.VISIBLE
                googleBtn.visibility = if (isRegister) View.GONE else View.VISIBLE
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * Displays the application version based on the Git tag name.
     */
    private fun setupAppVersion() {
        val gitTagName = BuildConfig.TAG_NAME
        versionTextView.setText(gitTagName)
    }

    /**
     * Sets the loading state of the login form.
     */
    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) {
            formProgress.visibility = View.VISIBLE
            emailInput.isEnabled = false
            passwordInput.isEnabled = false
            usernameInput.isEnabled = false
            loginBtn.isEnabled = false
            registerBtn.isEnabled = false
            googleBtn.isEnabled = false
        } else {
            formProgress.visibility = View.GONE
            emailInput.isEnabled = true
            passwordInput.isEnabled = true
            usernameInput.isEnabled = true
            loginBtn.isEnabled = true
            registerBtn.isEnabled = true
            googleBtn.isEnabled = true
        }
    }

    /**
     * Starts the MainActivity and finishes the current LoginActivity.
     */
    private fun closeAndStartMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // prevents returning to login screen with back button
    }
}
