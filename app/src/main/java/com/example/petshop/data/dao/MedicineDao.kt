package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.Medicine
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(medicine: Medicine): Long

    @Update
    suspend fun update(medicine: Medicine)

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("SELECT * FROM medicines WHERE medicineId = :id")
    suspend fun getById(id: Int): Medicine?

    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE category = :category AND isActive = 1 ORDER BY name ASC")
    fun getMedicinesByCategory(category: String): Flow<List<Medicine>>

    @Query("""
        SELECT * FROM medicines
        WHERE name        LIKE '%' || :query || '%'
           OR brand       LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR category    LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchMedicines(query: String): Flow<List<Medicine>>

    @Query("SELECT DISTINCT category FROM medicines ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("UPDATE medicines SET isActive = :isActive WHERE medicineId = :medicineId")
    suspend fun setActiveStatus(medicineId: Int, isActive: Boolean)
}

