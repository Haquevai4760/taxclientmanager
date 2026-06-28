package com.taxclientmanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.model.Client
import com.taxclientmanager.app.data.repository.TaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientWithStats(
    val client: Client,
    val totalServices: Int,
    val totalPaid: Double
)

data class ClientListUiState(
    val clients: List<ClientWithStats> = emptyList(),
    val totalClients: Int = 0,
    val newThisYear: Int = 0,
    val activeClients: Int = 0,
    val inactiveClients: Int = 0,
    val totalPaidAmount: Double = 0.0,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val repository: TaxRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ClientListUiState> = combine(
        _searchQuery,
        _sortOrder,
        repository.getAllClients(),
        repository.getAllServiceRecords()
    ) { query, order, allClients, allServices ->
        val serviceMap = allServices.groupBy { it.tinNumber }
        
        val clientsWithStats = allClients.map { client ->
            val clientServices = serviceMap[client.tinNumber] ?: emptyList()
            ClientWithStats(
                client = client,
                totalServices = clientServices.size,
                totalPaid = clientServices.sumOf { it.paidAmount }
            )
        }.filter { 
            it.client.fullName.contains(query, ignoreCase = true) ||
            it.client.tinNumber.contains(query, ignoreCase = true) ||
            it.client.mobileNumber.contains(query, ignoreCase = true)
        }

        val sorted = when (order) {
            SortOrder.NAME_ASC -> clientsWithStats.sortedBy { it.client.fullName.lowercase() }
            SortOrder.NAME_DESC -> clientsWithStats.sortedByDescending { it.client.fullName.lowercase() }
            SortOrder.DATE_ASC -> clientsWithStats.sortedBy { it.client.createdAt }
            SortOrder.DATE_DESC -> clientsWithStats.sortedByDescending { it.client.createdAt }
        }

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
        // Assuming "New This Year" means created in the current calendar year or a specific tax year.
        // For simplicity, let's say current year.
        val startOfYear = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.MONTH, 0)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis

        ClientListUiState(
            clients = sorted,
            totalClients = allClients.size,
            newThisYear = allClients.count { it.createdAt >= startOfYear },
            activeClients = allClients.count { it.isActive },
            inactiveClients = allClients.count { !it.isActive },
            totalPaidAmount = allServices.sumOf { it.paidAmount },
            searchQuery = query,
            sortOrder = order
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClientListUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSortOrderChange(newOrder: SortOrder) {
        _sortOrder.value = newOrder
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }
}

enum class SortOrder {
    NAME_ASC, NAME_DESC, DATE_ASC, DATE_DESC
}

