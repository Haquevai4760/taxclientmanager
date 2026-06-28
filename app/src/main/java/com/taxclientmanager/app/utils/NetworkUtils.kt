package com.taxclientmanager.app.utils

import java.net.UnknownHostException

object NetworkUtils {
    fun getReadableError(e: Throwable): String {
        return when (e) {
            is UnknownHostException -> "ইন্টারনেট সংযোগ নেই। অনুগ্রহ করে আপনার ইন্টারনেট সংযোগ পরীক্ষা করুন।"
            else -> {
                val message = e.message ?: ""
                if (message.contains("Unable to resolve host", ignoreCase = true)) {
                    "ইন্টারনেট সংযোগ নেই। অনুগ্রহ করে আপনার ইন্টারনেট সংযোগ পরীক্ষা করুন।"
                } else if (message.contains("timeout", ignoreCase = true)) {
                    "সার্ভার সংযোগ সময় শেষ হয়ে গেছে। আবার চেষ্টা করুন।"
                } else {
                    "একটি সমস্যা হয়েছে: ${e.localizedMessage ?: "আবার চেষ্টা করুন"}"
                }
            }
        }
    }
}
