package com.example.petshop.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.petshop.data.dao.*
import com.example.petshop.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Client::class,
        Pet::class,
        Staff::class,
        Service::class,
        Appointment::class,
        Medicine::class,
        MedicineInventory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetShopDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun petDao(): PetDao
    abstract fun staffDao(): StaffDao
    abstract fun serviceDao(): ServiceDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun medicineDao(): MedicineDao
    abstract fun medicineInventoryDao(): MedicineInventoryDao

    companion object {
        @Volatile
        private var INSTANCE: PetShopDatabase? = null

        fun getInstance(context: Context): PetShopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetShopDatabase::class.java,
                    "petshop_database"
                )
                    .addCallback(SeedCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Populates the database with seed data on first creation.
     */
    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedData(database)
                }
            }
        }

        private suspend fun seedData(database: PetShopDatabase) {
            // Default admin account  (password: "admin123" – store a real hash in production)
            database.userDao().insert(
                User(
                    username = "admin",
                    passwordHash = "admin123",
                    email = "admin@petshop.com",
                    role = UserRole.ADMIN
                )
            )

            // Normal client account for end-user testing (username: user1, password: user123)
            val userClientId = database.userDao().insert(
                User(
                    username = "user1",
                    passwordHash = "user123",
                    email = "user1@petshop.com",
                    role = UserRole.CLIENT
                )
            ).toInt()

            val clientId = database.clientDao().insert(
                Client(
                    userId = userClientId,
                    firstName = "Alex",
                    lastName = "Tan",
                    phone = "0123456789",
                    email = "user1@petshop.com",
                    address = "Kuala Lumpur"
                )
            ).toInt()

            database.petDao().insert(
                Pet(
                    clientId = clientId,
                    name = "Milo",
                    species = "Cat",
                    breed = "Domestic Shorthair",
                    gender = PetGender.MALE,
                    weightKg = 4.3f
                )
            )

            // Sample services
            val services = listOf(
                Service(name = "General Consultation",   category = "Consultation", price = 500.0,  durationMinutes = 30),
                Service(name = "Vaccination",            category = "Preventive",   price = 350.0,  durationMinutes = 15),
                Service(name = "Deworming",              category = "Preventive",   price = 200.0,  durationMinutes = 15),
                Service(name = "Grooming – Basic",       category = "Grooming",     price = 400.0,  durationMinutes = 60),
                Service(name = "Grooming – Full",        category = "Grooming",     price = 750.0,  durationMinutes = 120),
                Service(name = "Dental Cleaning",        category = "Dental",       price = 1200.0, durationMinutes = 60),
                Service(name = "Spay / Neuter",          category = "Surgery",      price = 3000.0, durationMinutes = 120),
                Service(name = "Blood Work",             category = "Diagnostics",  price = 800.0,  durationMinutes = 30),
                Service(name = "X-Ray",                  category = "Diagnostics",  price = 1000.0, durationMinutes = 30),
                Service(name = "Emergency Care",         category = "Emergency",    price = 2000.0, durationMinutes = 60)
            )
            services.forEach { database.serviceDao().insert(it) }

            // Sample medicines
            val medicines = listOf(
                Medicine(name = "Amoxicillin",    brand = "Generic",   category = "Antibiotic",    unit = "tablet",  requiresPrescription = true),
                Medicine(name = "Metronidazole",  brand = "Flagyl",    category = "Antibiotic",    unit = "tablet",  requiresPrescription = true),
                Medicine(name = "Ivermectin",     brand = "Heartgard", category = "Antiparasitic", unit = "tablet",  requiresPrescription = true),
                Medicine(name = "Rabies Vaccine", brand = "Nobivac",   category = "Vaccine",       unit = "vial",    requiresPrescription = false),
                Medicine(name = "Distemper Combo",brand = "DHPPiL",    category = "Vaccine",       unit = "vial",    requiresPrescription = false),
                Medicine(name = "Frontline Plus", brand = "Frontline", category = "Antiparasitic", unit = "pipette", requiresPrescription = false),
                Medicine(name = "Meloxicam",      brand = "Metacam",   category = "NSAID",         unit = "ml",      requiresPrescription = true),
                Medicine(name = "Vitamin B Complex", brand = "Generic",category = "Supplement",    unit = "tablet",  requiresPrescription = false)
            )
            medicines.forEach { database.medicineDao().insert(it) }
        }
    }
}
