package com.cliche.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
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
import com.google.android.material.appbar.MaterialToolbar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TopAppBar as main AppBar
        val toolbar: MaterialToolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar)

        // NavBar/Drawer management
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

        lifecycleScope.launch {
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
                        // When user is authenticated
                    }
                }
            }
        }
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

    // Ensure Up button works and is delegated to NavController
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
