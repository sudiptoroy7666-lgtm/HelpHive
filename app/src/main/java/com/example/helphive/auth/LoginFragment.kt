package com.example.helphive.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.helphive.R
import com.example.helphive.core.utils.PreferencesManager
import com.example.helphive.core.utils.ValidationUtils
import com.example.helphive.databinding.FragmentLoginBinding
import androidx.lifecycle.lifecycleScope
import com.example.helphive.MainActivity
import com.example.helphive.ui.profile.ProfileActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())

        // Setup "Remember Me" checkbox
        setupRememberMe()


        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val rememberMe = binding.cbRememberMe.isChecked

            if (validateInput(email, password)) {
                viewModel.signIn(email, password, rememberMe, preferencesManager)
            }
        }


        binding.forgotPassword.setOnClickListener {
            // Navigate to ForgotPasswordActivity
            val intent = Intent(requireContext(), ForgotPasswordActivity::class.java)
            startActivity(intent)
        }


        observeViewModel()
    }



    private fun setupRememberMe() {
        // Load saved email if remember me was enabled
        if (preferencesManager.isRememberMeEnabled()) {
            val savedEmail = preferencesManager.getSavedEmail()
            binding.etEmail.setText(savedEmail)
            binding.cbRememberMe.isChecked = true
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            !ValidationUtils.isValidEmail(email) -> {
                Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                false
            }
            !ValidationUtils.isValidPassword(password) -> {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state.isLoading) {
                    binding.btnLogin.text = "Logging in..."
                    binding.btnLogin.isEnabled = false
                } else {
                    binding.btnLogin.text = "Login"
                    binding.btnLogin.isEnabled = true
                }

                state.error?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}