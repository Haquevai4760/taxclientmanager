package com.taxclientmanager.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReminderCategory {
    @SerialName("RETURN_SUBMISSION")
    RETURN_SUBMISSION,
    @SerialName("TIN_RENEWAL")
    TIN_RENEWAL,
    @SerialName("PAYMENT")
    PAYMENT,
    @SerialName("OTHER")
    OTHER
}

@Serializable
enum class RepeatInterval {
    @SerialName("NONE")
    NONE,
    @SerialName("DAILY")
    DAILY,
    @SerialName("WEEKLY")
    WEEKLY,
    @SerialName("MONTHLY")
    MONTHLY
}

@Entity(tableName = "reminders")
@Serializable
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Int = 0,
    @SerialName("title")
    val title: String,
    @SerialName("client_tin")
    val clientTin: String? = null,
    @SerialName("category")
    val category: ReminderCategory,
    @SerialName("date")
    val date: Long,
    @SerialName("time")
    val time: String,
    @SerialName("repeat")
    val repeat: RepeatInterval = RepeatInterval.NONE,
    @SerialName("note")
    val note: String = "",
    @SerialName("is_completed")
    val isCompleted: Boolean = false,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("user_id")
    val userId: String? = null
)
