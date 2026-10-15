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

/**
 * 2 -> 3: perfil editavel.
 *
 * A linha de perfil nao e criada aqui. Quem ja usava o app fica sem ela ate a
 * primeira carga, e o repositorio trata a ausencia caindo no perfil padrao -
 * inserir dados dentro da migracao exigiria repetir aqui o conteudo do seed.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `profile` (" +
                "`id` INTEGER NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`since` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
    }
}

/**
 * 3 -> 4: historico de treinos concluidos.
 *
 * Os indices nao sao opcionais para o Room: ele compara tambem os indices ao
 * abrir o banco, e uma tabela criada sem eles reprova na validacao mesmo com
 * as colunas certas.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sessions` (" +
                "`id` TEXT NOT NULL, " +
                "`workoutId` TEXT NOT NULL, " +
                "`workoutLetter` TEXT NOT NULL, " +
                "`workoutName` TEXT NOT NULL, " +
                "`startedAtMillis` INTEGER NOT NULL, " +
                "`finishedAtMillis` INTEGER NOT NULL, " +
                "`effort` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_sessions_startedAtMillis` " +
                "ON `sessions` (`startedAtMillis`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `session_sets` (" +
                "`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`sessionId` TEXT NOT NULL, " +
                "`exerciseId` TEXT NOT NULL, " +
                "`exerciseName` TEXT NOT NULL, " +
                "`muscleGroup` TEXT NOT NULL, " +
                "`loadKg` REAL NOT NULL, " +
                "`reps` INTEGER NOT NULL, " +
                "FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_session_sets_sessionId` " +
                "ON `session_sets` (`sessionId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_session_sets_exerciseId` " +
                "ON `session_sets` (`exerciseId`)"
        )
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
