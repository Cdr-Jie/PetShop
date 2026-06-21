package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.Medicine
import com.example.petshop.data.entity.MedicineInventory
import com.example.petshop.data.relation.InventoryWithMedicine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MedicineViewModel(app: Application) : AndroidViewModel(app) {

    private val db = PetShopDatabase.getInstance(app)

    val medicines: StateFlow<List<Medicine>> =
        db.medicineDao().getActiveMedicines()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val inventory: StateFlow<List<InventoryWithMedicine>> =
        db.medicineInventoryDao().getAllWithMedicine()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lowStock: StateFlow<List<InventoryWithMedicine>> =
        db.medicineInventoryDao().getLowStockWithMedicine()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addMedicine(
        name: String, brand: String, category: String, unit: String,
        requiresPrescription: Boolean,
        qty: Int, unitPrice: Double, sellingPrice: Double, expiryDate: Long?
    ) = viewModelScope.launch {
        val medicineId = db.medicineDao().insert(
            Medicine(
                name = name.trim(), brand = brand.trim(),
                category = category.trim(), unit = unit.trim(),
                requiresPrescription = requiresPrescription
            )
        )
        db.medicineInventoryDao().insert(
            MedicineInventory(
                medicineId   = medicineId.toInt(),
                quantity     = qty,
                unitPrice    = unitPrice,
                sellingPrice = sellingPrice,
                expiryDate   = expiryDate
            )
        )
    }

    fun updateMedicine(medicine: Medicine) = viewModelScope.launch {
        db.medicineDao().update(medicine)
    }

    fun updateInventory(inventory: MedicineInventory) = viewModelScope.launch {
        db.medicineInventoryDao().update(inventory)
    }

    fun adjustStock(inventoryId: Int, delta: Int) = viewModelScope.launch {
        db.medicineInventoryDao().adjustQuantity(inventoryId, delta)
    }

    fun deleteInventory(inventory: MedicineInventory) = viewModelScope.launch {
        db.medicineInventoryDao().delete(inventory)
    }

    fun deleteMedicine(medicine: Medicine) = viewModelScope.launch {
        db.medicineDao().delete(medicine)
    }
}

