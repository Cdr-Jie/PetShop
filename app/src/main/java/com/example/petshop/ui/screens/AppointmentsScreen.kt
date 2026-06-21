package com.example.petshop.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.entity.*
import com.example.petshop.data.relation.AppointmentWithDetails
import com.example.petshop.ui.components.IosTimeSlider
import com.example.petshop.ui.navigation.SessionManager
import com.example.petshop.ui.theme.*
import com.example.petshop.ui.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private fun formatDt(millis: Long): String =
    SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault()).format(Date(millis))

private fun statusColor(status: AppointmentStatus): Color = when (status) {
    AppointmentStatus.PENDING     -> StatusPending
    AppointmentStatus.CONFIRMED   -> StatusConfirmed
    AppointmentStatus.IN_PROGRESS -> StatusInProgress
    AppointmentStatus.COMPLETED   -> StatusCompleted
    AppointmentStatus.CANCELLED   -> StatusCancelled
    AppointmentStatus.NO_SHOW     -> StatusNoShow
}

private data class StatusChangeRequest(
    val appointmentId: Int,
    val currentStatus: AppointmentStatus,
    val targetStatus: AppointmentStatus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onBack: () -> Unit,
    vm: AppointmentViewModel = viewModel()
) {
    val appointments by vm.appointments.collectAsState()
    val clients by vm.clients.collectAsState()
    val staffList by vm.staffList.collectAsState()
    val services by vm.services.collectAsState()
    val petsForClient by vm.petsForClient.collectAsState()
    val addError by vm.addError.collectAsState()
    val currentUser = SessionManager.currentUser
    val isStaffView = currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.STAFF

    var showAdd by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf<AppointmentStatus?>(null) }
    var pendingStatusChange by remember { mutableStateOf<StatusChangeRequest?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filtered = if (filterStatus == null) appointments
    else appointments.filter { it.appointment.status == filterStatus }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Appointments", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (isStaffView) {
                FloatingActionButton(onClick = { showAdd = true }) {
                    Icon(Icons.Filled.Add, "New Appointment")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Status filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterStatus == null,
                        onClick  = { filterStatus = null },
                        label    = { Text("All") }
                    )
                }
                items(AppointmentStatus.entries) { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick  = { filterStatus = if (filterStatus == status) null else status },
                        label    = { Text(status.name) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No appointments found", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.appointment.appointmentId }) {
                        AppointmentCard(
                            item = it,
                            onStatusChangeRequest = { id, current, target ->
                                if (current != target) {
                                    pendingStatusChange = StatusChangeRequest(id, current, target)
                                }
                            },
                            isStaffView = isStaffView
                        )
                    }
                }
            }
        }
    }

    if (pendingStatusChange != null) {
        val request = pendingStatusChange!!
        AlertDialog(
            onDismissRequest = { pendingStatusChange = null },
            title = { Text("Change appointment status?") },
            text = {
                Text("Update from ${request.currentStatus.name} to ${request.targetStatus.name}? The app will check for scheduling conflicts first.")
            },
            confirmButton = {
                Button(onClick = {
                    vm.updateStatus(request.appointmentId, request.targetStatus) { success, message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                    pendingStatusChange = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingStatusChange = null }) { Text("Cancel") }
            }
        )
    }

    if (showAdd && isStaffView) {
        AddAppointmentDialog(
            clients = clients,
            petsForClient = petsForClient,
            staffList = staffList,
            services = services,
            externalError = addError,
            onClientSelected = vm::selectClient,
            onDismiss = {
                vm.clearAddError()
                showAdd = false
            },
            onConfirm = { clientId, petId, staffId, serviceId, scheduledAt, notes ->
                vm.addAppointment(clientId, petId, staffId, serviceId, scheduledAt, notes) { success ->
                    if (success) {
                        vm.clearAddError()
                        showAdd = false
                    }
                }
            }
        )
    }
}

