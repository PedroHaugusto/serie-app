package com.serie.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        LibraryExerciseEntity::class,
        PreferencesEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class SerieDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun libraryDao(): LibraryDao
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        private const val NAME = "serie.db"

        @Volatile
        private var instance: SerieDatabase? = null

        /**
         * Uma instancia por processo.
         *
         * O Room guarda um pool de conexoes por instancia; criar uma por tela
         * abriria pools concorrentes sobre o mesmo arquivo e os Flows de uma
         * nao veriam as escritas da outra.
         */
        fun get(context: Context): SerieDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                SerieDatabase::class.java,
                NAME,
            )
                // Sem fallbackToDestructiveMigration de proposito: apagar os
                // treinos do usuario a cada mudanca de esquema e exatamente o
                // problema que este banco existe para resolver. Quando a
                // versao subir, escrevemos a migracao a partir dos esquemas
                // exportados em app/schemas.
                .build()
    }
}
