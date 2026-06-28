package com.taxclientmanager.app.data.dao

import androidx.room.*
import com.taxclientmanager.app.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY date ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE clientTin = :tinNumber")
    fun getRemindersForClient(tinNumber: String): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND date <= :currentTime")
    suspend fun getPendingReminders(currentTime: Long): List<Reminder>
}
