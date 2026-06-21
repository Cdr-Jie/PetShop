package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pet: Pet): Long

    @Update
    suspend fun update(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)

    @Query("SELECT * FROM pets WHERE petId = :id")
    suspend fun getById(id: Int): Pet?

    @Query("SELECT * FROM pets WHERE clientId = :clientId ORDER BY name ASC")
    fun getPetsByClient(clientId: Int): Flow<List<Pet>>

    @Query("SELECT * FROM pets ORDER BY name ASC")
    fun getAllPets(): Flow<List<Pet>>

    @Query("""
        SELECT * FROM pets 
        WHERE name    LIKE '%' || :query || '%'
           OR species LIKE '%' || :query || '%'
           OR breed   LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchPets(query: String): Flow<List<Pet>>

    @Query("SELECT COUNT(*) FROM pets WHERE clientId = :clientId")
    suspend fun getPetCountByClient(clientId: Int): Int
}

