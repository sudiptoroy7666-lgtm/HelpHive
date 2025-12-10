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
import androidx.appcompat.app.AppCompatActivity
import com.example.helphive.core.utils.Base64Utils
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.data.model.HelpRequest
import com.example.helphive.databinding.ActivityEditHelpBinding
import com.example.helphive.ui.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditHelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditHelpBinding
    private val viewModel: HelpViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null

    @Inject
    lateinit var authService: AuthService

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            uri?.let { imageUri ->
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                selectedImageBitmap = bitmap
                binding.ivHelpImage.setImageBitmap(bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val helpRequest: HelpRequest? = intent.getParcelableExtra("HELP_REQUEST")

        if (helpRequest != null) {
            binding.etHelpTitle.setText(helpRequest.title)
            binding.etHelpDescription.setText(helpRequest.description)
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ivHelpImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.btnUpdateHelp.setOnClickListener {
            val title = binding.etHelpTitle.text.toString().trim()
            val description = binding.etHelpDescription.text.toString().trim()
            val helpRequest = intent.getParcelableExtra<HelpRequest>("HELP_REQUEST")

            if (helpRequest != null && title.isNotEmpty() && description.isNotEmpty()) {
                if (selectedImageBitmap != null) {
                    val base64String = Base64Utils.bitmapToBase64(selectedImageBitmap!!)
                    // Update with image
                    viewModel.updateHelpRequest(helpRequest.copy(
                        title = title,
                        description = description,
                        imagePath = "help_request/${helpRequest.requestId}"
                    ), base64String)
                } else {
                    // Update without image
                    viewModel.updateHelpRequest(helpRequest.copy(
                        title = title,
                        description = description
                    ), "")
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}