package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.Service
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(service: Service): Long

    @Update
    suspend fun update(service: Service)

    @Delete
    suspend fun delete(service: Service)

    @Query("SELECT * FROM services WHERE serviceId = :id")
    suspend fun getById(id: Int): Service?

    @Query("SELECT * FROM services WHERE isActive = 1 ORDER BY category, name ASC")
    fun getActiveServices(): Flow<List<Service>>

    @Query("SELECT * FROM services ORDER BY category, name ASC")
    fun getAllServices(): Flow<List<Service>>

    @Query("SELECT * FROM services WHERE category = :category AND isActive = 1 ORDER BY name ASC")
    fun getServicesByCategory(category: String): Flow<List<Service>>

    @Query("""
        SELECT * FROM services
        WHERE name        LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR category    LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchServices(query: String): Flow<List<Service>>

    @Query("UPDATE services SET isActive = :isActive WHERE serviceId = :serviceId")
    suspend fun setActiveStatus(serviceId: Int, isActive: Boolean)

    @Query("SELECT DISTINCT category FROM services ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
}

