// Updated HelpAdapter
package com.example.helphive.ui.help

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphive.R
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.HelpRequest
import com.example.helphive.databinding.ItemHelpRequestWithUserBinding
import com.example.helphive.data.firebase.RealtimeDbService

class HelpAdapter(
    private val realtimeDbService: RealtimeDbService,
    private val onItemClick: (HelpRequest) -> Unit
) : ListAdapter<HelpRequest, HelpAdapter.HelpViewHolder>(HelpDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val binding = ItemHelpRequestWithUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HelpViewHolder(binding, realtimeDbService, onItemClick)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HelpViewHolder(
        private val binding: ItemHelpRequestWithUserBinding,
        private val realtimeDbService: RealtimeDbService,
        private val onItemClick: (HelpRequest) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(helpRequest: HelpRequest) {
            binding.apply {
                tvHelpTitle.text = helpRequest.title
                tvHelpDescription.text = helpRequest.description
                tvTimestamp.text = DateUtils.formatTimestamp(helpRequest.timestamp)
                tvUserName.text = helpRequest.userName

                // Load user profile image
                if (helpRequest.userProfileImage.isNotEmpty()) {
                    loadImage(ivUserImage, helpRequest.userProfileImage)
                } else {
                    Glide.with(ivUserImage.context)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivUserImage)
                }

                // Load help request image if available
                if (helpRequest.imagePath.isNotEmpty()) {
                    loadHelpImage(ivHelpImage, helpRequest.imagePath)
                    ivHelpImage.visibility = android.view.View.VISIBLE
                } else {
                    ivHelpImage.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onItemClick(helpRequest)
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

        private fun loadHelpImage(imageView: ImageView, imagePath: String) {
            realtimeDbService.observeImage(imagePath) { base64String ->
                if (base64String != null) {
                    val bitmap = com.example.helphive.core.utils.Base64Utils.base64ToBitmap(base64String)
                    if (bitmap != null) {
                        Glide.with(imageView.context)
                            .load(bitmap)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(imageView)
                        imageView.visibility = android.view.View.VISIBLE
                    } else {
                        imageView.visibility = android.view.View.GONE
                    }
                } else {
                    imageView.visibility = android.view.View.GONE
                }
            }
        }
    }

    class HelpDiffCallback : DiffUtil.ItemCallback<HelpRequest>() {
        override fun areItemsTheSame(oldItem: HelpRequest, newItem: HelpRequest): Boolean {
            return oldItem.requestId == newItem.requestId
        }

        override fun areContentsTheSame(oldItem: HelpRequest, newItem: HelpRequest): Boolean {
            return oldItem == newItem
        }
    }
}