package com.taxclientmanager.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val plan: String = "FREE",
    val plan_expire_date: String? = null
)
