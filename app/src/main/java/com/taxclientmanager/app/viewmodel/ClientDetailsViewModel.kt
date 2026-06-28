package com.taxclientmanager.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.model.Client
import com.taxclientmanager.app.data.model.ServiceRecord
import com.taxclientmanager.app.data.repository.TaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientDetailsUiState(
    val client: Client? = null,
    val serviceRecords: List<ServiceRecord> = emptyList()
)

@HiltViewModel
class ClientDetailsViewModel @Inject constructor(
    private val repository: TaxRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tinNumber: String = checkNotNull(savedStateHandle["clientId"]) // Nav key is clientId

    val uiState: StateFlow<ClientDetailsUiState> = combine(
        flow { emit(repository.getClientByTin(tinNumber)) },
        repository.getServiceRecordsForClient(tinNumber)
    ) { client, records ->
        ClientDetailsUiState(client = client, serviceRecords = records)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClientDetailsUiState()
    )

    fun deleteServiceRecord(record: ServiceRecord) {
        viewModelScope.launch {
            repository.deleteServiceRecord(record)
        }
    }
}
