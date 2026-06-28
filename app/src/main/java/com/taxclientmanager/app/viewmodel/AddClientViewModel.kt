package com.taxclientmanager.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.model.Client
import com.taxclientmanager.app.data.repository.TaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddClientViewModel @Inject constructor(
    private val repository: TaxRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tinFromNav: String? = savedStateHandle["clientId"] // Nav still uses clientId key but it will be TIN

    var fullName by mutableStateOf("")
    var mobileNumber by mutableStateOf("")
    var address by mutableStateOf("")
    var profession by mutableStateOf("")
    var tinNumber by mutableStateOf("")
    var isActive by mutableStateOf(true)
    
    var isEditMode by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        object SaveSuccess : UiEvent()
    }

    init {
        tinFromNav?.let { tin ->
            if (tin != "null") {
                isEditMode = true
                viewModelScope.launch {
                    repository.getClientByTin(tin)?.let { client ->
                        fullName = client.fullName
                        mobileNumber = client.mobileNumber
                        address = client.address
                        profession = client.profession
                        tinNumber = client.tinNumber
                        isActive = client.isActive
                    }
                }
            }
        }
    }

    fun saveClient() {
        if (fullName.isBlank() || mobileNumber.isBlank() || tinNumber.isBlank()) {
            errorMessage = "TIN, Name and Mobile Number are required"
            return
        }

        viewModelScope.launch {
            if (!isEditMode) {
                val existing = repository.getClientByTin(tinNumber)
                if (existing != null) {
                    errorMessage = "TIN number already exists"
                    return@launch
                }
            }

            val client = Client(
                tinNumber = tinNumber,
                fullName = fullName,
                mobileNumber = mobileNumber,
                address = address,
                profession = profession,
                isActive = isActive
            )

            if (isEditMode) {
                val oldTin = tinFromNav ?: ""
                if (oldTin != tinNumber && oldTin.isNotBlank()) {
                    // TIN changed. Since it's Primary Key, we must delete old and insert new
                    val oldClient = repository.getClientByTin(oldTin)
                    if (oldClient != null) {
                        repository.deleteClient(oldClient)
                        repository.insertClient(client)
                    } else {
                        repository.updateClient(client)
                    }
                } else {
                    repository.updateClient(client)
                }
            } else {
                repository.insertClient(client)
            }
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }
}
