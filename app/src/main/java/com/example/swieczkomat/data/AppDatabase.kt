package com.example.swieczkomat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Material::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "swieczkomat_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE materials_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        price REAL NOT NULL,
                        category TEXT NOT NULL,
                        materialType TEXT NOT NULL
                    )
                """)
                db.execSQL("""
                    INSERT INTO materials_new (id, name, price, category, materialType)
                    SELECT id, name, price, category, 'Other,' || quantity || ',' || unit FROM materials
                """)
                db.execSQL("DROP TABLE materials")
                db.execSQL("ALTER TABLE materials_new RENAME TO materials")
            }
        }
    }
}
