package com.example.petshop.ui.navigation

import com.example.petshop.data.entity.User

/** Holds the currently logged-in user for the app session. */
object SessionManager {
    var currentUser: User? = null
}

sealed class AppScreens(val route: String) {
    object Login        : AppScreens("login")
    object Dashboard    : AppScreens("dashboard")
    object Appointments : AppScreens("appointments")
    object Clients      : AppScreens("clients")
    object Staff        : AppScreens("staff")
    object Services     : AppScreens("services")
    object Medicine     : AppScreens("medicine")

    object Pets : AppScreens("pets/{clientId}") {
        fun createRoute(clientId: Int) = "pets/$clientId"
    }
}

