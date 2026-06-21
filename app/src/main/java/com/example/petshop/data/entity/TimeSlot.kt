package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_slots",
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
data class TimeSlot(
    @PrimaryKey(autoGenerate = true)
    val timeSlotId: Int = 0,
    val staffId: Int,
    val slotStartTime: Long,
    val slotEndTime: Long,
    val isBooked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

