package com.example.petshop.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.entity.Medicine
import com.example.petshop.data.entity.MedicineInventory
import com.example.petshop.data.relation.InventoryWithMedicine
import com.example.petshop.ui.viewmodel.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    onBack: () -> Unit,
    vm: MedicineViewModel = viewModel()
) {
    val medicines by vm.medicines.collectAsState()
    val inventory by vm.inventory.collectAsState()
    val lowStock by vm.lowStock.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var selectedInventory by remember { mutableStateOf<InventoryWithMedicine?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Medicine")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (lowStock.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${lowStock.size} item(s) at low stock level", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Medicines") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Inventory") })
            }

            when (selectedTab) {
                0 -> MedicinesList(medicines = medicines, onOpen = { selectedMedicine = it })
                1 -> InventoryList(
                    inventory = inventory,
                    onOpen = { selectedInventory = it },
                    onAdjust = { invId, delta -> vm.adjustStock(invId, delta) }
                )
            }
        }
    }

    if (showAdd) {
        AddMedicineDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, brand, cat, unit, rx, qty, up, sp, exp ->
                vm.addMedicine(name, brand, cat, unit, rx, qty, up, sp, exp)
                showAdd = false
            }
        )
    }

    selectedMedicine?.let { medicine ->
        MedicineDetailDialog(
            medicine = medicine,
            onDismiss = { selectedMedicine = null },
            onSave = {
                vm.updateMedicine(it)
                selectedMedicine = null
            },
            onDelete = {
                vm.deleteMedicine(medicine)
                selectedMedicine = null
            }
        )
    }

    selectedInventory?.let { item ->
        InventoryDetailDialog(
            item = item,
            onDismiss = { selectedInventory = null },
            onAdjust = { delta -> vm.adjustStock(item.inventory.inventoryId, delta) },
            onSave = {
                vm.updateInventory(it)
                selectedInventory = null
            },
            onDelete = {
                vm.deleteInventory(item.inventory)
                selectedInventory = null
            }
        )
    }
}

