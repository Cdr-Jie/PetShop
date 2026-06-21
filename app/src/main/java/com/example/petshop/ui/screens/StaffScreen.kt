package com.example.petshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var showAdd by remember { mutableStateOf(false) }
    var selectedStaff by remember { mutableStateOf<Staff?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Management", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Filled.PersonAdd, contentDescription = "Add Staff")
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
                    StaffCard(staff = staff, onOpen = { selectedStaff = staff })
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

    selectedStaff?.let { staff ->
        StaffDetailDialog(
            staff = staff,
            onDismiss = { selectedStaff = null },
            onToggle = {
                vm.toggleActive(staff)
                selectedStaff = null
            },
            onSave = { updated ->
                vm.updateStaff(updated)
                selectedStaff = null
            },
            onDelete = {
                vm.deleteStaff(staff)
                selectedStaff = null
            }
        )
    }
}

@Composable
private fun StaffCard(staff: Staff, onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
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
                    SuggestionChip(onClick = {}, label = { Text(staff.role.name, style = MaterialTheme.typography.labelSmall) })
                    if (!staff.isActive) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Inactive", style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        )
                    }
                }
                if (staff.specialization.isNotBlank()) {
                    Text(staff.specialization, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                Text(staff.phone, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StaffDetailDialog(
    staff: Staff,
    onDismiss: () -> Unit,
    onToggle: () -> Unit,
    onSave: (Staff) -> Unit,
    onDelete: () -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${staff.firstName} ${staff.lastName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Role: ${staff.role.name}")
                if (staff.specialization.isNotBlank()) Text("Specialization: ${staff.specialization}")
                Text("Phone: ${staff.phone}")
                if (staff.email.isNotBlank()) Text("Email: ${staff.email}")
                Text("Status: ${if (staff.isActive) "Active" else "Inactive"}")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onToggle) { Text(if (staff.isActive) "Deactivate" else "Activate") }
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
        EditStaffDialog(
            staff = staff,
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
            title = { Text("Remove Staff?") },
            text = { Text("${staff.firstName} ${staff.lastName} will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, StaffRole, String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(StaffRole.VETERINARIAN) }
    var specialization by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Staff Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                AppDropdown(
                    label = "Role",
                    selected = role.name,
                    options = StaffRole.entries.map { it.name },
                    onSelect = { role = StaffRole.entries[it] }
                )
                OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (firstName.isBlank() || lastName.isBlank() || phone.isBlank()) {
                    error = "Name and phone are required"
                    return@Button
                }
                onConfirm(firstName, lastName, phone, email, role, specialization)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditStaffDialog(
    staff: Staff,
    onDismiss: () -> Unit,
    onSave: (Staff) -> Unit
) {
    var firstName by remember { mutableStateOf(staff.firstName) }
    var lastName by remember { mutableStateOf(staff.lastName) }
    var phone by remember { mutableStateOf(staff.phone) }
    var email by remember { mutableStateOf(staff.email) }
    var role by remember { mutableStateOf(staff.role) }
    var specialization by remember { mutableStateOf(staff.specialization) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Staff") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                AppDropdown(
                    label = "Role",
                    selected = role.name,
                    options = StaffRole.entries.map { it.name },
                    onSelect = { role = StaffRole.entries[it] }
                )
                OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (firstName.isBlank() || lastName.isBlank() || phone.isBlank()) {
                    error = "Name and phone are required"
                    return@Button
                }
                onSave(
                    staff.copy(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        role = role,
                        specialization = specialization.trim()
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
