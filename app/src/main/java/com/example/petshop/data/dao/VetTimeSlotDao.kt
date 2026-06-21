package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.VetTimeSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface VetTimeSlotDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(slot: VetTimeSlot): Long

    @Update
    suspend fun update(slot: VetTimeSlot)

    @Delete
    suspend fun delete(slot: VetTimeSlot)

    @Query("SELECT * FROM vet_time_slots WHERE slotId = :id")
    suspend fun getById(id: Int): VetTimeSlot?

    @Query("SELECT * FROM vet_time_slots WHERE staffId = :staffId ORDER BY slotStartTime ASC")
    fun getSlotsByStaff(staffId: Int): Flow<List<VetTimeSlot>>

    @Query("""
        SELECT * FROM vet_time_slots 
        WHERE staffId = :staffId 
        AND slotStartTime >= :from 
        AND slotStartTime <= :to 
        ORDER BY slotStartTime ASC
    """)
    fun getSlotsByStaffInRange(staffId: Int, from: Long, to: Long): Flow<List<VetTimeSlot>>

    @Query("""
        SELECT * FROM vet_time_slots 
        WHERE staffId = :staffId 
        AND isBooked = false 
        AND slotStartTime >= :from 
        AND slotStartTime <= :to 
        ORDER BY slotStartTime ASC
    """)
    fun getAvailableSlotsByStaffInRange(staffId: Int, from: Long, to: Long): Flow<List<VetTimeSlot>>

    @Query("""
        SELECT COUNT(*) FROM vet_time_slots 
        WHERE staffId = :staffId 
        AND isBooked = true 
        AND slotStartTime = :slotStartTime
    """)
    suspend fun isSlotBooked(staffId: Int, slotStartTime: Long): Int

    @Query("UPDATE vet_time_slots SET isBooked = :isBooked, appointmentId = :appointmentId WHERE slotId = :slotId")
    suspend fun updateBookingStatus(slotId: Int, isBooked: Boolean, appointmentId: Int?)

    @Query("SELECT * FROM vet_time_slots WHERE appointmentId = :appointmentId")
    suspend fun getSlotByAppointmentId(appointmentId: Int): VetTimeSlot?

    @Query("SELECT * FROM vet_time_slots WHERE staffId = :staffId AND slotStartTime = :slotStartTime LIMIT 1")
    suspend fun getByStaffAndStartTime(staffId: Int, slotStartTime: Long): VetTimeSlot?

    @Query("DELETE FROM vet_time_slots WHERE slotStartTime < :beforeTime")
    suspend fun deleteOldSlots(beforeTime: Long)
}

