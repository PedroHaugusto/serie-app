package com.serie.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migracoes do banco.
 *
 * O DDL aqui e copiado do `createSql` dos esquemas exportados em
 * `app/schemas`, nao escrito de memoria: o Room compara a estrutura real com a
 * esperada ao abrir o banco e recusa qualquer diferenca, inclusive de
 * nulidade ou ordem de coluna. Escrever "parecido" falha em tempo de execucao.
 */

/**
 * 1 -> 2: exercicios criados pelo usuario e persistencia do treino em
 * andamento.
 *
 * A coluna `isCustom` entra com DEFAULT 0 porque as linhas ja existentes vem
 * todas do catalogo inicial. O DEFAULT so vale para esta migracao - o Room
 * declara a coluna sem default, e e por isso que ele nao aparece no createSql
 * acima.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `library_exercises` ADD COLUMN `isCustom` INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `active_session` (" +
                "`id` INTEGER NOT NULL, " +
                "`workoutId` TEXT NOT NULL, " +
                "`workoutLetter` TEXT NOT NULL, " +
                "`workoutName` TEXT NOT NULL, " +
                "`exerciseIndex` INTEGER NOT NULL, " +
                "`exerciseCount` INTEGER NOT NULL, " +
                "`exerciseName` TEXT NOT NULL, " +
                "`goal` TEXT NOT NULL, " +
                "`startedAtMillis` INTEGER NOT NULL, " +
                "`restEndsAtMillis` INTEGER, " +
                "`restRemainingSeconds` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `active_sets` (" +
                "`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`position` INTEGER NOT NULL, " +
                "`previous` TEXT NOT NULL, " +
                "`load` TEXT NOT NULL, " +
                "`reps` TEXT NOT NULL, " +
                "`done` INTEGER NOT NULL)"
        )
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
