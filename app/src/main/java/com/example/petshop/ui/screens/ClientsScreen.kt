package com.example.petshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.petshop.ui.components.PetShopTopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.entity.Client
import com.example.petshop.ui.viewmodel.ClientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    onBack: () -> Unit,
    onViewPets: (Int) -> Unit,
    vm: ClientViewModel = viewModel()
) {
    val clients     by vm.clients.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    var showAdd     by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf<Client?>(null) }

    Scaffold(
        topBar = {
            PetShopTopAppBar(title = "Clients", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.PersonAdd, "Add Client")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(8.dp))
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { vm.searchQuery.value = it },
                label = { Text("Search clients…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank())
                        IconButton(onClick = { vm.searchQuery.value = "" }) {
                            Icon(Icons.Filled.Clear, "Clear")
                        }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (clients.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No clients yet", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(clients, key = { it.clientId }) { client ->
                        ClientCard(
                            client = client,
                            onOpen = { selectedClient = client },
                            onViewPets = { onViewPets(client.clientId) }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddClientDialog(
            onDismiss = { showAdd = false },
            onConfirm = { fn, ln, ph, em, addr ->
                vm.addClient(fn, ln, ph, em, addr)
                showAdd = false
            }
        )
    }

    selectedClient?.let { client ->
        ClientDetailDialog(
            client = client,
            onDismiss = { selectedClient = null },
            onViewPets = {
                selectedClient = null
                onViewPets(client.clientId)
            },
            onSave = {
                vm.updateClient(it)
                selectedClient = null
            },
            onDelete = {
                vm.deleteClient(client)
                selectedClient = null
            }
        )
    }
}

@Composable
private fun ClientCard(client: Client, onOpen: () -> Unit, onViewPets: () -> Unit) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AccountCircle, null,
                Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${client.firstName} ${client.lastName}", fontWeight = FontWeight.SemiBold)
                Text(client.phone, style = MaterialTheme.typography.bodySmall)
                if (client.email.isNotBlank())
                    Text(client.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onViewPets) {
                Icon(Icons.Filled.Pets, "Pets", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun AddClientDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var phone     by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var address   by remember { mutableStateOf("") }
    var error     by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Client") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = lastName,  onValueChange = { lastName  = it }, label = { Text("Last Name *") },  modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = phone,   onValueChange = { phone   = it }, label = { Text("Phone *") },   modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email,   onValueChange = { email   = it }, label = { Text("Email") },     modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") },   modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (firstName.isBlank() || lastName.isBlank() || phone.isBlank()) { error = "First name, last name and phone are required"; return@Button }
                onConfirm(firstName, lastName, phone, email, address)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ClientDetailDialog(
    client: Client,
    onDismiss: () -> Unit,
    onViewPets: () -> Unit,
    onSave: (Client) -> Unit,
    onDelete: () -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${client.firstName} ${client.lastName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Phone: ${client.phone}")
                if (client.email.isNotBlank()) Text("Email: ${client.email}")
                if (client.address.isNotBlank()) Text("Address: ${client.address}")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onViewPets) { Text("View Pets") }
                Button(onClick = { showEdit = true }) { Text("Edit") }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteConfirm = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Delete")
            }
        }
    )

    if (showEdit) {
        EditClientDialog(
            client = client,
            onDismiss = { showEdit = false },
            onConfirm = {
                onSave(it)
                showEdit = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Client?") },
            text = { Text("This will also delete all their pets and appointments.") },
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
private fun EditClientDialog(
    client: Client,
    onDismiss: () -> Unit,
    onConfirm: (Client) -> Unit
) {
    var firstName by remember { mutableStateOf(client.firstName) }
    var lastName by remember { mutableStateOf(client.lastName) }
    var phone by remember { mutableStateOf(client.phone) }
    var email by remember { mutableStateOf(client.email) }
    var address by remember { mutableStateOf(client.address) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Client") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (firstName.isBlank() || lastName.isBlank() || phone.isBlank()) {
                    error = "First name, last name and phone are required"
                    return@Button
                }
                onConfirm(
                    client.copy(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        address = address.trim()
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

