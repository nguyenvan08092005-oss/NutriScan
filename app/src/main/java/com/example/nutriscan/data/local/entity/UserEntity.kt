package com.example.nutriscan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey
    val id: Int = 1, // Fix cứng id = 1 vì ứng dụng này dành cho 1 cá nhân sử dụng
    val name: String,
    val weight: Double,
    val height: Double,
    val age: Int,
    val isDarkMode: Boolean = false
)