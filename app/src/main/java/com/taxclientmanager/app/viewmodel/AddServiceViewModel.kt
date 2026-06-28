package com.taxclientmanager.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.model.ServiceRecord
import com.taxclientmanager.app.data.model.ServiceType
import com.taxclientmanager.app.data.repository.TaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val repository: TaxRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tinNumberFromNav: String = checkNotNull(savedStateHandle["clientId"]) // Nav still uses clientId key but it will be TIN
    private val serviceId: String? = savedStateHandle["serviceId"]

    var tinNumber by mutableStateOf(tinNumberFromNav)
    var serviceType by mutableStateOf(ServiceType.RETURN_SUBMISSION)
    var incomeYear by mutableStateOf("")
    var serviceCharge by mutableStateOf("")
    var paidAmount by mutableStateOf("")
    var serviceDate by mutableStateOf(System.currentTimeMillis())
    var remarks by mutableStateOf("")
    
    var isEditMode by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        object SaveSuccess : UiEvent()
    }

    var clientName by mutableStateOf("Loading...")
        private set

    init {
        viewModelScope.launch {
            repository.getClientByTin(tinNumberFromNav)?.let {
                clientName = it.fullName
                tinNumber = it.tinNumber
            }
        }
        serviceId?.let { idStr ->
            if (idStr != "null") {
                val id = idStr.toLongOrNull()
                if (id != null) {
                    isEditMode = true
                    viewModelScope.launch {
                        repository.getServiceRecordById(id)?.let { record ->
                            tinNumber = record.tinNumber
                            serviceType = record.serviceType
                            incomeYear = record.incomeYear
                            serviceCharge = record.serviceCharge.toString()
                            paidAmount = record.paidAmount.toString()
                            serviceDate = record.serviceDate
                            remarks = record.remarks ?: ""
                        }
                    }
                }
            }
        }
    }

    fun saveServiceRecord() {
        val charge = serviceCharge.toDoubleOrNull()
        val paid = paidAmount.toDoubleOrNull() ?: 0.0
        
        // Validation
        if (charge == null) {
            errorMessage = "Valid Service Charge is required"
            return
        }

        if (serviceType == ServiceType.RETURN_SUBMISSION || serviceType == ServiceType.RE_RETURN_SUBMISSION) {
            if (incomeYear.isBlank()) {
                errorMessage = "Income Year is required for Return Submissions"
                return
            }
        }

        viewModelScope.launch {
            val record = ServiceRecord(
                id = if (isEditMode) (serviceId?.toLongOrNull() ?: 0) else 0,
                tinNumber = tinNumber,
                serviceType = serviceType,
                incomeYear = if (serviceType == ServiceType.NEW_TIN_OPEN || serviceType == ServiceType.TIN_EDIT) "" else incomeYear,
                serviceCharge = charge,
                paidAmount = paid,
                serviceDate = serviceDate,
                remarks = if (remarks.isBlank()) null else remarks
            )

            if (isEditMode) {
                repository.updateServiceRecord(record)
            } else {
                repository.insertServiceRecord(record)
            }
            _eventFlow.emit(UiEvent.SaveSuccess)
        }
    }
}
