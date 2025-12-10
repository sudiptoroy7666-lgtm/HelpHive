// Updated ConversationAdapter
package com.example.helphive.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphive.R
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.firebase.ChatConversation
import com.example.helphive.databinding.ItemConversationBinding
import com.example.helphive.data.firebase.RealtimeDbService
import javax.inject.Inject

class ConversationAdapter(
    private val realtimeDbService: RealtimeDbService,
    private val onItemClick: (ChatConversation) -> Unit
) : ListAdapter<ChatConversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(binding, realtimeDbService, onItemClick)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ConversationViewHolder(
        private val binding: ItemConversationBinding,
        private val realtimeDbService: RealtimeDbService,
        private val onItemClick: (ChatConversation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // In ConversationAdapter.kt - inside bind() method
        fun bind(conversation: ChatConversation) {
            binding.apply {
                tvUserName.text = conversation.otherUserName
                tvLastMessage.text = conversation.lastMessage
                // ✅ FIXED: Use conversation-specific timestamp formatter
                tvTime.text = DateUtils.formatConversationTimestamp(conversation.lastMessageTime)

                // Load user profile image if available
                if (conversation.otherUserId.isNotEmpty()) {
                    loadImage(ivUserImage, "profile/${conversation.otherUserId}")
                }

                if (conversation.unreadCount > 0) {
                    // Show unread count indicator
                    // You can add an unread count badge here
                }

                root.setOnClickListener {
                    onItemClick(conversation)
                }
            }
        }

        private fun loadImage(imageView: ImageView, imagePath: String) {
            realtimeDbService.observeImage(imagePath) { base64String ->
                if (base64String != null) {
                    val bitmap = com.example.helphive.core.utils.Base64Utils.base64ToBitmap(base64String)
                    if (bitmap != null) {
                        Glide.with(imageView.context)
                            .load(bitmap)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(imageView)
                    } else {
                        Glide.with(imageView.context)
                            .load(R.drawable.ic_person)
                            .circleCrop()
                            .into(imageView)
                    }
                } else {
                    Glide.with(imageView.context)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(imageView)
                }
            }
        }
    }

    class ConversationDiffCallback : DiffUtil.ItemCallback<ChatConversation>() {
        override fun areItemsTheSame(oldItem: ChatConversation, newItem: ChatConversation): Boolean {
            return oldItem.requestId == newItem.requestId
        }

        override fun areContentsTheSame(oldItem: ChatConversation, newItem: ChatConversation): Boolean {
            return oldItem == newItem
        }
    }
}