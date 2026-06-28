package com.taxclientmanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxclientmanager.app.data.dao.MonthlyRevenue
import com.taxclientmanager.app.data.dao.YearlySummary
import com.taxclientmanager.app.data.model.ServiceRecord
import com.taxclientmanager.app.data.model.ServiceType
import com.taxclientmanager.app.data.repository.TaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ServiceWithClient(
    val service: ServiceRecord,
    val clientName: String
)

data class DashboardUiState(
    val totalTaxpayers: Int = 0,
    val totalServices: Int = 0,
    val totalRevenue: Double = 0.0,
    val avgServiceCharge: Double = 0.0,
    val newTinOpened: Int = 0,
    val tinEditCount: Int = 0,
    val returnSubmitted: Int = 0,
    val reReturnSubmitted: Int = 0,
    val selectedYear: String = "2026-2027",
    val searchQuery: String = "",
    val thisYearRevenue: Double = 0.0,
    val recentServices: List<ServiceWithClient> = emptyList(),
    val pendingAmount: Double = 0.0,
    val pendingClientsCount: Int = 0,
    val yearlySummaries: List<YearlySummary> = emptyList(),
    val monthlyRevenue: List<MonthlyRevenue> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TaxRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow("2026-2027")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSelectedYear(year: String) {
        _selectedYear.value = year
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getClientCount(),
        repository.getServiceCount(),
        repository.getTotalRevenue(),
        repository.getCountByServiceType(ServiceType.NEW_TIN_OPEN),
        repository.getCountByServiceType(ServiceType.TIN_EDIT),
        repository.getCountByServiceType(ServiceType.RETURN_SUBMISSION),
        repository.getCountByServiceType(ServiceType.RE_RETURN_SUBMISSION),
        combine(_searchQuery, repository.getAllServiceRecords(), repository.getAllClients()) { query, services, clients ->
            val clientMap = clients.associateBy { it.tinNumber }
            val filtered = if (query.isBlank()) services.take(10)
            else services.filter { 
                it.tinNumber.contains(query, ignoreCase = true) || 
                it.serviceType.displayName.contains(query, ignoreCase = true)
            }
            filtered.map { 
                ServiceWithClient(it, clientMap[it.tinNumber]?.fullName ?: "Unknown Client")
            }
        },
        _selectedYear.flatMapLatest { repository.getRevenueByIncomeYear(it) },
        repository.getTotalPendingAmount(),
        repository.getPendingClientsCount(),
        repository.getYearlySummaries(),
        _selectedYear.flatMapLatest { repository.getMonthlyRevenueByYear(it) }
    ) { args: Array<Any?> ->
        val totalRevenue = (args[2] as? Double) ?: 0.0
        val totalServices = args[1] as Int
        DashboardUiState(
            totalTaxpayers = args[0] as Int,
            totalServices = totalServices,
            totalRevenue = totalRevenue,
            avgServiceCharge = if (totalServices > 0) totalRevenue / totalServices else 0.0,
            newTinOpened = args[3] as Int,
            tinEditCount = args[4] as Int,
            returnSubmitted = args[5] as Int,
            reReturnSubmitted = args[6] as Int,
            recentServices = args[7] as List<ServiceWithClient>,
            thisYearRevenue = (args[8] as? Double) ?: 0.0,
            selectedYear = _selectedYear.value,
            searchQuery = _searchQuery.value,
            pendingAmount = (args[9] as? Double) ?: 0.0,
            pendingClientsCount = (args[10] as? Int) ?: 0,
            yearlySummaries = (args[11] as? List<YearlySummary>) ?: emptyList(),
            monthlyRevenue = (args[12] as? List<MonthlyRevenue>) ?: emptyList()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
