package com.taxclientmanager.app.data.dao

import androidx.room.*
import com.taxclientmanager.app.data.model.ServiceRecord
import com.taxclientmanager.app.data.model.ServiceType
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRecord(record: ServiceRecord): Long

    @Update
    suspend fun updateServiceRecord(record: ServiceRecord)

    @Delete
    suspend fun deleteServiceRecord(record: ServiceRecord)

    @Query("SELECT * FROM service_records WHERE tinNumber = :tinNumber ORDER BY serviceDate DESC")
    fun getServiceRecordsForClient(tinNumber: String): Flow<List<ServiceRecord>>

    @Query("SELECT SUM(serviceCharge) FROM service_records")
    fun getTotalRevenue(): Flow<Double?>

    @Query("SELECT SUM(serviceCharge - paidAmount) FROM service_records")
    fun getTotalPendingAmount(): Flow<Double?>

    @Query("SELECT COUNT(DISTINCT tinNumber) FROM service_records WHERE serviceCharge > paidAmount")
    fun getPendingClientsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM service_records")
    fun getServiceCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM service_records WHERE serviceType = :type")
    fun getCountByServiceType(type: ServiceType): Flow<Int>

    @Query("SELECT * FROM service_records WHERE id = :id")
    suspend fun getServiceRecordById(id: Long): ServiceRecord?

    @Query("SELECT * FROM service_records ORDER BY serviceDate DESC")
    fun getAllServiceRecords(): Flow<List<ServiceRecord>>

    @Query("SELECT SUM(serviceCharge) FROM service_records WHERE incomeYear = :incomeYear")
    fun getRevenueByIncomeYear(incomeYear: String): Flow<Double?>

    @Query("""
        SELECT 
            strftime('%m', serviceDate / 1000, 'unixepoch') as month,
            SUM(serviceCharge) as revenue
        FROM service_records 
        WHERE incomeYear = :incomeYear
        GROUP BY month
        ORDER BY month ASC
    """)
    fun getMonthlyRevenueByYear(incomeYear: String): Flow<List<MonthlyRevenue>>

    @Query("""
        SELECT 
            incomeYear, 
            COUNT(DISTINCT tinNumber) as totalClients, 
            COUNT(*) as totalServices, 
            SUM(serviceCharge) as totalRevenue, 
            SUM(serviceCharge - paidAmount) as totalPending 
        FROM service_records 
        GROUP BY incomeYear 
        ORDER BY incomeYear DESC
    """)
    fun getYearlySummaries(): Flow<List<YearlySummary>>
}

data class MonthlyRevenue(
    val month: String,
    val revenue: Double
)

data class YearlySummary(
    val incomeYear: String,
    val totalClients: Int,
    val totalServices: Int,
    val totalRevenue: Double,
    val totalPending: Double
)
