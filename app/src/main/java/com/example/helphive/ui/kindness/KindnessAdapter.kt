package com.example.helphive.ui.kindness

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphive.R
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.Kindness
import com.example.helphive.databinding.ItemKindnessWithUserBinding
import com.example.helphive.data.firebase.RealtimeDbService

class KindnessAdapter(
    private val realtimeDbService: RealtimeDbService
) : ListAdapter<Kindness, KindnessAdapter.KindnessViewHolder>(KindnessDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KindnessViewHolder {
        val binding = ItemKindnessWithUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KindnessViewHolder(binding, realtimeDbService)
    }

    override fun onBindViewHolder(holder: KindnessViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class KindnessViewHolder(
        private val binding: ItemKindnessWithUserBinding,
        private val realtimeDbService: RealtimeDbService
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(kindness: Kindness) {
            binding.apply {
                tvKindnessText.text = kindness.text
                tvTimestamp.text = DateUtils.formatTimestamp(kindness.timestamp)
                tvUserName.text = kindness.userName

                // Load user profile image
                if (kindness.userProfileImage.isNotEmpty()) {
                    loadImage(ivUserImage, kindness.userProfileImage)
                } else {
                    Glide.with(ivUserImage.context)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivUserImage)
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

    class KindnessDiffCallback : DiffUtil.ItemCallback<Kindness>() {
        override fun areItemsTheSame(oldItem: Kindness, newItem: Kindness): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Kindness, newItem: Kindness): Boolean {
            return oldItem == newItem
        }
    }
}