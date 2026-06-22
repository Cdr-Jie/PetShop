package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.Medicine
import com.example.petshop.data.entity.MedicineAdministrationLog
import com.example.petshop.data.entity.MedicineInventory
import com.example.petshop.data.entity.Staff
import com.example.petshop.data.entity.StaffRole
import com.example.petshop.data.relation.InventoryWithMedicine
import com.example.petshop.data.relation.MedicineLogWithMedicine
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

    val medicineLogs: StateFlow<List<MedicineLogWithMedicine>> =
        db.medicineAdministrationLogDao().getAllWithMedicine()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val veterinarians: StateFlow<List<Staff>> =
        db.staffDao().getStaffByRole(StaffRole.VETERINARIAN)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Medicine ──────────────────────────────────────────────────────────────

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

    // ── Medicine Log ──────────────────────────────────────────────────────────

    fun addMedicineLog(
        inventoryId: Int,
        petName: String,
        quantityUsed: Int,
        administeredBy: String,
        staffId: Int?,
        notes: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        val inv = db.medicineInventoryDao().getById(inventoryId)
        when {
            inv == null ->
                onResult(false, "Inventory item not found.")
            inv.quantity <= 0 ->
                onResult(false, "This medicine is out of stock.")
            inv.quantity < quantityUsed ->
                onResult(false, "Insufficient stock. Only ${inv.quantity} unit(s) available.")
            else -> {
                db.medicineAdministrationLogDao().insert(
                    MedicineAdministrationLog(
                        inventoryId    = inventoryId,
                        petName        = petName,
                        quantityUsed   = quantityUsed,
                        administeredBy = administeredBy,
                        staffId        = staffId,
                        notes          = notes
                    )
                )
                db.medicineInventoryDao().adjustQuantity(inventoryId, -quantityUsed)
                onResult(true, null)
            }
        }
    }

    fun deleteMedicineLog(log: MedicineAdministrationLog) = viewModelScope.launch {
        db.medicineAdministrationLogDao().delete(log)
    }

    // ── Veterinarian ──────────────────────────────────────────────────────────

    fun addVeterinarian(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        specialization: String,
        licenseNumber: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        runCatching {
            db.staffDao().insert(
                Staff(
                    firstName     = firstName.trim(),
                    lastName      = lastName.trim(),
                    phone         = phone.trim(),
                    email         = email.trim(),
                    role          = StaffRole.VETERINARIAN,
                    specialization = specialization.trim(),
                    licenseNumber = licenseNumber.trim()
                )
            )
            onResult(true, null)
        }.onFailure { onResult(false, it.message ?: "Failed to add veterinarian.") }
    }

    fun updateVeterinarian(
        staff: Staff,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        runCatching {
            db.staffDao().update(staff)
            onResult(true, null)
        }.onFailure { onResult(false, it.message ?: "Failed to update veterinarian.") }
    }

    fun deleteVeterinarian(
        staff: Staff,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        runCatching {
            db.staffDao().delete(staff)
            onResult(true, null)
        }.onFailure { onResult(false, it.message ?: "Failed to delete veterinarian.") }
    }
}
