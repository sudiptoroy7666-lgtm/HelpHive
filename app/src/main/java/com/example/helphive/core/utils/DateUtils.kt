// core/utils/DateUtils.kt
package com.example.helphive.core.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val locale = Locale.getDefault()

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", locale)
        return sdf.format(date)
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    // ✅ NEW: Smart chat timestamp formatter
    fun formatChatTimestamp(timestamp: Long): String {
        val messageDate = Date(timestamp)
        val now = Date()

        val messageCalendar = Calendar.getInstance().apply { time = messageDate }
        val nowCalendar = Calendar.getInstance().apply { time = now }

        val isSameYear = messageCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR)
        val isSameDay = isSameYear &&
                messageCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR)
        val isYesterday = isSameYear &&
                messageCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR) - 1

        return when {
            isSameDay -> {
                // Today: "2:30 PM"
                SimpleDateFormat("h:mm a", locale).format(messageDate)
            }
            isYesterday -> {
                // Yesterday: "Yesterday"
                "Yesterday"
            }
            !isSameYear -> {
                // Different year: "Jan 5, 2024"
                SimpleDateFormat("MMM d, yyyy", locale).format(messageDate)
            }
            else -> {
                // Same year, different day: "Jan 5"
                SimpleDateFormat("MMM d", locale).format(messageDate)
            }
        }
    }

    // ✅ For chat conversation list (shows time for recent, date for older)
    fun formatConversationTimestamp(timestamp: Long): String {
        val messageDate = Date(timestamp)
        val now = Date()

        val messageCalendar = Calendar.getInstance().apply { time = messageDate }
        val nowCalendar = Calendar.getInstance().apply { time = now }

        val isSameDay = messageCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
                messageCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR)

        return if (isSameDay) {
            // Today: show time "2:30 PM"
            SimpleDateFormat("h:mm a", locale).format(messageDate)
        } else {
            // Older: show date "Jan 5"
            SimpleDateFormat("MMM d", locale).format(messageDate)
        }
    }
}