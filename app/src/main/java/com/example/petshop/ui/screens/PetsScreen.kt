package com.example.petshop.ui.screens

import android.app.DatePickerDialog
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
import com.example.petshop.data.entity.Pet
import com.example.petshop.data.entity.PetGender
import com.example.petshop.ui.viewmodel.PetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    clientId: Int,
    onBack: () -> Unit
) {
    val vm: PetViewModel = viewModel(factory = PetViewModel.factory(clientId))
    val pets   by vm.pets.collectAsState()
    val client by vm.client.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pets", fontWeight = FontWeight.Bold)
                        client?.let { Text("${it.firstName} ${it.lastName}", style = MaterialTheme.typography.labelSmall) }
                    }
                },
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
                Icon(Icons.Filled.Add, "Add Pet")
            }
        }
    ) { padding ->
        if (pets.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No pets registered", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(pets, key = { it.petId }) { pet ->
                    PetCard(pet, onDelete = { vm.deletePet(pet) })
                }
            }
        }
    }

    if (showAdd) {
        AddPetDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, species, breed, gender, birthDate, weight ->
                vm.addPet(name, species, breed, gender, birthDate, weight)
                showAdd = false
            }
        )
    }
}

@Composable
private fun PetCard(pet: Pet, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Pets, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(pet.name, fontWeight = FontWeight.SemiBold)
                Text("${pet.species}  •  ${pet.breed.ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall)
                Text("Gender: ${pet.gender.name}  •  ${pet.weightKg?.let { "$it kg" } ?: ""}", style = MaterialTheme.typography.bodySmall)
                pet.birthDate?.let {
                    Text("Born: ${sdf.format(Date(it))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title   = { Text("Remove Pet?") },
            text    = { Text("${pet.name} will be removed from the system.") },
            confirmButton = {
                Button(onClick = { onDelete(); showConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, PetGender, Long?, Float?) -> Unit
) {
    val context  = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var name      by remember { mutableStateOf("") }
    var species   by remember { mutableStateOf("Dog") }
    var breed     by remember { mutableStateOf("") }
    var gender    by remember { mutableStateOf(PetGender.UNKNOWN) }
    var birthDate by remember { mutableStateOf<Long?>(null) }
    var weight    by remember { mutableStateOf("") }
    var error     by remember { mutableStateOf("") }

    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Pet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Pet Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                AppDropdown(
                    label    = "Species",
                    selected = species,
                    options  = listOf("Dog", "Cat", "Bird", "Rabbit", "Hamster", "Fish", "Reptile", "Other"),
                    onSelect = { species = listOf("Dog", "Cat", "Bird", "Rabbit", "Hamster", "Fish", "Reptile", "Other")[it] }
                )
                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Breed") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                AppDropdown(
                    label    = "Gender",
                    selected = gender.name,
                    options  = PetGender.values().map { it.name },
                    onSelect = { gender = PetGender.values()[it] }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        birthDate?.let { "Born: ${sdf.format(Date(it))}" } ?: "Birth date (optional)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d); birthDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }) { Text("Pick") }
                }
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) { error = "Pet name is required"; return@Button }
                onConfirm(name, species, breed, gender, birthDate, weight.toFloatOrNull())
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

