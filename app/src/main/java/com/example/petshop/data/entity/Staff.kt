package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StaffRole {
    VETERINARIAN, VETERINARY_NURSE, RECEPTIONIST, GROOMER, ADMIN
}

@Entity(
    tableName = "staff",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId")]
)
data class Staff(
    @PrimaryKey(autoGenerate = true)
    val staffId: Int = 0,
    val userId: Int? = null,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val role: StaffRole,
    val specialization: String = "",
    val licenseNumber: String = "",
    val hireDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val schedule: String = ""      // JSON string of working hours
)

