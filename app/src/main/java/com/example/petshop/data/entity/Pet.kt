package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PetGender {
    MALE, FEMALE, UNKNOWN
}

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["clientId"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clientId")]
)
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val petId: Int = 0,
    val clientId: Int,
    val name: String,
    val species: String,           // e.g. Dog, Cat, Bird
    val breed: String = "",
    val gender: PetGender = PetGender.UNKNOWN,
    val birthDate: Long? = null,   // epoch millis
    val weightKg: Float? = null,
    val color: String = "",
    val microchipNumber: String = "",
    val medicalHistory: String = "",
    val profileImageUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

