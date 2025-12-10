package com.example.helphive

import com.google.firebase.firestore.firestoreSettings
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import com.example.helphive.databinding.ActivityMainBinding
import com.example.helphive.ui.chat.ChatActivity
import com.example.helphive.ui.help.HelpActivity
import com.example.helphive.ui.home.HomeViewModel
import com.example.helphive.ui.kindness.KindnessActivity
import com.example.helphive.ui.mood.MoodActivity
import com.example.helphive.ui.mood.MoodStatisticsActivity
import com.example.helphive.ui.profile.ProfileActivity
import com.example.helphive.auth.AuthActivity
import com.example.helphive.data.firebase.AuthService
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.helphive.ui.profile.DeleteAccountActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    @Inject
    lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is logged in
        if (!authService.isUserLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setupNavigationDrawer()
        setupClickListeners()
        observeViewModel()
        setupBackHandler()
    // <-- add the OnBackPressedDispatcher callback



        binding.btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)



    }

    private fun setupNavigationDrawer() {
        drawerLayout = binding.drawerLayout
        navView = binding.navView

        // Setup toolbar
        setSupportActionBar(binding.toolbar)

        // Setup drawer toggle
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation item clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_sign_out -> {
                    viewModel.signOut()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_delete_account -> {
                    startActivity(Intent(this, DeleteAccountActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }
    }




    private fun setupClickListeners() {
        binding.btnMood.setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
        }

        binding.btnMoodStats.setOnClickListener {
            startActivity(Intent(this, MoodStatisticsActivity::class.java))
        }

        binding.btnKindness.setOnClickListener {
            startActivity(Intent(this, KindnessActivity::class.java))
        }

        binding.btnHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    if (state.isSignedOut) {
                        startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    private fun setupBackHandler() {
        // Register lifecycle-aware back callback
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If drawer is open, close it. Otherwise delegate to system default.
                if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Allow system to handle the back (will finish the activity or navigate up)
                    // Temporarily disable this callback and call onBackPressedDispatcher again
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Remove override fun onBackPressed() — handled via OnBackPressedDispatcher now
}
