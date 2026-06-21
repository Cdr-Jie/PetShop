package com.example.petshop.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.petshop.data.entity.*

data class AppointmentWithDetails(
    @Embedded val appointment: Appointment,
    @Relation(parentColumn = "clientId", entityColumn = "clientId")
    val client: Client?,
    @Relation(parentColumn = "petId", entityColumn = "petId")
    val pet: Pet?,
    @Relation(parentColumn = "staffId", entityColumn = "staffId")
    val staff: Staff?
)

