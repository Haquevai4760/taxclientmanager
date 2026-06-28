package com.taxclientmanager.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    /**
     * Formats a timestamp into a readable date string.
     * Example: 15 Oct 2024
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Formats a timestamp into a numeric date string for input fields.
     * Example: 15-10-2024
     */
    fun formatDateNumeric(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
