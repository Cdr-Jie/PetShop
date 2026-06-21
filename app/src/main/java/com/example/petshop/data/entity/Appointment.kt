package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AppointmentStatus {
    PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
}

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["clientId"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["petId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["staffId"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Service::class,
            parentColumns = ["serviceId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = TimeSlot::class,
            parentColumns = ["timeSlotId"],
            childColumns = ["timeSlotId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("clientId"),
        Index("petId"),
        Index("staffId"),
        Index("serviceId"),
        Index("timeSlotId")
    ]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val appointmentId: Int = 0,
    val clientId: Int,
    val petId: Int,
    val staffId: Int? = null,
    val serviceId: Int? = null,
    val timeSlotId: Int? = null,
    val scheduledAt: Long,              // epoch millis
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notes: String = "",
    val diagnosis: String = "",
    val prescription: String = "",
    val totalAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

