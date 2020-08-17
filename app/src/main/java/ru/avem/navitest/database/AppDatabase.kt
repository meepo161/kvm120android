package ru.avem.navitest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.avem.navitest.database.dot.ProtocolDot
import ru.avem.navitest.database.dot.ProtocolDotDao
import ru.avem.navitest.database.graph.ProtocolGraph
import ru.avem.navitest.database.graph.ProtocolGraphDao
import ru.avem.navitest.utils.DATABASE_NAME

@Database(entities = [ProtocolGraph::class, ProtocolDot::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun protocolGraphDao(): ProtocolGraphDao
    abstract fun protocolDotDao(): ProtocolDotDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(ctx: Context) = instance ?: synchronized(this) {
            instance ?: buildDatabase(ctx).also { instance = it }
        }

        private fun buildDatabase(ctx: Context) =
            Room
                .databaseBuilder(
                    ctx,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
    }
}
