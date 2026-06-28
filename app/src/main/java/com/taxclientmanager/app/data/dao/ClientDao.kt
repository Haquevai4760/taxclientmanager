package com.taxclientmanager.app.data.dao

import androidx.room.*
import com.taxclientmanager.app.data.model.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClient(client: Client)

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)

    @Query("SELECT * FROM clients WHERE tinNumber = :tinNumber")
    suspend fun getClientByTin(tinNumber: String): Client?

    @Query("SELECT * FROM clients ORDER BY fullName ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("""
        SELECT * FROM clients 
        WHERE fullName LIKE '%' || :query || '%' 
        OR mobileNumber LIKE '%' || :query || '%' 
        OR tinNumber LIKE '%' || :query || '%'
        ORDER BY fullName ASC
    """)
    fun searchClients(query: String): Flow<List<Client>>

    @Query("SELECT COUNT(*) FROM clients")
    fun getClientCount(): Flow<Int>
}
