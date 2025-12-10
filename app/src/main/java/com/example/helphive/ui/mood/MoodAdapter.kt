package com.example.helphive.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.Mood
import com.example.helphive.databinding.ItemMoodBinding

class MoodAdapter : ListAdapter<Mood, MoodAdapter.MoodViewHolder>(MoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MoodViewHolder(private val binding: ItemMoodBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: Mood) {
            binding.apply {
                tvEmoji.text = mood.emoji
                tvNote.text = mood.note.takeIf { it.isNotEmpty() } ?: "No note"
                tvDate.text = DateUtils.formatTimestamp(mood.timestamp)
            }
        }
    }

    class MoodDiffCallback : DiffUtil.ItemCallback<Mood>() {
        override fun areItemsTheSame(oldItem: Mood, newItem: Mood): Boolean {
            return oldItem.moodId == newItem.moodId
        }

        override fun areContentsTheSame(oldItem: Mood, newItem: Mood): Boolean {
            return oldItem == newItem
        }
    }
}