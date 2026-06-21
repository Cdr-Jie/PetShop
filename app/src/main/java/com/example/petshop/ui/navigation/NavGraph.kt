package com.example.petshop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petshop.ui.screens.*

@Composable
fun PetShopNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreens.Login.route) {

        // ── Login ──────────────────────────────────────────────────────────────
        composable(AppScreens.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    SessionManager.currentUser = user
                    navController.navigate(AppScreens.Dashboard.route) {
                        popUpTo(AppScreens.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard ──────────────────────────────────────────────────────────
        composable(AppScreens.Dashboard.route) {
            DashboardScreen(
                onLogout = {
                    SessionManager.currentUser = null
                    navController.navigate(AppScreens.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Appointments ───────────────────────────────────────────────────────
        composable(AppScreens.Appointments.route) {
            AppointmentsScreen(onBack = { navController.popBackStack() })
        }

        // ── Clients ────────────────────────────────────────────────────────────
        composable(AppScreens.Clients.route) {
            ClientsScreen(
                onBack     = { navController.popBackStack() },
                onViewPets = { clientId -> navController.navigate(AppScreens.Pets.createRoute(clientId)) }
            )
        }

        // ── Pets (per client) ──────────────────────────────────────────────────
        composable(
            route     = AppScreens.Pets.route,
            arguments = listOf(navArgument("clientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments!!.getInt("clientId")
            PetsScreen(clientId = clientId, onBack = { navController.popBackStack() })
        }

        // ── Medicine ───────────────────────────────────────────────────────────
        composable(AppScreens.Medicine.route) {
            MedicineScreen(onBack = { navController.popBackStack() })
        }

        // ── Staff ──────────────────────────────────────────────────────────────
        composable(AppScreens.Staff.route) {
            StaffScreen(onBack = { navController.popBackStack() })
        }

        // ── Services ───────────────────────────────────────────────────────────
        composable(AppScreens.Services.route) {
            ServicesScreen(onBack = { navController.popBackStack() })
        }
    }
}
