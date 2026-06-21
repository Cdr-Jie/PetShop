package com.example.petshop.data.dao

import androidx.room.*
import com.example.petshop.data.entity.User
import com.example.petshop.data.entity.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getById(id: Int): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(username: String, passwordHash: String): User?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun getUsersByRole(role: UserRole): Flow<List<User>>

    @Query("UPDATE users SET isActive = :isActive WHERE userId = :userId")
    suspend fun setActiveStatus(userId: Int, isActive: Boolean)

    @Query("UPDATE users SET passwordHash = :newHash WHERE userId = :userId")
    suspend fun updatePassword(userId: Int, newHash: String)
}

