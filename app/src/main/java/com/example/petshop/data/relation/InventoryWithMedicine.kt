package com.example.petshop.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.petshop.data.entity.Medicine
import com.example.petshop.data.entity.MedicineInventory

data class InventoryWithMedicine(
    @Embedded val inventory: MedicineInventory,
    @Relation(parentColumn = "medicineId", entityColumn = "medicineId")
    val medicine: Medicine?
)

