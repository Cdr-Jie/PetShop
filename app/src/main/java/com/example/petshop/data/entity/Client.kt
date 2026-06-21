package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clients",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId")]
)
data class Client(
    @PrimaryKey(autoGenerate = true)
    val clientId: Int = 0,
    val userId: Int? = null,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

