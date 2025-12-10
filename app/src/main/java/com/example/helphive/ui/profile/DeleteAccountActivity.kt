package com.example.helphive.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.helphive.R
import com.example.helphive.databinding.ActivityDeleteAccountBinding
import com.example.helphive.databinding.DialogPasswordConfirmationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.auth.AuthActivity
import android.content.Intent

@AndroidEntryPoint
class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private val viewModel: DeleteAccountViewModel by viewModels()

    @Inject
    lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_account_confirmation))
            .setMessage(getString(R.string.delete_account_warning))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                showPasswordInputDialog()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPasswordInputDialog() {
        val dialogBinding = DialogPasswordConfirmationBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_password))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                val password = dialogBinding.etPassword.text.toString().trim()
                if (password.isNotEmpty()) {
                    deleteAccount(password)
                } else {
                    Toast.makeText(this, getString(R.string.password_required), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAccount(password: String) {
        val currentUserId = authService.getCurrentUser()?.uid
        if (currentUserId != null) {
            viewModel.deleteAccount(currentUserId, password)
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state.isLoading) {
                    // Show loading if needed
                    binding.btnDeleteAccount.isEnabled = false
                    binding.btnDeleteAccount.text = "Deleting..."
                } else {
                    binding.btnDeleteAccount.isEnabled = true
                    binding.btnDeleteAccount.text = getString(R.string.delete_account_confirmation)
                }

                state.error?.let { error ->
                    Toast.makeText(this@DeleteAccountActivity, error, Toast.LENGTH_LONG).show()
                }

                if (state.isAccountDeleted) {
                    Toast.makeText(this@DeleteAccountActivity, getString(R.string.account_deleted_successfully), Toast.LENGTH_LONG).show()
                    // Navigate to AuthActivity and finish
                    startActivity(Intent(this@DeleteAccountActivity, AuthActivity::class.java))
                    finish()
                }
            }
        }
    }
}