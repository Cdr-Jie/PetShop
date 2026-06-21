package com.example.petshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petshop.data.entity.AppointmentStatus
import com.example.petshop.data.entity.Pet
import com.example.petshop.data.entity.UserRole
import com.example.petshop.data.relation.AppointmentWithDetails
import com.example.petshop.ui.navigation.SessionManager
import com.example.petshop.ui.theme.EmeraldAccent
import com.example.petshop.ui.theme.IndigoPrimary
import com.example.petshop.ui.theme.IndigoSecondary
import com.example.petshop.ui.theme.PetAccentAmber
import com.example.petshop.ui.theme.PetAccentBlue
import com.example.petshop.ui.theme.PetAccentCoral
import com.example.petshop.ui.theme.PetAccentMint
import com.example.petshop.ui.theme.PetAccentPink
import com.example.petshop.ui.theme.SkyAccent
import com.example.petshop.ui.theme.StatusCancelled
import com.example.petshop.ui.theme.StatusCompleted
import com.example.petshop.ui.theme.StatusConfirmed
import com.example.petshop.ui.theme.StatusInProgress
import com.example.petshop.ui.theme.StatusNoShow
import com.example.petshop.ui.theme.StatusPending
import com.example.petshop.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class BottomDestination(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

private data class HomeMetric(
    val label: String,
    val value: String,
    val accent: Color,
    val onClick: () -> Unit,
)

private fun fmtTime(millis: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))

private fun todayLabel(): String =
    SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(Date())

private fun statusColor(status: AppointmentStatus): Color = when (status) {
    AppointmentStatus.PENDING -> StatusPending
    AppointmentStatus.CONFIRMED -> StatusConfirmed
    AppointmentStatus.IN_PROGRESS -> StatusInProgress
    AppointmentStatus.COMPLETED -> StatusCompleted
    AppointmentStatus.CANCELLED -> StatusCancelled
    AppointmentStatus.NO_SHOW -> StatusNoShow
}

