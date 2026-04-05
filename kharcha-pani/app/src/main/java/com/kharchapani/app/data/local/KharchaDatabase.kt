package com.kharchapani.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KharchaDatabase : RoomDatabase() {

    abstract fun kharchaDao(): KharchaDao

    companion object {
        @Volatile
        private var instance: KharchaDatabase? = null

        fun getInstance(context: Context): KharchaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KharchaDatabase::class.java,
                    "kharcha_pani.db"
                ).build().also { instance = it }
            }
    }
}
