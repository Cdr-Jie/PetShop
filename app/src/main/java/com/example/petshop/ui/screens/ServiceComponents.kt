package com.example.petshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petshop.data.entity.Service

@Composable
fun ServiceListCard(service: Service, onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = if (!service.isActive) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(service.name, fontWeight = FontWeight.SemiBold)
                if (!service.isActive) {
                    Text("(Inactive)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Text(service.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            if (service.description.isNotBlank()) {
                Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 2)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("RM ${service.price}", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                Text("${service.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun ServiceDetailsDialog(
    service: Service,
    onDismiss: () -> Unit,
    onToggle: () -> Unit,
    onSave: (Service) -> Unit,
    onDelete: () -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(service.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category: ${service.category}")
                if (service.description.isNotBlank()) Text(service.description)
                Text("Price: RM ${service.price}")
                Text("Duration: ${service.durationMinutes} min")
                Text("Status: ${if (service.isActive) "Active" else "Inactive"}")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onToggle) { Text(if (service.isActive) "Deactivate" else "Activate") }
                Button(onClick = { showEdit = true }) { Text("Edit") }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )

    if (showEdit) {
        EditServiceDialog(
            service = service,
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
            title = { Text("Delete Service?") },
            text = { Text("\"${service.name}\" will be permanently deleted.") },
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
fun ServiceAddDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var error by remember { mutableStateOf("") }

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
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || category.isBlank()) {
                    error = "Name and category are required"
                    return@Button
                }
                val parsedPrice = price.toDoubleOrNull()
                if (parsedPrice == null) {
                    error = "Enter a valid price"
                    return@Button
                }
                onConfirm(name, category, desc, parsedPrice, duration.toIntOrNull() ?: 30)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditServiceDialog(
    service: Service,
    onDismiss: () -> Unit,
    onSave: (Service) -> Unit
) {
    var name by remember { mutableStateOf(service.name) }
    var category by remember { mutableStateOf(service.category) }
    var desc by remember { mutableStateOf(service.description) }
    var price by remember { mutableStateOf(service.price.toString()) }
    var duration by remember { mutableStateOf(service.durationMinutes.toString()) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Service") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (RM) *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (min)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || category.isBlank()) {
                    error = "Name and category are required"
                    return@Button
                }
                val parsedPrice = price.toDoubleOrNull()
                if (parsedPrice == null) {
                    error = "Enter a valid price"
                    return@Button
                }
                onSave(
                    service.copy(
                        name = name.trim(),
                        category = category.trim(),
                        description = desc.trim(),
                        price = parsedPrice,
                        durationMinutes = duration.toIntOrNull() ?: service.durationMinutes
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


