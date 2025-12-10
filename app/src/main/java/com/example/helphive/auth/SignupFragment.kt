package com.example.helphive.auth

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
import com.example.helphive.databinding.FragmentSignupBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()


            if (validateInput(name, email, password)) {
                viewModel.signUp(email, password, name)
            }
        }

        observeViewModel()
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                false
            }
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

    // In SignupFragment.kt - update the signup success handling
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state.isLoading) {
                    binding.btnSignUp.text = "Creating account..."
                    binding.btnSignUp.isEnabled = false
                } else {
                    binding.btnSignUp.text = "Sign Up"
                    binding.btnSignUp.isEnabled = true
                }

                state.error?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }

                // If profile is needed after signup, navigate to profile creation
                if (state.needsProfile && !state.isLoggedIn) {
                    // Stay on auth screen but switch to profile creation
                    // The AuthActivity will handle this
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}