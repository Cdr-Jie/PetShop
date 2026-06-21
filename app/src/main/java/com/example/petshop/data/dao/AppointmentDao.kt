package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.Appointment
import com.example.petshop.data.entity.AppointmentStatus
import com.example.petshop.data.relation.AppointmentWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("SELECT * FROM appointments WHERE appointmentId = :id")
    suspend fun getById(id: Int): Appointment?

    @Query("SELECT * FROM appointments ORDER BY scheduledAt DESC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE clientId = :clientId ORDER BY scheduledAt DESC")
    fun getAppointmentsByClient(clientId: Int): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE petId = :petId ORDER BY scheduledAt DESC")
    fun getAppointmentsByPet(petId: Int): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE staffId = :staffId ORDER BY scheduledAt DESC")
    fun getAppointmentsByStaff(staffId: Int): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE status = :status ORDER BY scheduledAt ASC")
    fun getAppointmentsByStatus(status: AppointmentStatus): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments
        WHERE scheduledAt >= :startOfDay AND scheduledAt <= :endOfDay
        ORDER BY scheduledAt ASC
    """)
    fun getAppointmentsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments
        WHERE scheduledAt >= :from AND scheduledAt <= :to
        ORDER BY scheduledAt ASC
    """)
    fun getAppointmentsInRange(from: Long, to: Long): Flow<List<Appointment>>

    @Query("UPDATE appointments SET status = :status, updatedAt = :updatedAt WHERE appointmentId = :id")
    suspend fun updateStatus(id: Int, status: AppointmentStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM appointments WHERE status = :status")
    suspend fun countByStatus(status: AppointmentStatus): Int

    @Query("""
        SELECT * FROM appointments
        WHERE status IN ('PENDING','CONFIRMED','IN_PROGRESS')
        ORDER BY scheduledAt ASC
    """)
    fun getUpcomingAppointments(): Flow<List<Appointment>>

    @Transaction
    @Query("SELECT * FROM appointments ORDER BY scheduledAt DESC")
    fun getAllWithDetails(): Flow<List<AppointmentWithDetails>>

    @Query(
        """
        SELECT COUNT(*) FROM appointments
        WHERE scheduledAt = :scheduledAt
          AND status IN ('PENDING','CONFIRMED','IN_PROGRESS')
          AND (
               petId = :petId
               OR (:staffId IS NOT NULL AND staffId = :staffId)
          )
        """
    )
    suspend fun countConflicts(scheduledAt: Long, petId: Int, staffId: Int?): Int
}
