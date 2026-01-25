package com.cliche.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import android.view.View
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cliche.app.databinding.ActivityMainBinding
import com.cliche.app.modules.supabaseClient
import com.cliche.app.services.auth.AuthManager
import com.cliche.app.settings.SettingsActivity
import com.cliche.app.ui.posts.PostFragment
import com.google.android.material.appbar.MaterialToolbar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val REQUEST_POST_NOTIFICATIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TopAppBar as main AppBar
        val toolbar: MaterialToolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar)

        // NavBar/Drawer
        val bottomNavView = findViewById<BottomNavigationView?>(R.id.nav_view)
        val drawerNavView = findViewById<NavigationView?>(R.id.drawer_nav_view)
        val containerView = findViewById<View?>(R.id.container)
        val drawerLayout = containerView as? androidx.drawerlayout.widget.DrawerLayout

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        this.navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val topLevel = setOf(
            R.id.navigation_home, R.id.navigation_map, R.id.navigation_notifications
        )
        this.appBarConfiguration = if (drawerLayout != null) {
            AppBarConfiguration(topLevel, drawerLayout)
        } else {
            AppBarConfiguration(topLevel)
        }

        // Setup ActionBar, BottomNavView and DrawerNavView with NavController
        setupActionBarWithNavController(this.navController, this.appBarConfiguration)
        bottomNavView?.setupWithNavController(this.navController)
        drawerNavView?.setupWithNavController(this.navController)

        // Show a '+' on the top-left (navigation icon) for top-level screens
        // Only override the navigation icon to '+' when there's no drawer.
        if (drawerLayout == null) {
            val topLevelDestinations = topLevel
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (topLevelDestinations.contains(destination.id)) {
                    val icon = ContextCompat.getDrawable(this, R.drawable.material_symbols__add_a_photo_rounded)
                    if (icon != null) {
                        DrawableCompat.setTint(icon, ContextCompat.getColor(this, android.R.color.white))
                    }
                    toolbar.navigationIcon = icon
                    toolbar.navigationContentDescription = getString(R.string.add_post_title)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.navigation_add_post)
                    }
                } else {
                    // For non top-level screens, keep default back/up behavior
                    toolbar.navigationContentDescription = null
                    toolbar.setNavigationOnClickListener { onSupportNavigateUp() }
                }
            }
        }

        handleIntentParams(intent)

        lifecycleScope.launch {
            // Observe authentication state (sign-in/sign-out)
            supabaseClient.auth.sessionStatus.collect {
                when (it) {
                    is SessionStatus.NotAuthenticated -> {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                    else -> {
//                        // When user is authenticated
//                        // Check notification permission on Android 13+
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                            val hasPerm = ContextCompat.checkSelfPermission(
//                                this@MainActivity,
//                                Manifest.permission.POST_NOTIFICATIONS
//                            ) == PackageManager.PERMISSION_GRANTED
//
//                            if (!hasPerm) {
//                                ActivityCompat.requestPermissions(
//                                    this@MainActivity,
//                                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                                    REQUEST_POST_NOTIFICATIONS
//                                )
//                            } else {
//                                // Permission already granted
//                                Toast.makeText(this@MainActivity, "Realtime likes enabled", Toast.LENGTH_SHORT).show()
//                                LikesRealtimeService.start(this@MainActivity)
//                            }
//                        } else {
//                            // No runtime permission needed on older Android versions
//                            Toast.makeText(this@MainActivity, "Realtime likes enabled", Toast.LENGTH_SHORT).show()
//                            LikesRealtimeService.start(this@MainActivity)
//                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentParams(intent)
    }

    /**
     * Handle intent parameters for navigation (e.g., from notifications)
     */
    private fun handleIntentParams(intent: Intent) {
        val notifPostId = intent.getLongExtra("postId", -1L)
        if (notifPostId > 0L) {
            val args = Bundle().apply { putLong(PostFragment.ARG_POST_ID, notifPostId) }
            navController.navigate(R.id.navigation_post, args)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                Toast.makeText(this, "Notifications permission granted — realtime likes enabled", Toast.LENGTH_SHORT).show()
//                // Start the listener after permission is granted
//                lifecycleScope.launch {
//                    LikesRealtimeService.start(this@MainActivity)
//                }
            } else {
                Toast.makeText(this, "Notifications disabled — likes listener still runs without alerts", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                navController.navigate(R.id.navigation_profile)
                true
            }
            R.id.bookmarks -> {
                navController.navigate(R.id.navigation_bookmarks)
                true
            }
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.logout -> {
                lifecycleScope.launch {
                    AuthManager.signOut()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
