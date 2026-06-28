package com.taxclientmanager.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ServiceType(val displayName: String) {
    @SerialName("NEW_TIN_OPEN")
    NEW_TIN_OPEN("New TIN Open"),
    @SerialName("TIN_EDIT")
    TIN_EDIT("TIN Edit"),
    @SerialName("RETURN_SUBMISSION")
    RETURN_SUBMISSION("Return Submission"),
    @SerialName("RE_RETURN_SUBMISSION")
    RE_RETURN_SUBMISSION("Re-Return Submission")
}

@Entity(
    tableName = "service_records",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["tinNumber"],
            childColumns = ["tinNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tinNumber"])]
)
@Serializable
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Long = 0,
    @SerialName("tin_number")
    val tinNumber: String,
    @SerialName("service_type")
    val serviceType: ServiceType,
    @SerialName("income_year")
    val incomeYear: String,
    @SerialName("service_charge")
    val serviceCharge: Double,
    @SerialName("paid_amount")
    val paidAmount: Double = 0.0,
    @SerialName("service_date")
    val serviceDate: Long,
    @SerialName("remarks")
    val remarks: String? = null,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("user_id")
    val userId: String? = null
)
