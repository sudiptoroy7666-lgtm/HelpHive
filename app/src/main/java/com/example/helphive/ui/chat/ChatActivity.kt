// File: com/example/helphive/ui/chat/ChatActivity.kt
package com.example.helphive.ui.chat

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.data.firebase.RealtimeDbService
import com.example.helphive.databinding.ActivityChatBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var conversationAdapter: ConversationAdapter

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var realtimeDbService: RealtimeDbService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        val userId = authService.getCurrentUser()?.uid ?: return
        viewModel.loadChatConversations(userId)
    }

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(
            realtimeDbService = realtimeDbService
        ) { conversation ->
            val intent = Intent(this, ConversationActivity::class.java)
            intent.putExtra("REQUEST_ID", conversation.requestId)
            startActivity(intent)
        }
        binding.rvConversations.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = conversationAdapter
        }
    }

    private fun observeViewModel() {
        // Collect StateFlow in a coroutine
        lifecycleScope.launch {
            viewModel.conversationsState.collectLatest { state ->
                // Show/hide loading indicator based on state.isLoading
                // Assuming you have a ProgressBar in your layout with id 'progressBar'
                // binding.progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE

                conversationAdapter.submitList(state.conversations)

                state.error?.let {
                    Toast.makeText(this@ChatActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}