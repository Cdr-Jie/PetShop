package com.example.petshop.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN, STAFF, CLIENT
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true), Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val username: String,
    val passwordHash: String,
    val email: String,
    val role: UserRole = UserRole.CLIENT,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

