package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SubscriptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SubJoggerDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: SubJoggerDatabase? = null

        fun getInstance(context: Context): SubJoggerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SubJoggerDatabase::class.java,
                    "subjogger_local_ledger.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
