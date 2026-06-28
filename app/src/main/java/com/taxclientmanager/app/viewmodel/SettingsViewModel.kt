package com.taxclientmanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.CurrencyPreferences
import com.taxclientmanager.app.data.repository.TaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val currencyPreferences: CurrencyPreferences,
    private val repository: TaxRepository
) : ViewModel() {

    val currencyCode: StateFlow<String> = currencyPreferences.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BDT")

    val currencySymbol: StateFlow<String> = currencyPreferences.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "৳")

    private val _exportState = kotlinx.coroutines.flow.MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState

    fun updateCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            currencyPreferences.saveCurrency(code, symbol)
        }
    }

    fun exportClients(outputStream: java.io.OutputStream, format: ExportFormat) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            val clients = repository.getAllClients().first()
            val success = when (format) {
                ExportFormat.CSV -> com.taxclientmanager.app.utils.ExportUtils.exportClientsToCSV(clients, outputStream)
                ExportFormat.EXCEL -> com.taxclientmanager.app.utils.ExportUtils.exportClientsToExcel(clients, outputStream)
            }
            _exportState.value = if (success) ExportState.Success else ExportState.Error("Export failed")
        }
    }

    fun exportServices(outputStream: java.io.OutputStream, format: ExportFormat) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            val services = repository.getAllServiceRecords().first()
            val success = when (format) {
                ExportFormat.CSV -> com.taxclientmanager.app.utils.ExportUtils.exportServicesToCSV(services, outputStream)
                ExportFormat.EXCEL -> false // TODO: Implement Excel for services if needed
            }
            _exportState.value = if (success) ExportState.Success else ExportState.Error("Export failed")
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}

enum class ExportFormat {
    CSV, EXCEL
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}