@Composable
private fun AppointmentCard(
    item: AppointmentWithDetails,
    onStatusChangeRequest: (Int, AppointmentStatus, AppointmentStatus) -> Unit,
    isStaffView: Boolean
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    val apt = item.appointment

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${item.client?.firstName ?: "?"} ${item.client?.lastName ?: ""}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Pet: ${item.pet?.name ?: "?"} (${item.pet?.species ?: ""})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isStaffView) {
                    Box {
                        SuggestionChip(
                            onClick = { showStatusMenu = true },
                            label   = { Text(apt.status.name, style = MaterialTheme.typography.labelSmall) },
                            colors  = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = statusColor(apt.status).copy(alpha = 0.15f),
                                labelColor     = statusColor(apt.status)
                            )
                        )
                        DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                            AppointmentStatus.entries.forEach { s ->
                                DropdownMenuItem(
                                    text    = { Text(s.name) },
                                    onClick = {
                                        onStatusChangeRequest(apt.appointmentId, apt.status, s)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    SuggestionChip(
                        onClick = {},
                        enabled = false,
                        label   = { Text(apt.status.name, style = MaterialTheme.typography.labelSmall) },
                        colors  = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = statusColor(apt.status).copy(alpha = 0.15f),
                            labelColor     = statusColor(apt.status)
                        )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccessTime, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.width(4.dp))
                Text(formatDt(apt.scheduledAt), style = MaterialTheme.typography.bodySmall)
            }
            if (apt.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("📝 ${apt.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAppointmentDialog(
    clients: List<Client>,
    petsForClient: List<Pet>,
    staffList: List<Staff>,
    services: List<Service>,
    externalError: String?,
    onClientSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int?, Int?, Long, String) -> Unit
) {
    val context  = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var selectedClient  by remember { mutableStateOf<Client?>(null) }
    var selectedPet     by remember { mutableStateOf<Pet?>(null) }
    var selectedStaff   by remember { mutableStateOf<Staff?>(null) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var scheduledAt     by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notes           by remember { mutableStateOf("") }
    var error           by remember { mutableStateOf("") }
    var showTimeSlider  by remember { mutableStateOf(false) }

    LaunchedEffect(selectedClient?.clientId, petsForClient) {
        selectedPet = selectedPet?.takeIf { pet ->
            petsForClient.any { it.petId == pet.petId }
        }
    }

    val hour = Calendar.getInstance().apply { timeInMillis = scheduledAt }.get(Calendar.HOUR_OF_DAY)
    val minute = Calendar.getInstance().apply { timeInMillis = scheduledAt }.get(Calendar.MINUTE)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Appointment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Client dropdown
                AppDropdown(
                    label    = "Client *",
                    selected = selectedClient?.let { "${it.firstName} ${it.lastName}" } ?: "",
                    options  = clients.map { "${it.firstName} ${it.lastName}" },
                    onSelect = { idx ->
                        selectedClient = clients[idx]
                        selectedPet = null
                        onClientSelected(clients[idx].clientId)
                    }
                )
                // Pet dropdown (filtered)
                AppDropdown(
                    label    = "Pet *",
                    selected = selectedPet?.name ?: "",
                    options  = petsForClient.map { it.name },
                    onSelect = { idx -> selectedPet = petsForClient[idx] }
                )
                // Staff dropdown (optional)
                AppDropdown(
                    label    = "Staff (optional)",
                    selected = selectedStaff?.let { "${it.firstName} ${it.lastName}" } ?: "None",
                    options  = listOf("None") + staffList.map { "${it.firstName} ${it.lastName}" },
                    onSelect = { idx -> selectedStaff = if (idx == 0) null else staffList[idx - 1] }
                )
                // Service dropdown (optional)
                AppDropdown(
                    label    = "Service (optional)",
                    selected = selectedService?.name ?: "None",
                    options  = listOf("None") + services.map { it.name },
                    onSelect = { idx -> selectedService = if (idx == 0) null else services[idx - 1] }
                )
                // Date/Time
                Text(
                    "📅  ${formatDt(scheduledAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        DatePickerDialog(
                            context,
                            { _, y, m, d -> calendar.set(y, m, d); scheduledAt = calendar.timeInMillis },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }, modifier = Modifier.weight(1f)) { Text("Date") }

                    OutlinedButton(onClick = {
                        showTimeSlider = true
                    }, modifier = Modifier.weight(1f)) { Text("Time") }
                }
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") }, maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank())
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                if (!externalError.isNullOrBlank())
                    Text(externalError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedClient == null || selectedPet == null) { error = "Client and Pet are required"; return@Button }
                onConfirm(
                    selectedClient!!.clientId,
                    selectedPet!!.petId,
                    selectedStaff?.staffId,
                    selectedService?.serviceId,
                    scheduledAt, notes
                )
            }) { Text("Book") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showTimeSlider) {
        AlertDialog(
            onDismissRequest = { showTimeSlider = false },
            title = { Text("Select Time") },
            text = {
                IosTimeSlider(
                    hour = hour,
                    minute = minute,
                    onTimeChange = { h, m ->
                        calendar.set(Calendar.HOUR_OF_DAY, h)
                        calendar.set(Calendar.MINUTE, m)
                        scheduledAt = calendar.timeInMillis
                    }
                )
            },
            confirmButton = {
                Button(onClick = { showTimeSlider = false }) { Text("Done") }
            }
        )
    }
}

/** Reusable dropdown composable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdown(label: String, selected: String, options: List<String>, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected, onValueChange = {},
            readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { i, opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(i); expanded = false })
            }
        }
    }
}

