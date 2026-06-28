package com.taxclientmanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.model.ServiceRecord
import com.taxclientmanager.app.data.repository.TaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.taxclientmanager.app.data.model.ServiceType
import java.util.Calendar

data class ServiceWithClientInfo(
    val service: ServiceRecord,
    val clientName: String
)

data class ServiceListUiState(
    val services: List<ServiceWithClientInfo> = emptyList(),
    val totalServices: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val pendingClientsCount: Int = 0,
    val todaysServicesCount: Int = 0,
    val todaysRevenue: Double = 0.0,
    val searchQuery: String = "",
    val selectedServiceType: ServiceType? = null,
    val sortOrder: ServiceSortOrder = ServiceSortOrder.DATE_DESC
)

enum class ServiceSortOrder {
    DATE_ASC, DATE_DESC, AMOUNT_ASC, AMOUNT_DESC, CLIENT_NAME_ASC, CLIENT_NAME_DESC
}

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val repository: TaxRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOrder = MutableStateFlow(ServiceSortOrder.DATE_DESC)
    val sortOrder: StateFlow<ServiceSortOrder> = _sortOrder

    private val _selectedServiceType = MutableStateFlow<ServiceType?>(null)
    val selectedServiceType: StateFlow<ServiceType?> = _selectedServiceType

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ServiceListUiState> = combine(
        _searchQuery,
        _sortOrder,
        _selectedServiceType,
        repository.getAllServiceRecords(),
        repository.getAllClients()
    ) { query, order, typeFilter, allServices, allClients ->
        val clientMap = allClients.associateBy { it.tinNumber }
        
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        val servicesWithInfo = allServices.map { service ->
            ServiceWithClientInfo(
                service = service,
                clientName = clientMap[service.tinNumber]?.fullName ?: "Unknown Client"
            )
        }.filter { 
            val matchesQuery = it.clientName.contains(query, ignoreCase = true) ||
                    it.service.tinNumber.contains(query, ignoreCase = true) ||
                    it.service.serviceType.displayName.contains(query, ignoreCase = true) ||
                    it.service.incomeYear.contains(query, ignoreCase = true)
            
            val matchesType = typeFilter == null || it.service.serviceType == typeFilter
            
            matchesQuery && matchesType
        }

        val sorted = when (order) {
            ServiceSortOrder.DATE_ASC -> servicesWithInfo.sortedBy { it.service.serviceDate }
            ServiceSortOrder.DATE_DESC -> servicesWithInfo.sortedByDescending { it.service.serviceDate }
            ServiceSortOrder.AMOUNT_ASC -> servicesWithInfo.sortedBy { it.service.serviceCharge }
            ServiceSortOrder.AMOUNT_DESC -> servicesWithInfo.sortedByDescending { it.service.serviceCharge }
            ServiceSortOrder.CLIENT_NAME_ASC -> servicesWithInfo.sortedBy { it.clientName.lowercase() }
            ServiceSortOrder.CLIENT_NAME_DESC -> servicesWithInfo.sortedByDescending { it.clientName.lowercase() }
        }

        val pendingServices = allServices.filter { it.serviceCharge > it.paidAmount }
        val pendingClients = pendingServices.map { it.tinNumber }.distinct()

        ServiceListUiState(
            services = sorted,
            totalServices = allServices.size,
            totalRevenue = allServices.sumOf { it.serviceCharge },
            totalPaid = allServices.sumOf { it.paidAmount },
            totalPending = allServices.sumOf { it.serviceCharge - it.paidAmount },
            pendingClientsCount = pendingClients.size,
            todaysServicesCount = allServices.count { it.serviceDate >= todayStart },
            todaysRevenue = allServices.filter { it.serviceDate >= todayStart }.sumOf { it.serviceCharge },
            searchQuery = query,
            selectedServiceType = typeFilter,
            sortOrder = order
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServiceListUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSortOrderChange(newOrder: ServiceSortOrder) {
        _sortOrder.value = newOrder
    }

    fun onServiceTypeChange(type: ServiceType?) {
        _selectedServiceType.value = type
    }

    fun deleteService(service: ServiceRecord) {
        viewModelScope.launch {
            repository.deleteServiceRecord(service)
        }
    }
}
