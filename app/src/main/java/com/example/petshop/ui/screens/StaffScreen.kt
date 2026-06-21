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
import com.example.petshop.data.entity.Staff
import com.example.petshop.data.entity.StaffRole
import com.example.petshop.ui.viewmodel.StaffViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    onBack: () -> Unit,
    vm: StaffViewModel = viewModel()
) {
    val staffList by vm.staffList.collectAsState()
    var showAdd   by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Management", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Filled.PersonAdd, "Add Staff")
            }
        }
    ) { padding ->
        if (staffList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No staff members", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(staffList, key = { it.staffId }) { staff ->
                    StaffCard(
                        staff     = staff,
                        onToggle  = { vm.toggleActive(staff) },
                        onDelete  = { vm.deleteStaff(staff) }
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddStaffDialog(
            onDismiss = { showAdd = false },
            onConfirm = { fn, ln, ph, em, role, spec ->
                vm.addStaff(fn, ln, ph, em, role, spec)
                showAdd = false
            }
        )
    }
}

@Composable
private fun StaffCard(staff: Staff, onToggle: () -> Unit, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AccountCircle, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${staff.firstName} ${staff.lastName}", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label   = { Text(staff.role.name, style = MaterialTheme.typography.labelSmall) }
                    )
                    if (!staff.isActive)
                        SuggestionChip(
                            onClick = {},
                            label   = { Text("Inactive", style = MaterialTheme.typography.labelSmall) },
                            colors  = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        )
                }
                if (staff.specialization.isNotBlank())
                    Text(staff.specialization, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text(staff.phone, style = MaterialTheme.typography.bodySmall)
            }
            Column {
                IconButton(onClick = onToggle) {
                    Icon(
                        if (staff.isActive) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                        "Toggle",
                        tint = if (staff.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = { showConfirm = true }) {
                    Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title   = { Text("Remove Staff?") },
            text    = { Text("${staff.firstName} ${staff.lastName} will be removed.") },
            confirmButton = { Button(onClick = { onDelete(); showConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, StaffRole, String) -> Unit
) {
    var firstName      by remember { mutableStateOf("") }
    var lastName       by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var role           by remember { mutableStateOf(StaffRole.VETERINARIAN) }
    var specialization by remember { mutableStateOf("") }
    var error          by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Staff Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = lastName,  onValueChange = { lastName  = it }, label = { Text("Last Name *") },  modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },  modifier = Modifier.fillMaxWidth(), singleLine = true)
                AppDropdown(
                    label    = "Role",
                    selected = role.name,
                    options  = StaffRole.values().map { it.name },
                    onSelect = { role = StaffRole.values()[it] }
                )
                OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (firstName.isBlank() || lastName.isBlank() || phone.isBlank()) { error = "Name and phone are required"; return@Button }
                onConfirm(firstName, lastName, phone, email, role, specialization)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
