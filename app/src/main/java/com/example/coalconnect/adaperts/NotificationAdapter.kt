package com.example.coalconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coalconnect.R
import com.example.coalconnect.databinding.NotificationItemBinding

class NotificationAdapter(
    private val notifications: List<String>, // Changed from ArrayList to List for immutability
    private val notificationImages: List<Int> // Changed from ArrayList to List for immutability
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(private val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                notificationTextView.text = notifications[position]

                // Safety check to avoid IndexOutOfBoundsException
                if (position < notificationImages.size) {
                    notificationImageView.setImageResource(notificationImages[position])
                } else {
                    // Provide a default image or handle the case when the image is not available
                    notificationImageView.setImageResource(R.drawable.sademoji)
                }
            }
        }
    }
}
