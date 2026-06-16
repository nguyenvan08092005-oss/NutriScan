package com.example.nutriscan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nutriscan.data.local.dao.FoodDiaryDao
import com.example.nutriscan.data.local.dao.UserDao
import com.example.nutriscan.data.local.entity.FoodDiaryEntity
import com.example.nutriscan.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        FoodDiaryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun foodDiaryDao(): FoodDiaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutriscan_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}