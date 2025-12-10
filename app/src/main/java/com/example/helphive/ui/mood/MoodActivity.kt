package com.example.helphive.ui.mood

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helphive.R
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.databinding.ActivityMoodBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    private val viewModel: MoodViewModel by viewModels()
    private lateinit var moodAdapter: MoodAdapter

    @Inject
    lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        val userId = authService.getCurrentUser()?.uid ?: return
        viewModel.loadUserMoods(userId)
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter()
        binding.rvMoodList.apply {
            layoutManager = LinearLayoutManager(this@MoodActivity)
            adapter = moodAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectEmoji.setOnClickListener {
            // Simple emoji picker - in real app, use a proper emoji picker
            val emojis = listOf("😊", "😢", "😠", "😴", "😍", "🤔", "😎", "🥳")
            val randomEmoji = emojis.random()
            binding.tvEmoji.text = randomEmoji
        }

        binding.btnSaveMood.setOnClickListener {
            val emoji = binding.tvEmoji.text.toString()
            val note = binding.etNote.text.toString().trim()
            val userId = authService.getCurrentUser()?.uid ?: return@setOnClickListener

            if (emoji.isNotEmpty()) {
                viewModel.addMood(emoji, note, userId)
                binding.etNote.text?.clear()
            } else {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    moodAdapter.submitList(state.moods.reversed())

                    state.error?.let {
                        Toast.makeText(this@MoodActivity, it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

}