@Composable
private fun MedicinesList(medicines: List<Medicine>, onOpen: (Medicine) -> Unit) {
    if (medicines.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No medicines", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(medicines, key = { it.medicineId }) { med ->
                Card(
                    onClick = { onOpen(med) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Medication, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(med.name, fontWeight = FontWeight.SemiBold)
                            Text("${med.brand}  •  ${med.category}", style = MaterialTheme.typography.bodySmall)
                            Text("Unit: ${med.unit}${if (med.requiresPrescription) "  •  Rx" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryList(
    inventory: List<InventoryWithMedicine>,
    onOpen: (InventoryWithMedicine) -> Unit,
    onAdjust: (Int, Int) -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    if (inventory.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No inventory records", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(inventory, key = { it.inventory.inventoryId }) { item ->
                val isLow = item.inventory.quantity <= item.inventory.reorderLevel
                Card(
                    onClick = { onOpen(item) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = if (isLow) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    else CardDefaults.cardColors()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.medicine?.name ?: "Unknown", fontWeight = FontWeight.SemiBold)
                                Text("Batch: ${item.inventory.batchNumber.ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall)
                                item.inventory.expiryDate?.let {
                                    Text("Expires: ${sdf.format(Date(it))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${item.inventory.quantity}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                Text("units", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("RM ${item.inventory.sellingPrice}", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.weight(1f))
                            OutlinedButton(onClick = { onAdjust(item.inventory.inventoryId, -1) }, modifier = Modifier.size(32.dp), contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Filled.Remove, "−", Modifier.size(14.dp))
                            }
                            OutlinedButton(onClick = { onAdjust(item.inventory.inventoryId, 1) }, modifier = Modifier.size(32.dp), contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Filled.Add, "+", Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicineDetailDialog(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onSave: (Medicine) -> Unit,
    onDelete: () -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(medicine.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (medicine.brand.isNotBlank()) Text("Brand: ${medicine.brand}")
                if (medicine.category.isNotBlank()) Text("Category: ${medicine.category}")
                Text("Unit: ${medicine.unit}")
                Text("Prescription: ${if (medicine.requiresPrescription) "Required" else "Not required"}")
                Text("Status: ${if (medicine.isActive) "Active" else "Inactive"}")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showEdit = true }) { Text("Edit") }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteConfirm = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
        }
    )

    if (showEdit) {
        EditMedicineDialog(
            medicine = medicine,
            onDismiss = { showEdit = false },
            onSave = {
                onSave(it)
                showEdit = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Medicine?") },
            text = { Text("${medicine.name} and its inventory will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun InventoryDetailDialog(
    item: InventoryWithMedicine,
    onDismiss: () -> Unit,
    onAdjust: (Int) -> Unit,
    onSave: (MedicineInventory) -> Unit,
    onDelete: () -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.medicine?.name ?: "Inventory Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quantity: ${item.inventory.quantity}")
                Text("Selling Price: RM ${item.inventory.sellingPrice}")
                Text("Cost: RM ${item.inventory.unitPrice}")
                Text("Batch: ${item.inventory.batchNumber.ifBlank { "—" }}")
                item.inventory.expiryDate?.let { Text("Expiry: ${sdf.format(Date(it))}") }
                Text("Supplier: ${item.inventory.supplierName.ifBlank { "—" }}")
                Text("Reorder Level: ${item.inventory.reorderLevel}")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onAdjust(-1) }) { Text("-1") }
                TextButton(onClick = { onAdjust(1) }) { Text("+1") }
                Button(onClick = { showEdit = true }) { Text("Edit") }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteConfirm = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
        }
    )

    if (showEdit) {
        EditInventoryDialog(
            inventory = item.inventory,
            onDismiss = { showEdit = false },
            onSave = {
                onSave(it)
                showEdit = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Inventory Item?") },
            text = { Text("This stock entry will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Boolean, Int, Double, Double, Long?) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("tablet") }
    var rxReq by remember { mutableStateOf(false) }
    var qty by remember { mutableStateOf("0") }
    var unitPx by remember { mutableStateOf("") }
    var sellPx by remember { mutableStateOf("") }
    var expDate by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medicine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                AppDropdown(label = "Unit", selected = unit, options = listOf("tablet", "capsule", "ml", "vial", "pipette", "sachet", "tube"), onSelect = { unit = listOf("tablet", "capsule", "ml", "vial", "pipette", "sachet", "tube")[it] })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rxReq, onCheckedChange = { rxReq = it })
                    Text("Requires Prescription")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Initial Qty") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = unitPx, onValueChange = { unitPx = it }, label = { Text("Cost (RM)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = sellPx, onValueChange = { sellPx = it }, label = { Text("Price (RM)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(expDate?.let { "Exp: ${sdf.format(Date(it))}" } ?: "Expiry date (optional)", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            expDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }) { Text("Pick") }
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) {
                    error = "Name is required"
                    return@Button
                }
                onConfirm(name, brand, category, unit, rxReq, qty.toIntOrNull() ?: 0, unitPx.toDoubleOrNull() ?: 0.0, sellPx.toDoubleOrNull() ?: 0.0, expDate)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditMedicineDialog(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onSave: (Medicine) -> Unit
) {
    var name by remember { mutableStateOf(medicine.name) }
    var brand by remember { mutableStateOf(medicine.brand) }
    var category by remember { mutableStateOf(medicine.category) }
    var unit by remember { mutableStateOf(medicine.unit) }
    var rxReq by remember { mutableStateOf(medicine.requiresPrescription) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Medicine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                AppDropdown(label = "Unit", selected = unit, options = listOf("tablet", "capsule", "ml", "vial", "pipette", "sachet", "tube"), onSelect = { unit = listOf("tablet", "capsule", "ml", "vial", "pipette", "sachet", "tube")[it] })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rxReq, onCheckedChange = { rxReq = it })
                    Text("Requires Prescription")
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) {
                    error = "Name is required"
                    return@Button
                }
                onSave(
                    medicine.copy(
                        name = name.trim(),
                        brand = brand.trim(),
                        category = category.trim(),
                        unit = unit,
                        requiresPrescription = rxReq
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditInventoryDialog(
    inventory: MedicineInventory,
    onDismiss: () -> Unit,
    onSave: (MedicineInventory) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance().apply { inventory.expiryDate?.let { timeInMillis = it } } }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    var quantity by remember { mutableStateOf(inventory.quantity.toString()) }
    var unitPrice by remember { mutableStateOf(inventory.unitPrice.toString()) }
    var sellingPrice by remember { mutableStateOf(inventory.sellingPrice.toString()) }
    var batchNumber by remember { mutableStateOf(inventory.batchNumber) }
    var reorderLevel by remember { mutableStateOf(inventory.reorderLevel.toString()) }
    var supplierName by remember { mutableStateOf(inventory.supplierName) }
    var expiryDate by remember { mutableStateOf(inventory.expiryDate) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Inventory") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = reorderLevel, onValueChange = { reorderLevel = it }, label = { Text("Reorder") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text("Cost") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = { Text("Price") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = batchNumber, onValueChange = { batchNumber = it }, label = { Text("Batch Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = supplierName, onValueChange = { supplierName = it }, label = { Text("Supplier") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(expiryDate?.let { "Expiry: ${sdf.format(Date(it))}" } ?: "Expiry date (optional)", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            expiryDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }) { Text("Pick") }
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedQty = quantity.toIntOrNull()
                val parsedReorder = reorderLevel.toIntOrNull()
                val parsedUnitPrice = unitPrice.toDoubleOrNull()
                val parsedSelling = sellingPrice.toDoubleOrNull()
                if (parsedQty == null || parsedReorder == null || parsedUnitPrice == null || parsedSelling == null) {
                    error = "Please enter valid numeric values"
                    return@Button
                }
                onSave(
                    inventory.copy(
                        quantity = parsedQty,
                        reorderLevel = parsedReorder,
                        unitPrice = parsedUnitPrice,
                        sellingPrice = parsedSelling,
                        batchNumber = batchNumber.trim(),
                        supplierName = supplierName.trim(),
                        expiryDate = expiryDate,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
