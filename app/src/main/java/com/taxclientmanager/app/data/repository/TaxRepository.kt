package com.taxclientmanager.app.data.repository

import com.taxclientmanager.app.data.dao.ClientDao
import com.taxclientmanager.app.data.dao.ServiceRecordDao
import com.taxclientmanager.app.data.model.Client
import com.taxclientmanager.app.data.model.ServiceRecord
import com.taxclientmanager.app.data.model.ServiceType
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxRepository @Inject constructor(
    private val clientDao: ClientDao,
    private val serviceRecordDao: ServiceRecordDao,
    private val postgrest: Postgrest,
    private val auth: Auth
) {
    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    // Client Operations
    fun getAllClients(): Flow<List<Client>> = clientDao.getAllClients()
    
    fun searchClients(query: String): Flow<List<Client>> = clientDao.searchClients(query)
    
    suspend fun getClientByTin(tinNumber: String): Client? = clientDao.getClientByTin(tinNumber)
    
    suspend fun insertClient(client: Client) {
        val clientWithUser = client.copy(userId = currentUserId)
        clientDao.insertClient(clientWithUser)
        try {
            postgrest.from("clients").insert(clientWithUser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updateClient(client: Client) {
        val clientWithUser = client.copy(userId = currentUserId)
        clientDao.updateClient(clientWithUser)
        try {
            postgrest.from("clients").update(clientWithUser) {
                filter {
                    eq("tin_number", client.tinNumber)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deleteClient(client: Client) {
        clientDao.deleteClient(client)
        try {
            postgrest.from("clients").delete {
                filter {
                    eq("tin_number", client.tinNumber)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getClientCount(): Flow<Int> = clientDao.getClientCount()

    // Service Record Operations
    fun getServiceRecordsForClient(tinNumber: String): Flow<List<ServiceRecord>> = 
        serviceRecordDao.getServiceRecordsForClient(tinNumber)
    
    suspend fun insertServiceRecord(record: ServiceRecord) {
        val recordWithUser = record.copy(userId = currentUserId)
        val rowId = serviceRecordDao.insertServiceRecord(recordWithUser)
        try {
            val recordWithId = recordWithUser.copy(id = rowId)
            postgrest.from("services").insert(recordWithId) // Changed table name to 'services'
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updateServiceRecord(record: ServiceRecord) {
        val recordWithUser = record.copy(userId = currentUserId)
        serviceRecordDao.updateServiceRecord(recordWithUser)
        try {
            postgrest.from("services").update(recordWithUser) {
                filter {
                    eq("id", record.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deleteServiceRecord(record: ServiceRecord) {
        serviceRecordDao.deleteServiceRecord(record)
        try {
            postgrest.from("services").delete {
                filter {
                    eq("id", record.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getTotalRevenue(): Flow<Double?> = serviceRecordDao.getTotalRevenue()

    fun getTotalPendingAmount(): Flow<Double?> = serviceRecordDao.getTotalPendingAmount()

    fun getPendingClientsCount(): Flow<Int> = serviceRecordDao.getPendingClientsCount()
    
    fun getServiceCount(): Flow<Int> = serviceRecordDao.getServiceCount()

    fun getCountByServiceType(type: ServiceType): Flow<Int> = 
        serviceRecordDao.getCountByServiceType(type)

    suspend fun getServiceRecordById(id: Long): ServiceRecord? = 
        serviceRecordDao.getServiceRecordById(id)

    fun getAllServiceRecords(): Flow<List<ServiceRecord>> = 
        serviceRecordDao.getAllServiceRecords()

    fun getRevenueByIncomeYear(incomeYear: String): Flow<Double?> = 
        serviceRecordDao.getRevenueByIncomeYear(incomeYear)

    fun getMonthlyRevenueByYear(incomeYear: String): Flow<List<com.taxclientmanager.app.data.dao.MonthlyRevenue>> =
        serviceRecordDao.getMonthlyRevenueByYear(incomeYear)

    fun getYearlySummaries(): Flow<List<com.taxclientmanager.app.data.dao.YearlySummary>> =
        serviceRecordDao.getYearlySummaries()

    fun searchServices(query: String): Flow<List<ServiceRecord>> =
        serviceRecordDao.getAllServiceRecords().map { services ->
            if (query.isBlank()) services
            else services.filter {
                it.tinNumber.contains(query, ignoreCase = true) ||
                it.serviceType.displayName.contains(query, ignoreCase = true) ||
                it.incomeYear.contains(query, ignoreCase = true)
            }
        }
}
