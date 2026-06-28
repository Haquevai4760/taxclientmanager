package com.taxclientmanager.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.taxclientmanager.app.data.dao.ClientDao
import com.taxclientmanager.app.data.dao.ServiceRecordDao
import com.taxclientmanager.app.data.dao.ReminderDao
import com.taxclientmanager.app.data.dao.UserDao
import com.taxclientmanager.app.data.model.Client
import com.taxclientmanager.app.data.model.Reminder
import com.taxclientmanager.app.data.model.ServiceRecord
import com.taxclientmanager.app.data.model.User

@Database(entities = [Client::class, ServiceRecord::class, Reminder::class, User::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun reminderDao(): ReminderDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "tax_client_manager_db"
    }
}

