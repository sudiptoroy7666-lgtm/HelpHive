// File: com/example/helphive/ui/help/HelpActivity.kt
package com.example.helphive.ui.help

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.databinding.ActivityHelpBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.helphive.data.firebase.RealtimeDbService
import com.example.helphive.data.model.HelpRequest
import com.example.helphive.ui.chat.ConversationActivity
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding
    private val viewModel: HelpViewModel by viewModels()
    private lateinit var helpAdapter: HelpAdapter
    private var selectedImageBitmap: Bitmap? = null

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var realtimeDbService: RealtimeDbService

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            uri?.let { imageUri ->
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                selectedImageBitmap = bitmap
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    // In HelpActivity.kt - update the adapter click handler
    // In HelpActivity.kt
    private fun setupRecyclerView() {
        helpAdapter = HelpAdapter(realtimeDbService) { helpRequest ->
            val currentUserId = authService.getCurrentUser()?.uid ?: return@HelpAdapter
            if (helpRequest.userId == currentUserId) {
                // Show options for own requests
                showHelpRequestOptions(helpRequest)
            } else {
                // Navigate to conversation activity for others
                val intent = Intent(this, ConversationActivity::class.java)
                intent.putExtra("REQUEST_ID", helpRequest.requestId)
                startActivity(intent)
            }
        }

        // Apply both layoutManager and adapter to the RecyclerView
        binding.rvHelpRequests.apply { // Use the correct RecyclerView ID from your layout
            layoutManager = LinearLayoutManager(this@HelpActivity) // Set the LayoutManager
            adapter = helpAdapter // Set the adapter
        }
    }

    private fun showHelpRequestOptions(helpRequest: HelpRequest) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Help Request Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Edit
                        val intent = Intent(this, EditHelpActivity::class.java)
                        intent.putExtra("HELP_REQUEST", helpRequest)
                        startActivity(intent)
                    }
                    1 -> { // Delete
                        showDeleteConfirmationDialog(helpRequest)
                    }
                }
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(helpRequest: HelpRequest) {
        AlertDialog.Builder(this)
            .setTitle("Delete Help Request")
            .setMessage("Are you sure you want to delete this help request?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteHelpRequest(helpRequest.requestId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupClickListeners() {
        binding.btnAddHelpRequest.setOnClickListener {
            val title = binding.etHelpTitle.text.toString().trim()
            val description = binding.etHelpDescription.text.toString().trim()
            val userId = authService.getCurrentUser()?.uid ?: return@setOnClickListener

            if (title.isNotEmpty() && description.isNotEmpty()) {
                val requestId = java.util.UUID.randomUUID().toString()
                val imagePath = if (selectedImageBitmap != null) "help_request/${requestId}" else ""

                viewModel.addHelpRequest(title, description, userId, imagePath)

                if (selectedImageBitmap != null) {
                    val base64String = com.example.helphive.core.utils.Base64Utils.bitmapToBase64(selectedImageBitmap!!)
                    realtimeDbService.uploadImage(imagePath, base64String)
                }

                binding.etHelpTitle.text?.clear()
                binding.etHelpDescription.text?.clear()
                selectedImageBitmap = null
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }
    }

    // In HelpActivity.kt
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Log the number of requests received by the Activity
                    android.util.Log.d("HelpActivity", "Activity received ${state.helpRequests.size} requests from ViewModel. Loading: ${state.isLoading}")

                    // Ensure the adapter is set before trying to submit data
                    if (binding.rvHelpRequests.adapter == null) {
                        android.util.Log.w("HelpActivity", "Adapter was null, setting it now. This should ideally happen in setupRecyclerView.")
                        binding.rvHelpRequests.adapter = helpAdapter // Redundant check, but safe
                    }

                    // Submit the list to the adapter
                    helpAdapter.submitList(state.helpRequests.reversed())

                    // Log the adapter's item count after submission
                    android.util.Log.d("HelpActivity", "Adapter now has ${helpAdapter.itemCount} items after submitList")

                    state.error?.let { error ->
                        android.util.Log.e("HelpActivity", "ViewModel reported error: $error")
                        Toast.makeText(this@HelpActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}