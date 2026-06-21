package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class Service(
    @PrimaryKey(autoGenerate = true)
    val serviceId: Int = 0,
    val name: String,
    val description: String = "",
    val category: String = "",         // e.g. Consultation, Grooming, Vaccination
    val price: Double,
    val durationMinutes: Int,
    val isActive: Boolean = true
)

