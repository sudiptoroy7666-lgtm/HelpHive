package com.example.helphive.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.databinding.ActivityConversationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private val viewModel: ConversationViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    @Inject
    lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestId = intent.getStringExtra("REQUEST_ID") ?: run {
            finish()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Start real-time listening for messages
        viewModel.startListeningForMessages(requestId)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvChatMessages.apply {
            layoutManager = LinearLayoutManager(this@ConversationActivity)
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSendMessage.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            val requestId = intent.getStringExtra("REQUEST_ID") ?: return@setOnClickListener
            val senderId = authService.getCurrentUser()?.uid ?: return@setOnClickListener

            if (message.isNotEmpty()) {
                viewModel.sendMessage(requestId, message, senderId)
                binding.etMessage.text?.clear()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.messagesState.collect { state ->
                // Update the adapter with new messages
                chatAdapter.submitList(state.messages)

                // Scroll to bottom when new messages arrive
                if (state.messages.isNotEmpty()) {
                    binding.rvChatMessages.scrollToPosition(chatAdapter.itemCount - 1)
                }

                state.error?.let { error ->
                    Toast.makeText(this@ConversationActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}