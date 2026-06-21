package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(client: Client): Long

    @Update
    suspend fun update(client: Client)

    @Delete
    suspend fun delete(client: Client)

    @Query("SELECT * FROM clients WHERE clientId = :id")
    suspend fun getById(id: Int): Client?

    @Query("SELECT * FROM clients WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: Int): Client?

    @Query("SELECT * FROM clients ORDER BY lastName, firstName ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("""
        SELECT * FROM clients 
        WHERE firstName LIKE '%' || :query || '%'
           OR lastName  LIKE '%' || :query || '%'
           OR phone     LIKE '%' || :query || '%'
           OR email     LIKE '%' || :query || '%'
        ORDER BY lastName, firstName ASC
    """)
    fun searchClients(query: String): Flow<List<Client>>

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun getClientCount(): Int
}

