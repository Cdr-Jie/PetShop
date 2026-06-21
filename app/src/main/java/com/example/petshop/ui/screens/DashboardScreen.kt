package com.example.petshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.entity.Appointment
import com.example.petshop.data.entity.Pet
import com.example.petshop.data.entity.UserRole
import com.example.petshop.data.relation.AppointmentWithDetails
import com.example.petshop.ui.navigation.SessionManager
import com.example.petshop.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class BottomDestination(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

private fun fmtTime(millis: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val user = SessionManager.currentUser
    val isVetAdminView = user?.role == UserRole.ADMIN || user?.role == UserRole.STAFF

    val staffTabs = listOf(
        BottomDestination("schedule", "Schedule", Icons.Filled.DateRange),
        BottomDestination("clients", "Clients", Icons.Filled.Group),
        BottomDestination("home", "Home", Icons.Filled.Home),
        BottomDestination("medicine", "Medicine", Icons.Filled.Medication),
        BottomDestination("services", "Services", Icons.Filled.Build),
    )
    val clientTabs = listOf(
        BottomDestination("schedule", "Bookings", Icons.Filled.DateRange),
        BottomDestination("home", "Home", Icons.Filled.Home),
        BottomDestination("mypets", "My Pets", Icons.Filled.Pets),
    )
    val tabs = if (isVetAdminView) staffTabs else clientTabs

    var selectedTabId by remember { mutableStateOf("home") }
    var selectedClientIdForPets by remember { mutableStateOf<Int?>(null) }

    val staffSummary by vm.staffSummary.collectAsState()
    val clientSummary by vm.clientSummary.collectAsState()
    val myClient by vm.myClient.collectAsState()
    val myPets by vm.myPets.collectAsState()
    val todaySchedule by vm.todaySchedule.collectAsState()
    val myTodayAppointments by vm.myTodayAppointments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isVetAdminView) "Pet Shop Admin" else "Pet Shop")
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTabId == tab.id,
                        onClick = { selectedTabId = tab.id },
                        icon = { Icon(tab.icon, tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabId) {
                "home" -> {
                    if (isVetAdminView) {
                        StaffHomePanel(
                            username = user.username,
                            appointmentsToday = todaySchedule,
                            summaryAppointmentsToday = staffSummary.appointmentsToday,
                            summaryUpcoming = staffSummary.upcomingAppointments,
                            summaryClients = staffSummary.totalClients,
                            summaryPets = staffSummary.totalPets,
                            summaryLowStock = staffSummary.lowStockItems,
                            onTapQuick = { selectedTabId = it }
                        )
                    } else {
                        ClientHomePanel(
                            username = user?.username ?: "User",
                            myPets = myPets,
                            myAppointmentsToday = myTodayAppointments,
                            myPetsCount = clientSummary.myPetsCount,
                            hasLinkedClient = myClient != null,
                            onTapQuick = { selectedTabId = it },
                            onTapProfile = { selectedTabId = "profile" }
                        )
                    }
                }
                "schedule" -> AppointmentsScreen(onBack = {})
                "clients" -> ClientsScreen(
                    onBack = {},
                    onViewPets = { clientId ->
                        selectedClientIdForPets = clientId
                        selectedTabId = "petsByClient"
                    }
                )
                "petsByClient" -> {
                    val cid = selectedClientIdForPets
                    if (cid != null) {
                        PetsScreen(clientId = cid, onBack = { selectedTabId = "clients" })
                    }
                }
                "mypets" -> MyPetsPanel(myPets = myPets)
                "medicine" -> MedicineScreen(onBack = {})
                "services" -> ServicesScreen(onBack = {})
                "profile" -> ProfileScreen(
                    user = user,
                    onLogout = onLogout,
                    onBack = { selectedTabId = "home" }
                )
                else -> Text("Unknown tab", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun StaffHomePanel(
    username: String,
    appointmentsToday: List<AppointmentWithDetails>,
    summaryAppointmentsToday: Int,
    summaryUpcoming: Int,
    summaryClients: Int,
    summaryPets: Int,
    summaryLowStock: Int,
    onTapQuick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Welcome back, Dr. $username", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Today at a glance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("Today's Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        item {
            if (appointmentsToday.isEmpty()) {
                Card { Text("No appointments for today.", modifier = Modifier.padding(12.dp)) }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(appointmentsToday.take(10), key = { it.appointment.appointmentId }) { item ->
                        Card(modifier = Modifier.fillParentMaxWidth(0.82f)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(fmtTime(item.appointment.scheduledAt), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("${item.pet?.name ?: "Pet"} • ${item.client?.firstName ?: "Client"}", style = MaterialTheme.typography.bodyMedium)
                                Text(item.appointment.status.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickTile("Schedule", summaryAppointmentsToday.toString(), Modifier.weight(1f)) { onTapQuick("schedule") }
                QuickTile("Upcoming", summaryUpcoming.toString(), Modifier.weight(1f)) { onTapQuick("schedule") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickTile("Clients", summaryClients.toString(), Modifier.weight(1f)) { onTapQuick("clients") }
                QuickTile("Pets", summaryPets.toString(), Modifier.weight(1f)) { onTapQuick("clients") }
            }
        }
        item {
            QuickTile("Low Stock", summaryLowStock.toString(), Modifier.fillMaxWidth()) { onTapQuick("medicine") }
        }
    }
}

@Composable
private fun ClientHomePanel(
    username: String,
    myPets: List<Pet>,
    myAppointmentsToday: List<Appointment>,
    myPetsCount: Int,
    hasLinkedClient: Boolean,
    onTapQuick: (String) -> Unit,
    onTapProfile: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                onClick = onTapProfile) {
                Column(Modifier.padding(14.dp)) {
                    Text("Hi, $username", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Here is your pet overview", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("Today's Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        item {
            if (myAppointmentsToday.isEmpty()) {
                Card { Text("No appointments today.", modifier = Modifier.padding(12.dp)) }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(myAppointmentsToday.take(10), key = { it.appointmentId }) { item ->
                        Card(modifier = Modifier.fillParentMaxWidth(0.78f)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(fmtTime(item.scheduledAt), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("Status: ${item.status.name}", style = MaterialTheme.typography.bodyMedium)
                                Text(item.notes.ifBlank { "No notes" }, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickTile("My Pets", myPetsCount.toString(), Modifier.weight(1f)) { onTapQuick("mypets") }
                QuickTile("Schedule", myAppointmentsToday.size.toString(), Modifier.weight(1f)) { onTapQuick("schedule") }
            }
        }
        if (!hasLinkedClient) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        "This account is not linked to a client profile yet. Please ask admin to link it.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        if (myPets.isNotEmpty()) {
            item { Text("My Pets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(myPets.take(3), key = { it.petId }) { pet ->
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(pet.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${pet.species} ${pet.breed}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(pet.gender.name, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPetsPanel(myPets: List<Pet>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (myPets.isEmpty()) {
            item { Text("No pets linked to this account yet.") }
        } else {
            items(myPets, key = { it.petId }) { pet ->
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(pet.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${pet.species} ${pet.breed}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(pet.gender.name, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}


@Composable
private fun QuickTile(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(82.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

