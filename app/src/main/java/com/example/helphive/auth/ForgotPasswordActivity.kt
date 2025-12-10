// File: com/example/helphive/auth/ForgotPasswordActivity.kt
package com.example.helphive.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.helphive.core.utils.ValidationUtils
import com.example.helphive.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint // This is crucial
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint // This annotation is required
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (validateInput(email)) {
                viewModel.sendPasswordResetEmail(email)
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(email: String): Boolean {
        return when {
            !ValidationUtils.isValidEmail(email) -> {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state.isLoading) {
                    binding.btnResetPassword.text = "Sending..."
                    binding.btnResetPassword.isEnabled = false
                } else {
                    binding.btnResetPassword.text = "Reset Password"
                    binding.btnResetPassword.isEnabled = true
                }

                state.error?.let { error ->
                    Toast.makeText(this@ForgotPasswordActivity, error, Toast.LENGTH_LONG).show()
                }

                if (state.isEmailSent) {
                    Toast.makeText(this@ForgotPasswordActivity,
                        "Password reset email sent. Check your inbox.",
                        Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}