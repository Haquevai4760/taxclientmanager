package com.taxclientmanager.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val fullName: String,
    val passwordHash: String,
    val isLoggedIn: Boolean = false
)
