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
import java.util.Calendar

@Database(
    entities = [
        User::class,
        Client::class,
        Pet::class,
        Staff::class,
        Service::class,
        Appointment::class,
        TimeSlot::class,
        Medicine::class,
        MedicineInventory::class,
        MedicineAdministrationLog::class,
        VetTimeSlot::class
    ],
    version = 5,
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
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun medicineDao(): MedicineDao
    abstract fun medicineInventoryDao(): MedicineInventoryDao
    abstract fun medicineAdministrationLogDao(): MedicineAdministrationLogDao
    abstract fun vetTimeSlotDao(): VetTimeSlotDao

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
     * Populates the database with seed data on first creation OR after a destructive migration.
     */
    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch { seedData(database) }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch { seedData(database) }
            }
        }

        private suspend fun seedData(database: PetShopDatabase) {
            fun nextDayAt(hour24: Int, minute: Int): Long {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, hour24)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                return cal.timeInMillis
            }

            // Default admin account  (password: "admin123" – store a real hash in production)
            database.userDao().insert(
                User(
                    username = "admin",
                    passwordHash = "admin123",
                    email = "admin@petshop.com",
                    firstName = "Admin",
                    lastName = "User",
                    avatarImageUri = "default://avatar/admin",
                    role = UserRole.ADMIN
                )
            )

            // Staff users
            val vetUserId = database.userDao().insert(
                User(
                    username = "dr_lee",
                    passwordHash = "staff123",
                    email = "dr.lee@petshop.com",
                    firstName = "Mei",
                    lastName = "Lee",
                    phone = "0118888888",
                    avatarImageUri = "default://avatar/staff",
                    role = UserRole.STAFF
                )
            ).toInt()

            val receptionistUserId = database.userDao().insert(
                User(
                    username = "amy_frontdesk",
                    passwordHash = "staff123",
                    email = "amy@petshop.com",
                    firstName = "Amy",
                    lastName = "Wong",
                    phone = "0117777777",
                    avatarImageUri = "default://avatar/staff",
                    role = UserRole.STAFF
                )
            ).toInt()

            val vetStaffId = database.staffDao().insert(
                Staff(
                    userId = vetUserId,
                    firstName = "Mei",
                    lastName = "Lee",
                    phone = "0118888888",
                    email = "dr.lee@petshop.com",
                    role = StaffRole.VETERINARIAN,
                    specialization = "Small Animal Medicine",
                    licenseNumber = "VET-MY-1001"
                )
            ).toInt()

            database.staffDao().insert(
                Staff(
                    userId = receptionistUserId,
                    firstName = "Amy",
                    lastName = "Wong",
                    phone = "0117777777",
                    email = "amy@petshop.com",
                    role = StaffRole.RECEPTIONIST
                )
            )

            // Normal client account for end-user testing (username: user1, password: user123)
            val userClientId = database.userDao().insert(
                User(
                    username = "user1",
                    passwordHash = "user123",
                    email = "user1@petshop.com",
                    firstName = "Alex",
                    lastName = "Tan",
                    avatarImageUri = "default://avatar/client",
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

            val userClient2Id = database.userDao().insert(
                User(
                    username = "user2",
                    passwordHash = "user123",
                    email = "user2@petshop.com",
                    firstName = "Sara",
                    lastName = "Lim",
                    phone = "0191234567",
                    avatarImageUri = "default://avatar/client",
                    role = UserRole.CLIENT
                )
            ).toInt()

            val client2Id = database.clientDao().insert(
                Client(
                    userId = userClient2Id,
                    firstName = "Sara",
                    lastName = "Lim",
                    phone = "0191234567",
                    email = "user2@petshop.com",
                    address = "Petaling Jaya"
                )
            ).toInt()

            database.petDao().insert(
                Pet(
                    clientId = clientId,
                    name = "Milo",
                    species = "Cat",
                    breed = "Domestic Shorthair",
                    gender = PetGender.MALE,
                    weightKg = 4.3f,
                    profileImageUri = "default://pet/cat"
                )
            )

            val lunaPetId = database.petDao().insert(
                Pet(
                    clientId = client2Id,
                    name = "Luna",
                    species = "Dog",
                    breed = "Shiba Inu",
                    gender = PetGender.FEMALE,
                    weightKg = 8.2f,
                    profileImageUri = "default://pet/dog"
                )
            ).toInt()

            val cocoPetId = database.petDao().insert(
                Pet(
                    clientId = client2Id,
                    name = "Coco",
                    species = "Rabbit",
                    breed = "Mini Lop",
                    gender = PetGender.FEMALE,
                    weightKg = 1.9f,
                    profileImageUri = "default://pet/rabbit"
                )
            ).toInt()

            // Sample services
            val consultationServiceId = database.serviceDao().insert(
                Service(name = "General Consultation", category = "Consultation", price = 500.0, durationMinutes = 30)
            ).toInt()

            val vaccinationServiceId = database.serviceDao().insert(
                Service(name = "Vaccination", category = "Preventive", price = 350.0, durationMinutes = 15)
            ).toInt()

            val services = listOf(
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
            val amoxicillinId = database.medicineDao().insert(
                Medicine(name = "Amoxicillin", brand = "Generic", category = "Antibiotic", unit = "tablet", requiresPrescription = true)
            ).toInt()
            val rabiesVaccineId = database.medicineDao().insert(
                Medicine(name = "Rabies Vaccine", brand = "Nobivac", category = "Vaccine", unit = "vial", requiresPrescription = false)
            ).toInt()

            val medicines = listOf(
                Medicine(name = "Metronidazole",  brand = "Flagyl",    category = "Antibiotic",    unit = "tablet",  requiresPrescription = true),
                Medicine(name = "Ivermectin",     brand = "Heartgard", category = "Antiparasitic", unit = "tablet",  requiresPrescription = true),
                Medicine(name = "Distemper Combo",brand = "DHPPiL",    category = "Vaccine",       unit = "vial",    requiresPrescription = false),
                Medicine(name = "Frontline Plus", brand = "Frontline", category = "Antiparasitic", unit = "pipette", requiresPrescription = false),
                Medicine(name = "Meloxicam",      brand = "Metacam",   category = "NSAID",         unit = "ml",      requiresPrescription = true),
                Medicine(name = "Vitamin B Complex", brand = "Generic",category = "Supplement",    unit = "tablet",  requiresPrescription = false)
            )
            medicines.forEach { database.medicineDao().insert(it) }

            // Starter medicine stock
            val amoxInvId = database.medicineInventoryDao().insert(
                MedicineInventory(
                    medicineId = amoxicillinId,
                    quantity = 120,
                    unitPrice = 1.2,
                    sellingPrice = 2.5,
                    batchNumber = "AMX-2406-A",
                    reorderLevel = 25,
                    supplierName = "Vet Pharma"
                )
            ).toInt()
            val rabiesInvId = database.medicineInventoryDao().insert(
                MedicineInventory(
                    medicineId = rabiesVaccineId,
                    quantity = 8,
                    unitPrice = 20.0,
                    sellingPrice = 35.0,
                    batchNumber = "RBV-2406-B",
                    reorderLevel = 10,
                    supplierName = "AnimalCare Supplies"
                )
            ).toInt()

            // Sample medicine administration logs
            val threeDaysAgo = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
            val twoDaysAgo   = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
            val yesterday    = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L

            database.medicineAdministrationLogDao().insert(
                MedicineAdministrationLog(
                    inventoryId    = amoxInvId,
                    petName        = "Luna",
                    quantityUsed   = 2,
                    administeredAt = threeDaysAgo,
                    administeredBy = "Dr. Mei Lee",
                    staffId        = vetStaffId,
                    notes          = "Post-surgery antibiotic course, Day 1"
                )
            )
            database.medicineAdministrationLogDao().insert(
                MedicineAdministrationLog(
                    inventoryId    = amoxInvId,
                    petName        = "Milo",
                    quantityUsed   = 1,
                    administeredAt = twoDaysAgo,
                    administeredBy = "Dr. Mei Lee",
                    staffId        = vetStaffId,
                    notes          = "Respiratory infection treatment"
                )
            )
            database.medicineAdministrationLogDao().insert(
                MedicineAdministrationLog(
                    inventoryId    = rabiesInvId,
                    petName        = "Coco",
                    quantityUsed   = 1,
                    administeredAt = yesterday,
                    administeredBy = "Dr. Mei Lee",
                    staffId        = vetStaffId,
                    notes          = "Annual rabies booster"
                )
            )

            // Starter appointments + timeslots
            val tomorrow10am = nextDayAt(10, 0)
            val tomorrow1030am = nextDayAt(10, 30)

            val slotId = database.timeSlotDao().insert(
                TimeSlot(
                    staffId = vetStaffId,
                    slotStartTime = tomorrow10am,
                    slotEndTime = tomorrow1030am
                )
            ).toInt()

            database.appointmentDao().insert(
                Appointment(
                    clientId = client2Id,
                    petId = lunaPetId,
                    staffId = vetStaffId,
                    serviceId = consultationServiceId,
                    timeSlotId = slotId,
                    scheduledAt = tomorrow10am,
                    status = AppointmentStatus.CONFIRMED,
                    notes = "First-time consultation"
                )
            )
            database.timeSlotDao().updateBookedState(slotId, true)

            database.appointmentDao().insert(
                Appointment(
                    clientId = client2Id,
                    petId = cocoPetId,
                    staffId = vetStaffId,
                    serviceId = vaccinationServiceId,
                    scheduledAt = nextDayAt(14, 0),
                    status = AppointmentStatus.PENDING,
                    notes = "Annual vaccine"
                )
            )
        }
    }
}
