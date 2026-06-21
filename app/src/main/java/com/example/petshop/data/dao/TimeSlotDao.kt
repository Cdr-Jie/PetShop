package com.example.petshop.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.petshop.data.entity.TimeSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(slot: TimeSlot): Long

    @Update
    suspend fun update(slot: TimeSlot)

    @Query("SELECT * FROM time_slots WHERE staffId = :staffId ORDER BY slotStartTime ASC")
    fun getSlotsByStaff(staffId: Int): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots WHERE staffId = :staffId AND slotStartTime = :slotStartTime LIMIT 1")
    suspend fun findByStartTime(staffId: Int, slotStartTime: Long): TimeSlot?

    @Query("UPDATE time_slots SET isBooked = :isBooked WHERE timeSlotId = :timeSlotId")
    suspend fun updateBookedState(timeSlotId: Int, isBooked: Boolean)
}

