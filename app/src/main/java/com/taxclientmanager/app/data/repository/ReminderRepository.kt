package com.taxclientmanager.app.data.repository

import com.taxclientmanager.app.data.dao.ReminderDao
import com.taxclientmanager.app.data.model.Reminder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val postgrest: Postgrest,
    private val auth: Auth
) {
    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()

    fun getRemindersForClient(tinNumber: String): Flow<List<Reminder>> = reminderDao.getRemindersForClient(tinNumber)

    suspend fun insertReminder(reminder: Reminder) {
        val reminderWithUser = reminder.copy(userId = currentUserId)
        val rowId = reminderDao.insertReminder(reminderWithUser)
        try {
            val reminderWithId = reminderWithUser.copy(id = rowId.toInt())
            postgrest.from("reminders").insert(reminderWithId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateReminder(reminder: Reminder) {
        val reminderWithUser = reminder.copy(userId = currentUserId)
        reminderDao.updateReminder(reminderWithUser)
        try {
            postgrest.from("reminders").update(reminderWithUser) {
                filter {
                    eq("id", reminder.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
        try {
            postgrest.from("reminders").delete {
                filter {
                    eq("id", reminder.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getPendingReminders(currentTime: Long): List<Reminder> = reminderDao.getPendingReminders(currentTime)
}
