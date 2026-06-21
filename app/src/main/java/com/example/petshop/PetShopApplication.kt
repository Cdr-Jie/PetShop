package com.example.petshop

import android.app.Application
import com.example.petshop.data.database.PetShopDatabase

class PetShopApplication : Application() {

    val database: PetShopDatabase by lazy {
        PetShopDatabase.getInstance(this)
    }
}

