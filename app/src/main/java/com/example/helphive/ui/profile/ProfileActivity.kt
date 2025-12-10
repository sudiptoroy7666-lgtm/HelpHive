package com.example.helphive.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.example.helphive.core.utils.Base64Utils
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.databinding.ActivityProfileBinding
import com.example.helphive.ui.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.helphive.auth.AuthActivity
import com.example.helphive.MainActivity
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null

    @Inject
    lateinit var authService: AuthService

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            uri?.let { imageUri ->
                // Load bitmap in background thread
                lifecycleScope.launch {
                    val bitmap = try {
                        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    } catch (e: Exception) {
                        null
                    }

                    if (bitmap != null) {
                        selectedImageBitmap = bitmap
                        // Update UI on main thread
                        runOnUiThread {
                            binding.ivProfile.setImageBitmap(bitmap)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isProfileCreation = intent.getBooleanExtra("isProfileCreation", false)
        setupClickListeners(isProfileCreation)
        observeViewModel(isProfileCreation)

        // Load user data if available
        val userId = authService.getCurrentUser()?.uid
        if (userId != null) {
            viewModel.loadUser(userId)
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    private fun setupClickListeners(isProfileCreation: Boolean) {
        binding.btnEditProfile.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()
            val userId = authService.getCurrentUser()?.uid ?: return@setOnClickListener

            if (!validateProfileInput(name, address, gender)) return@setOnClickListener

            val base64String = if (selectedImageBitmap != null) {
                Base64Utils.bitmapToBase64(selectedImageBitmap!!)
            } else {
                ""
            }

            viewModel.createOrUpdateProfile(userId, name, address, gender, base64String, isProfileCreation)
        }

        binding.ivProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        binding.btnSignOut.setOnClickListener {
            authService.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun validateProfileInput(name: String, address: String, gender: String): Boolean {
        return when {
            name.isEmpty() -> {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                false
            }
            address.isEmpty() -> {
                Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show()
                false
            }
            gender.isEmpty() -> {
                Toast.makeText(this, "Gender is required", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun observeViewModel(isProfileCreation: Boolean) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update profile fields
                    state.user?.let { user ->
                        // Only update if fields are empty or we want to refresh
                        if (binding.etName.text.toString().trim().isEmpty() && user.name.isNotEmpty()) {
                            binding.etName.setText(user.name)
                        }
                        if (binding.etAddress.text.toString().trim().isEmpty() && user.address.isNotEmpty()) {
                            binding.etAddress.setText(user.address)
                        }
                        if (binding.etGender.text.toString().trim().isEmpty() && user.gender.isNotEmpty()) {
                            binding.etGender.setText(user.gender)
                        }
                    }

                    // Update button state
                    binding.btnEditProfile.apply {
                        text = if (state.isLoading) {
                            if (isProfileCreation) "Creating Profile..." else "Updating Profile..."
                        } else {
                            if (isProfileCreation) "Complete Profile" else "Update Profile"
                        }
                        isEnabled = !state.isLoading
                    }

                    // Handle errors
                    state.error?.let { error ->
                        Toast.makeText(this@ProfileActivity, error, Toast.LENGTH_LONG).show()
                    }

                    // Handle success
                    if (state.updateSuccess) {
                        Toast.makeText(this@ProfileActivity, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                        finish()
                    }

                    // Handle profile image loading
                    state.profileImageBitmap?.let { bitmap ->
                        binding.ivProfile.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear bitmap to prevent memory leaks
        selectedImageBitmap?.recycle()
        viewModel.clearImageBitmap()
    }
}