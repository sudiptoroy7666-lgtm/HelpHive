package com.example.helphive.ui.kindness

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.databinding.ActivityKindnessBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.helphive.data.firebase.RealtimeDbService
import kotlinx.coroutines.launch

@AndroidEntryPoint
class KindnessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKindnessBinding
    private val viewModel: KindnessViewModel by viewModels()
    private lateinit var kindnessAdapter: KindnessAdapter

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var realtimeDbService: RealtimeDbService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKindnessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        kindnessAdapter = KindnessAdapter(realtimeDbService)
        binding.rvKindnessFeed.apply {
            layoutManager = LinearLayoutManager(this@KindnessActivity)
            adapter = kindnessAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddKindness.setOnClickListener {
            val text = binding.etKindness.text.toString().trim()
            val userId = authService.getCurrentUser()?.uid ?: return@setOnClickListener

            if (text.isNotEmpty()) {
                viewModel.addKindness(text, userId)
                binding.etKindness.text?.clear()
            } else {
                Toast.makeText(this, "Please enter a kindness act", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    kindnessAdapter.submitList(state.kindnessFeed.reversed())

                    state.error?.let {
                        Toast.makeText(this@KindnessActivity, it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}