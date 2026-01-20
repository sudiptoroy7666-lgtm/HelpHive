// In MoodActivity.kt
package com.example.helphive.ui.mood
import android.widget.TextView
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.core.content.ContextCompat
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
import java.util.Random

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

    private fun showSupportiveMessage(message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_supportive_message)

        // ✅ CRITICAL: Set proper window attributes
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val tvMessage = dialog.findViewById<TextView>(R.id.tvSupportiveMessage)
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)

        tvMessage.text = message

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            viewModel.clearSupportiveMessage()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        moodAdapter.submitList(state.moods.reversed())
                        state.error?.let {
                            Toast.makeText(this@MoodActivity, it, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                launch {
                    viewModel.supportiveMessage.collect { message ->
                        message?.let {
                            showSupportiveMessage(it)
                        }
                    }
                }
            }
        }
    }
}