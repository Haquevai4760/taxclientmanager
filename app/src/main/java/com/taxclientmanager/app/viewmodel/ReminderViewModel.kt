package com.taxclientmanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.model.Reminder
import com.taxclientmanager.app.data.model.ReminderCategory
import com.taxclientmanager.app.data.model.RepeatInterval
import com.taxclientmanager.app.data.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllReminders().collect { reminders ->
                _uiState.value = _uiState.value.copy(reminders = reminders, isLoading = false)
            }
        }
    }

    fun getReminderById(id: Int, callback: (Reminder?) -> Unit) {
        viewModelScope.launch {
            val reminders = _uiState.value.reminders
            val reminder = reminders.find { it.id == id }
            callback(reminder)
        }
    }

    fun addReminder(
        title: String,
        clientTin: String?,
        category: ReminderCategory,
        date: Long,
        time: String,
        repeat: RepeatInterval,
        note: String
    ) {
        viewModelScope.launch {
            val reminder = Reminder(
                title = title,
                clientTin = clientTin,
                category = category,
                date = date,
                time = time,
                repeat = repeat,
                note = note
            )
            repository.insertReminder(reminder)
        }
    }

    fun updateReminder(
        id: Int,
        title: String,
        clientTin: String?,
        category: ReminderCategory,
        date: Long,
        time: String,
        repeat: RepeatInterval,
        note: String,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            val reminder = Reminder(
                id = id,
                title = title,
                clientTin = clientTin,
                category = category,
                date = date,
                time = time,
                repeat = repeat,
                note = note,
                isCompleted = isCompleted
            )
            repository.updateReminder(reminder)
        }
    }

    fun toggleReminderCompletion(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }
}
