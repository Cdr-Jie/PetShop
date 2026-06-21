package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vet_time_slots",
    foreignKeys = [
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["staffId"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("staffId"),
        Index("slotStartTime"),
        Index(value = ["staffId", "slotStartTime"], unique = true)
    ]
)
data class VetTimeSlot(
    @PrimaryKey(autoGenerate = true)
    val slotId: Int = 0,
    val staffId: Int,
    val slotStartTime: Long,  // epoch millis - start of the time slot
    val slotEndTime: Long,    // epoch millis - end of the time slot
    val isBooked: Boolean = false,
    val appointmentId: Int? = null,  // Reference to appointment if booked
    val createdAt: Long = System.currentTimeMillis()
)

