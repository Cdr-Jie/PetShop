package com.example.petshop.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.entity.Medicine
import com.example.petshop.data.relation.InventoryWithMedicine
import com.example.petshop.ui.viewmodel.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    onBack: () -> Unit,
    vm: MedicineViewModel = viewModel()
) {
    val medicines by vm.medicines.collectAsState()
    val inventory by vm.inventory.collectAsState()
    val lowStock  by vm.lowStock.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAdd     by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
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
                Icon(Icons.Filled.Add, "Add Medicine")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Low stock warning
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
                    Text(
                        "${lowStock.size} item(s) at low stock level",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Medicines") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Inventory") })
            }

            when (selectedTab) {
                0 -> MedicinesList(medicines, onDelete = vm::deleteMedicine)
                1 -> InventoryList(inventory, onAdjust = { invId, delta -> vm.adjustStock(invId, delta) })
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
}

@Composable
private fun MedicinesList(medicines: List<Medicine>, onDelete: (Medicine) -> Unit) {
    if (medicines.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No medicines", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(medicines, key = { it.medicineId }) { med ->
                var showConfirm by remember { mutableStateOf(false) }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Medication, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(med.name, fontWeight = FontWeight.SemiBold)
                            Text("${med.brand}  •  ${med.category}", style = MaterialTheme.typography.bodySmall)
                            Text("Unit: ${med.unit}${if (med.requiresPrescription) "  •  Rx" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        IconButton(onClick = { showConfirm = true }) {
                            Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                if (showConfirm) {
                    AlertDialog(
                        onDismissRequest = { showConfirm = false },
                        title   = { Text("Delete Medicine?") },
                        text    = { Text("${med.name} and its inventory will be removed.") },
                        confirmButton = { Button(onClick = { onDelete(med); showConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
                        dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } }
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryList(inventory: List<InventoryWithMedicine>, onAdjust: (Int, Int) -> Unit) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    if (inventory.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No inventory records", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(inventory, key = { it.inventory.inventoryId }) { item ->
                val isLow = item.inventory.quantity <= item.inventory.reorderLevel
                Card(
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
                                Text(
                                    "${item.inventory.quantity}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Boolean, Int, Double, Double, Long?) -> Unit
) {
    val context  = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val sdf      = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    var name     by remember { mutableStateOf("") }
    var brand    by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit     by remember { mutableStateOf("tablet") }
    var rxReq    by remember { mutableStateOf(false) }
    var qty      by remember { mutableStateOf("0") }
    var unitPx   by remember { mutableStateOf("") }
    var sellPx   by remember { mutableStateOf("") }
    var expDate  by remember { mutableStateOf<Long?>(null) }
    var error    by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medicine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                AppDropdown(
                    label    = "Unit",
                    selected = unit,
                    options  = listOf("tablet", "capsule", "ml", "vial", "pipette", "sachet", "tube"),
                    onSelect = { unit = listOf("tablet", "capsule", "ml", "vial", "pipette", "sachet", "tube")[it] }
                )
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
                    Text(
                        expDate?.let { "Exp: ${sdf.format(Date(it))}" } ?: "Expiry date (optional)",
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d); expDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }) { Text("Pick") }
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) { error = "Name is required"; return@Button }
                onConfirm(name, brand, category, unit, rxReq, qty.toIntOrNull() ?: 0, unitPx.toDoubleOrNull() ?: 0.0, sellPx.toDoubleOrNull() ?: 0.0, expDate)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
