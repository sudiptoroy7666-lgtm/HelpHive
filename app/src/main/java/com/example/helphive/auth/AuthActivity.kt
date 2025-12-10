package com.example.helphive.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.example.helphive.MainActivity
import com.example.helphive.core.utils.PreferencesManager
import com.example.helphive.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.helphive.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 👇 Check auth state BEFORE inflating layout
        checkFirebaseAuthState()
    }

    private fun setupAuthUi() {
        // 👇 Only inflate layout if we need to show auth screen
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        setupViewPager()
        observeViewModel()
    }

    private fun setupViewPager() {
        val adapter = AuthPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Login"
                1 -> "Sign Up"
                else -> "Login"
            }
        }.attach()
    }

    private fun checkFirebaseAuthState() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            // User is logged in → check profile and navigate
            viewModel.checkProfileCompletion(currentUser.uid)
            observeViewModel() // Start observing for navigation
        } else {
            // Not logged in → show auth UI
            setupAuthUi()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state.isLoggedIn) {
                    if (state.needsProfile) {
                        val intent = Intent(this@AuthActivity, ProfileActivity::class.java)
                        intent.putExtra("isProfileCreation", true)
                        startActivity(intent)
                        finish()
                    } else {
                        startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                        finish()
                    }
                } else if (state.showLoginTab) {
                    // Only reachable if UI is shown
                    binding.viewPager.currentItem = 0
                }

                state.error?.let { error ->
                    Toast.makeText(this@AuthActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}