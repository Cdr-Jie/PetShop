package com.example.petshop

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.Appointment
import com.example.petshop.data.entity.AppointmentStatus
import com.example.petshop.data.entity.Client
import com.example.petshop.data.entity.Pet
import com.example.petshop.data.entity.PetGender
import com.example.petshop.data.entity.Staff
import com.example.petshop.data.entity.StaffRole
import com.example.petshop.data.entity.TimeSlot
import com.example.petshop.data.entity.User
import com.example.petshop.data.entity.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppointmentBookingRulesTest {

    private lateinit var db: PetShopDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, PetShopDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun creatingDuplicateTimeSlotForSameStaffAndStartTimeFails() = runBlocking {
        val (_, _, staffId) = seedMinimalBookingGraph()
        val slotStart = 1_725_000_000_000L

        db.timeSlotDao().insert(
            TimeSlot(
                staffId = staffId,
                slotStartTime = slotStart,
                slotEndTime = slotStart + 30 * 60_000L
            )
        )

        var duplicateFailed = false
        try {
            db.timeSlotDao().insert(
                TimeSlot(
                    staffId = staffId,
                    slotStartTime = slotStart,
                    slotEndTime = slotStart + 30 * 60_000L
                )
            )
        } catch (_: SQLiteConstraintException) {
            duplicateFailed = true
        }

        assertTrue("Duplicate timeslot insert should fail", duplicateFailed)
    }

    @Test
    fun activeAppointmentCountPerSlotSupportsCancelThenRebookFlow() = runBlocking {
        val (clientId, petId, staffId) = seedMinimalBookingGraph()
        val slotStart = 1_725_000_000_000L

        val slotId = db.timeSlotDao().insert(
            TimeSlot(
                staffId = staffId,
                slotStartTime = slotStart,
                slotEndTime = slotStart + 30 * 60_000L
            )
        ).toInt()

        val firstAppointmentId = db.appointmentDao().insert(
            Appointment(
                clientId = clientId,
                petId = petId,
                staffId = staffId,
                timeSlotId = slotId,
                scheduledAt = slotStart,
                status = AppointmentStatus.PENDING
            )
        ).toInt()

        db.appointmentDao().insert(
            Appointment(
                clientId = clientId,
                petId = petId,
                staffId = staffId,
                timeSlotId = slotId,
                scheduledAt = slotStart,
                status = AppointmentStatus.CANCELLED
            )
        )

        assertEquals(1, db.appointmentDao().countActiveByTimeSlot(slotId))

        db.appointmentDao().updateStatus(firstAppointmentId, AppointmentStatus.CANCELLED)

        assertEquals(0, db.appointmentDao().countActiveByTimeSlot(slotId))

        db.appointmentDao().insert(
            Appointment(
                clientId = clientId,
                petId = petId,
                staffId = staffId,
                timeSlotId = slotId,
                scheduledAt = slotStart,
                status = AppointmentStatus.CONFIRMED
            )
        )

        assertEquals(1, db.appointmentDao().countActiveByTimeSlot(slotId))
    }

    private suspend fun seedMinimalBookingGraph(): Triple<Int, Int, Int> {
        val userId = db.userDao().insert(
            User(
                username = "booking-user",
                passwordHash = "pw",
                email = "booking-user@petshop.local",
                firstName = "Book",
                lastName = "User",
                role = UserRole.CLIENT
            )
        ).toInt()

        val clientId = db.clientDao().insert(
            Client(
                userId = userId,
                firstName = "Book",
                lastName = "User",
                phone = "0100000000",
                email = "booking-user@petshop.local"
            )
        ).toInt()

        val petId = db.petDao().insert(
            Pet(
                clientId = clientId,
                name = "Milo",
                species = "Cat",
                gender = PetGender.MALE
            )
        ).toInt()

        val staffId = db.staffDao().insert(
            Staff(
                firstName = "Vet",
                lastName = "One",
                phone = "0101111111",
                email = "vet1@petshop.local",
                role = StaffRole.VETERINARIAN
            )
        ).toInt()

        return Triple(clientId, petId, staffId)
    }
}

