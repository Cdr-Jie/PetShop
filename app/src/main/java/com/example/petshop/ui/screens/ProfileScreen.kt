package com.example.petshop.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import com.example.petshop.ui.components.PetShopTopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.petshop.data.entity.Pet
import com.example.petshop.data.entity.User
import com.example.petshop.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val myUser by vm.myUser.collectAsState()
    val myClient by vm.myClient.collectAsState()
    val myPets by vm.myPets.collectAsState()
    val displayUser = myUser ?: user

    var showEditProfile by remember { mutableStateOf(false) }
    var editingPet by remember { mutableStateOf<Pet?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            PetShopTopAppBar(title = "Profile", onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Card
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(imageUri = displayUser?.avatarImageUri)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            displayUser?.username ?: "User",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${displayUser?.firstName ?: ""} ${displayUser?.lastName ?: ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!displayUser?.email.isNullOrBlank()) {
                                            Text(
                                                displayUser?.email ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (!displayUser?.phone.isNullOrBlank()) {
                                            Text(
                                                displayUser?.phone ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { showEditProfile = true }) {
                                Icon(Icons.Filled.Edit, "Edit Profile")
                            }
                        }
                    }
                }
            }

            if (!saveError.isNullOrBlank()) {
                item {
                    Text(
                        saveError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Logout Button
            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Logout, "Logout", modifier = Modifier.padding(end = 8.dp))
                    Text("Logout")
                }
            }

            // My Pets Section
            if (myPets.isNotEmpty()) {
                item {
                    Text(
                        "My Pets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(myPets, key = { it.petId }) { pet ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PetAvatar(imageUri = pet.profileImageUri)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    pet.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${pet.species} ${pet.breed}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (pet.weightKg != null) {
                                    Text(
                                        "Weight: ${pet.weightKg}kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { editingPet = pet }) {
                                Icon(Icons.Filled.Edit, "Edit Pet")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditProfile && displayUser != null) {
        EditProfileDialog(
            user = displayUser,
            onDismiss = { showEditProfile = false },
            onSave = { firstName, lastName, email, phone, avatarUri ->
                vm.updateMyProfile(firstName, lastName, email, phone, avatarUri) { success, message ->
                    if (success) {
                        saveError = null
                        showEditProfile = false
                    } else {
                        saveError = message
                    }
                }
            }
        )
    }

    if (editingPet != null) {
        EditPetDialog(
            pet = editingPet!!,
            onDismiss = { editingPet = null },
            onSave = {
                vm.updatePet(it) { success, message ->
                    if (success) {
                        saveError = null
                        editingPet = null
                    } else {
                        saveError = message
                    }
                }
            }
        )
    }
}

@Composable
private fun UserAvatar(imageUri: String?) {
    if (!imageUri.isNullOrBlank() && !imageUri.startsWith("default://")) {
        AsyncImage(
            model = imageUri,
            contentDescription = "User image",
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun PetAvatar(imageUri: String?) {
    if (!imageUri.isNullOrBlank() && !imageUri.startsWith("default://")) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Pet image",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var avatarImageUri by remember { mutableStateOf(user.avatarImageUri) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { avatarImageUri = it.toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Profile Image",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(imageUri = avatarImageUri)
                    OutlinedButton(onClick = { launcher.launch("image/*") }) {
                        Text("Choose Image")
                    }
                }
                if (avatarImageUri.isNotBlank()) {
                    Text(
                        avatarImageUri,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(firstName, lastName, email, phone, avatarImageUri)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditPetDialog(
    pet: Pet,
    onDismiss: () -> Unit,
    onSave: (Pet) -> Unit
) {
    var name by remember { mutableStateOf(pet.name) }
    var breed by remember { mutableStateOf(pet.breed) }
    var weight by remember { mutableStateOf(pet.weightKg?.toString() ?: "") }
    var color by remember { mutableStateOf(pet.color) }
    var imageUri by remember { mutableStateOf(pet.profileImageUri) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${pet.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Pet Image",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PetAvatar(imageUri = imageUri)
                    OutlinedButton(onClick = { launcher.launch("image/*") }) {
                        Text("Choose Image")
                    }
                }
                if (imageUri.isNotBlank()) {
                    Text(
                        imageUri,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Start
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(pet.copy(
                    name = name,
                    breed = breed,
                    weightKg = weight.toFloatOrNull(),
                    color = color,
                    profileImageUri = imageUri
                ))
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


