package com.taxclientmanager.app.utils

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    /**
     * Formats a double amount into Bangladeshi Taka (TK) currency format.
     * Uses the Indian locale (en-IN) which follows the same grouping as BD (e.g., 2,58,500).
     */
    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getInstance(Locale("en", "IN"))
        return formatter.format(amount)
    }

    /**
     * Returns the currency symbol for Bangladeshi Taka.
     */
    fun getCurrencySymbol(): String = "৳"
}
