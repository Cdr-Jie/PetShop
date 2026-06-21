package com.example.petshop.data.database

import androidx.room.TypeConverter
import com.example.petshop.data.entity.AppointmentStatus
import com.example.petshop.data.entity.PetGender
import com.example.petshop.data.entity.StaffRole
import com.example.petshop.data.entity.UserRole

class Converters {

    // ----- UserRole -----
    @TypeConverter fun fromUserRole(role: UserRole): String = role.name
    @TypeConverter fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    // ----- StaffRole -----
    @TypeConverter fun fromStaffRole(role: StaffRole): String = role.name
    @TypeConverter fun toStaffRole(value: String): StaffRole = StaffRole.valueOf(value)

    // ----- PetGender -----
    @TypeConverter fun fromPetGender(gender: PetGender): String = gender.name
    @TypeConverter fun toPetGender(value: String): PetGender = PetGender.valueOf(value)

    // ----- AppointmentStatus -----
    @TypeConverter fun fromAppointmentStatus(status: AppointmentStatus): String = status.name
    @TypeConverter fun toAppointmentStatus(value: String): AppointmentStatus = AppointmentStatus.valueOf(value)
}

