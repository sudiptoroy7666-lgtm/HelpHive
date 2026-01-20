// ui/chat/ChatAdapter.kt
package com.example.helphive.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.ChatMessage
import com.example.helphive.databinding.ItemChatMessageSentBinding
import com.example.helphive.databinding.ItemChatMessageReceivedBinding

class ChatAdapter(private val currentUserId: String) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemChatMessageSentBinding.inflate(inflater, parent, false)
                SentViewHolder(binding)
            }
            else -> {
                val binding = ItemChatMessageReceivedBinding.inflate(inflater, parent, false)
                ReceivedViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentViewHolder -> holder.bind(message)
            is ReceivedViewHolder -> holder.bind(message)
        }
    }

    class SentViewHolder(private val binding: ItemChatMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.apply {
                tvMessage.text = message.message
                tvTime.text = DateUtils.formatChatTimestamp(message.timestamp)
            }
        }
    }

    class ReceivedViewHolder(private val binding: ItemChatMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.apply {
                tvMessage.text = message.message
                tvTime.text = DateUtils.formatChatTimestamp(message.timestamp)
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}