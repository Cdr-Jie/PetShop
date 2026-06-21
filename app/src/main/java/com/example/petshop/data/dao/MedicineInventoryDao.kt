package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.MedicineInventory
import com.example.petshop.data.relation.InventoryWithMedicine
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineInventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventory: MedicineInventory): Long

    @Update
    suspend fun update(inventory: MedicineInventory)

    @Delete
    suspend fun delete(inventory: MedicineInventory)

    @Query("SELECT * FROM medicine_inventory WHERE inventoryId = :id")
    suspend fun getById(id: Int): MedicineInventory?

    @Query("SELECT * FROM medicine_inventory WHERE medicineId = :medicineId ORDER BY expiryDate ASC")
    fun getByMedicine(medicineId: Int): Flow<List<MedicineInventory>>

    @Query("SELECT * FROM medicine_inventory ORDER BY lastUpdated DESC")
    fun getAllInventory(): Flow<List<MedicineInventory>>

    @Query("SELECT SUM(quantity) FROM medicine_inventory WHERE medicineId = :medicineId")
    suspend fun getTotalQuantity(medicineId: Int): Int?

    // Items with quantity at or below their reorder level
    @Query("""
        SELECT * FROM medicine_inventory 
        WHERE quantity <= reorderLevel
        ORDER BY quantity ASC
    """)
    fun getLowStockItems(): Flow<List<MedicineInventory>>

    // Items expiring before the given timestamp
    @Query("""
        SELECT * FROM medicine_inventory
        WHERE expiryDate IS NOT NULL AND expiryDate <= :beforeDate
        ORDER BY expiryDate ASC
    """)
    fun getExpiringItems(beforeDate: Long): Flow<List<MedicineInventory>>

    @Query("UPDATE medicine_inventory SET quantity = quantity + :delta, lastUpdated = :ts WHERE inventoryId = :inventoryId")
    suspend fun adjustQuantity(inventoryId: Int, delta: Int, ts: Long = System.currentTimeMillis())

    @Transaction
    @Query("SELECT * FROM medicine_inventory ORDER BY lastUpdated DESC")
    fun getAllWithMedicine(): Flow<List<InventoryWithMedicine>>

    @Transaction
    @Query("SELECT * FROM medicine_inventory WHERE quantity <= reorderLevel ORDER BY quantity ASC")
    fun getLowStockWithMedicine(): Flow<List<InventoryWithMedicine>>
}



