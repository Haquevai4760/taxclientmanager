package com.taxclientmanager.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "clients")
@Serializable
data class Client(
    @PrimaryKey
    @SerialName("tin_number")
    val tinNumber: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("mobile_number")
    val mobileNumber: String,
    @SerialName("address")
    val address: String,
    @SerialName("profession")
    val profession: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("user_id")
    val userId: String? = null
)
