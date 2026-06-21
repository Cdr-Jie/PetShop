package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicine_inventory",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["medicineId"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId")]
)
data class MedicineInventory(
    @PrimaryKey(autoGenerate = true)
    val inventoryId: Int = 0,
    val medicineId: Int,
    val quantity: Int,
    val unitPrice: Double,
    val sellingPrice: Double,
    val batchNumber: String = "",
    val expiryDate: Long? = null,      // epoch millis
    val reorderLevel: Int = 10,
    val supplierName: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