private fun petAccent(petId: Int): Color = when (petId % 5) {
    0 -> PetAccentBlue
    1 -> PetAccentPink
    2 -> PetAccentAmber
    3 -> PetAccentMint
    else -> PetAccentCoral
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val user = SessionManager.currentUser ?: return
    val isAdmin = user.role == UserRole.ADMIN
    val isStaff = user.role == UserRole.STAFF
    val isVetAdminView = isAdmin || isStaff

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isVetAdminView) "Pet Shop Dashboard" else "Pet Care Hub") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
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
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (selectedTabId) {
                "home" -> {
                    if (isVetAdminView) {
                        StaffHomePanel(
                            username = user.firstName.ifBlank { user.username },
                            isAdmin = isAdmin,
                            schedule = todaySchedule,
                            summaryAppointmentsToday = if (isAdmin) staffSummary.appointmentsToday else todaySchedule.size,
                            summaryUpcoming = if (isAdmin) staffSummary.upcomingAppointments else todaySchedule.count {
                                it.appointment.status == AppointmentStatus.PENDING ||
                                    it.appointment.status == AppointmentStatus.CONFIRMED ||
                                    it.appointment.status == AppointmentStatus.IN_PROGRESS
                            },
                            summaryClients = staffSummary.totalClients,
                            summaryPets = staffSummary.totalPets,
                            summaryLowStock = staffSummary.lowStockItems,
                            onTapQuick = { selectedTabId = it }
                        )
                    } else {
                        ClientHomePanel(
                            username = user.firstName.ifBlank { user.username },
                            myPets = myPets,
                            myAppointmentsToday = todaySchedule,
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
                    selectedClientIdForPets?.let { cid ->
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
    isAdmin: Boolean,
    schedule: List<AppointmentWithDetails>,
    summaryAppointmentsToday: Int,
    summaryUpcoming: Int,
    summaryClients: Int,
    summaryPets: Int,
    summaryLowStock: Int,
    onTapQuick: (String) -> Unit,
) {
    val groupedSchedule = remember(schedule) {
        if (isAdmin) {
            schedule.groupBy { item ->
                item.staff?.let { "Dr. ${it.firstName} ${it.lastName}" } ?: "Unassigned Vet"
            }.toList().sortedBy { it.first }
        } else {
            listOf("Your Schedule" to schedule)
        }
    }

    val metrics = if (isAdmin) {
        listOf(
            HomeMetric("Today", summaryAppointmentsToday.toString(), IndigoPrimary) { onTapQuick("schedule") },
            HomeMetric("Upcoming", summaryUpcoming.toString(), SkyAccent) { onTapQuick("schedule") },
            HomeMetric("Clients", summaryClients.toString(), EmeraldAccent) { onTapQuick("clients") },
            HomeMetric("Low Stock", summaryLowStock.toString(), StatusPending) { onTapQuick("medicine") },
        )
    } else {
        listOf(
            HomeMetric("Today", summaryAppointmentsToday.toString(), IndigoPrimary) { onTapQuick("schedule") },
            HomeMetric("Active", summaryUpcoming.toString(), EmeraldAccent) { onTapQuick("schedule") },
            HomeMetric("Patients", schedule.mapNotNull { it.pet?.petId }.distinct().size.toString(), SkyAccent) { onTapQuick("schedule") },
            HomeMetric("Pets", summaryPets.toString(), PetAccentBlue) { onTapQuick("clients") },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroHeader(
                title = if (isAdmin) "Operations overview" else "Your care timeline",
                subtitle = if (isAdmin) {
                    "Track every vet's appointments in one place • ${todayLabel()}"
                } else {
                    "Welcome back, Dr. $username • ${todayLabel()}"
                }
            )
        }
        item {
            MetricRow(metrics = metrics)
        }
        item {
            SectionHeader(
                title = if (isAdmin) "Today's vet timetable" else "Today's appointments",
                subtitle = if (isAdmin) "Grouped by vet and colour-coded by booking status" else "Only your appointments are shown here"
            )
        }
        if (schedule.isEmpty()) {
            item { EmptyStateCard(message = "No appointments scheduled for today.") }
        } else {
            groupedSchedule.forEach { (groupTitle, itemsForGroup) ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(groupTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        itemsForGroup.forEach { appt ->
                            ScheduleTimelineCard(
                                time = fmtTime(appt.appointment.scheduledAt),
                                title = appt.pet?.name ?: "Pet",
                                subtitle = buildString {
                                    append(appt.client?.firstName ?: "Client")
                                    if (!appt.pet?.species.isNullOrBlank()) append(" • ${appt.pet?.species}")
                                },
                                detail = appt.appointment.status.name.replace('_', ' '),
                                accent = statusColor(appt.appointment.status)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientHomePanel(
    username: String,
    myPets: List<Pet>,
    myAppointmentsToday: List<AppointmentWithDetails>,
    myPetsCount: Int,
    hasLinkedClient: Boolean,
    onTapQuick: (String) -> Unit,
    onTapProfile: () -> Unit,
) {
    val metrics = listOf(
        HomeMetric("My Pets", myPetsCount.toString(), PetAccentBlue) { onTapQuick("mypets") },
        HomeMetric("Today", myAppointmentsToday.size.toString(), IndigoPrimary) { onTapQuick("schedule") },
        HomeMetric("Upcoming", myAppointmentsToday.count { it.appointment.status != AppointmentStatus.COMPLETED && it.appointment.status != AppointmentStatus.CANCELLED }.toString(), EmeraldAccent) { onTapQuick("schedule") },
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                onClick = onTapProfile,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HeroHeader(
                    title = "Hi, $username",
                    subtitle = "Your pet day at a glance • ${todayLabel()}"
                )
            }
        }
        item { MetricRow(metrics = metrics) }
        if (!hasLinkedClient) {
            item {
                EmptyStateCard(message = "This account is not linked to a client profile yet. Please ask admin to link it.")
            }
        }
        item {
            SectionHeader(
                title = "Today's bookings",
                subtitle = "Colour-coded by pet so you can spot each family member quickly"
            )
        }
        if (myAppointmentsToday.isEmpty()) {
            item { EmptyStateCard(message = "No appointments booked for today.") }
        } else {
            items(myAppointmentsToday, key = { it.appointment.appointmentId }) { item ->
                ScheduleTimelineCard(
                    time = fmtTime(item.appointment.scheduledAt),
                    title = item.pet?.name ?: "Pet",
                    subtitle = item.pet?.species ?: "Pet appointment",
                    detail = item.appointment.status.name.replace('_', ' '),
                    accent = petAccent(item.pet?.petId ?: item.appointment.petId)
                )
            }
        }
        if (myPets.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "My pets",
                    subtitle = "Quick snapshot of your companions"
                )
            }
            items(myPets.take(4), key = { it.petId }) { pet ->
                ModernPetCard(pet = pet)
            }
        }
    }
}

@Composable
private fun MyPetsPanel(myPets: List<Pet>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroHeader(
                title = "My Pets",
                subtitle = "A neat overview of every pet linked to this account"
            )
        }
        if (myPets.isEmpty()) {
            item { EmptyStateCard(message = "No pets linked to this account yet.") }
        } else {
            items(myPets, key = { it.petId }) { pet ->
                ModernPetCard(pet = pet)
            }
        }
    }
}

@Composable
private fun HeroHeader(title: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(
                    Brush.linearGradient(
                        listOf(
                            IndigoPrimary,
                            IndigoSecondary,
                            SkyAccent,
                            EmeraldAccent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f))
            }
        }
    }
}

@Composable
private fun MetricRow(metrics: List<HomeMetric>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowMetrics.forEach { metric ->
                    MetricCard(metric = metric, modifier = Modifier.weight(1f))
                }
                if (rowMetrics.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricCard(metric: HomeMetric, modifier: Modifier = Modifier) {
    Card(
        onClick = metric.onClick,
        modifier = modifier.height(104.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(metric.accent)
            )
            Text(metric.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(metric.value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ScheduleTimelineCard(
    time: String,
    title: String,
    subtitle: String,
    detail: String,
    accent: Color,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 54.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(accent)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(time, style = MaterialTheme.typography.labelLarge, color = accent)
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = accent.copy(alpha = 0.14f)
            ) {
                Text(
                    detail,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModernPetCard(pet: Pet) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(pet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${pet.species} • ${pet.breed.ifBlank { "Unknown breed" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = petAccent(pet.petId).copy(alpha = 0.14f)
            ) {
                Text(
                    pet.gender.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = petAccent(pet.petId)
                )
            }
        }
    }
}
