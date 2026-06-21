package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicines",
    indices = [Index(value = ["name", "brand"], unique = true)]
)
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val medicineId: Int = 0,
    val name: String,
    val brand: String = "",
    val description: String = "",
    val category: String = "",         // e.g. Antibiotic, Vaccine, Antiparasitic
    val unit: String = "tablet",       // tablet, ml, vial, etc.
    val requiresPrescription: Boolean = false,
    val isActive: Boolean = true
)

