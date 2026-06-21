package com.example.petshop.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.entity.Service
import com.example.petshop.ui.viewmodel.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    onBack: () -> Unit,
    vm: ServiceViewModel = viewModel()
) {
    val services   by vm.services.collectAsState()
    val categories by vm.categories.collectAsState()
    var showAdd    by remember { mutableStateOf(false) }
    var filterCat  by remember { mutableStateOf<String?>(null) }

    val filtered = if (filterCat == null) services else services.filter { it.category == filterCat }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Services", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Filled.Add, "Add Service")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Category filter
            if (categories.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(selected = filterCat == null, onClick = { filterCat = null }, label = { Text("All") })
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = filterCat == cat,
                            onClick  = { filterCat = if (filterCat == cat) null else cat },
                            label    = { Text(cat) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No services yet", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.serviceId }) { service ->
                        ServiceCard(service, onToggle = { vm.toggleActive(service) }, onDelete = { vm.deleteService(service) })
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddServiceDialog(
            categories = categories,
            onDismiss  = { showAdd = false },
            onConfirm  = { name, cat, desc, price, dur ->
                vm.addService(name, cat, desc, price, dur)
                showAdd = false
            }
        )
    }
}

@Composable
private fun ServiceCard(service: Service, onToggle: () -> Unit, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = if (!service.isActive)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        else CardDefaults.cardColors()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(service.name, fontWeight = FontWeight.SemiBold)
                    if (!service.isActive) Text("(Inactive)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Text(service.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                if (service.description.isNotBlank())
                    Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("RM ${service.price}", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Text("${service.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            IconButton(onClick = onToggle) {
                Icon(
                    if (service.isActive) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                    "Toggle",
                    tint = if (service.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title   = { Text("Delete Service?") },
            text    = { Text("\"${service.name}\" will be permanently deleted.") },
            confirmButton = { Button(onClick = { onDelete(); showConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddServiceDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Int) -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var desc     by remember { mutableStateOf("") }
    var price    by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var error    by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Service") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (RM) *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (min)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || category.isBlank()) { error = "Name and category are required"; return@Button }
                val p = price.toDoubleOrNull()
                if (p == null) { error = "Enter a valid price"; return@Button }
                onConfirm(name, category, desc, p, duration.toIntOrNull() ?: 30)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
