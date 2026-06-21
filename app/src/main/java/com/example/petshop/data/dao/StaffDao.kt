package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.Staff
import com.example.petshop.data.entity.StaffRole
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(staff: Staff): Long

    @Update
    suspend fun update(staff: Staff)

    @Delete
    suspend fun delete(staff: Staff)

    @Query("SELECT * FROM staff WHERE staffId = :id")
    suspend fun getById(id: Int): Staff?

    @Query("SELECT * FROM staff WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: Int): Staff?

    @Query("SELECT * FROM staff WHERE isActive = 1 ORDER BY lastName, firstName ASC")
    fun getActiveStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff ORDER BY lastName, firstName ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE role = :role AND isActive = 1 ORDER BY lastName, firstName ASC")
    fun getStaffByRole(role: StaffRole): Flow<List<Staff>>

    @Query("""
        SELECT * FROM staff
        WHERE firstName    LIKE '%' || :query || '%'
           OR lastName     LIKE '%' || :query || '%'
           OR specialization LIKE '%' || :query || '%'
        ORDER BY lastName, firstName ASC
    """)
    fun searchStaff(query: String): Flow<List<Staff>>

    @Query("UPDATE staff SET isActive = :isActive WHERE staffId = :staffId")
    suspend fun setActiveStatus(staffId: Int, isActive: Boolean)
}

