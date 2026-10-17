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

/**
 * 4 -> 5: descricao nos exercicios e o exercicio da sessao por id.
 *
 * Duas colunas de texto com DEFAULT vazio. O default so existe para as linhas
 * que ja estao no banco: o Room declara as colunas sem default, e por isso ele
 * nao aparece no esquema exportado.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `library_exercises` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''"
        )
        db.execSQL(
            "ALTER TABLE `active_session` ADD COLUMN `exerciseId` TEXT NOT NULL DEFAULT ''"
        )
    }
}

/**
 * 5 -> 6: a sessao passa a guardar as series de todos os exercicios.
 *
 * As linhas que ja existem sao do unico exercicio que a versao anterior
 * registrava, entao o default 0 as coloca corretamente no primeiro.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `active_sets` ADD COLUMN `exerciseIndex` INTEGER NOT NULL DEFAULT 0"
        )
        db.execSQL(
            "ALTER TABLE `active_sets` ADD COLUMN `exerciseId` TEXT NOT NULL DEFAULT ''"
        )
    }
}

/** 6 -> 7: historico de peso corporal. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `body_weights` (" +
                "`id` TEXT NOT NULL, " +
                "`atMillis` INTEGER NOT NULL, " +
                "`kg` REAL NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_body_weights_atMillis` " +
                "ON `body_weights` (`atMillis`)"
        )
    }
}

/**
 * Medidas do corpo.
 *
 * As quatro medidas padrao sao inseridas aqui, e nao so no seed da primeira
 * execucao: quem ja usava o app nunca passa pelo seed, e sem elas a tabela de
 * medidas abriria vazia para justamente quem tem mais historico.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `measure_types` (" +
                "`id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`lowerIsBetter` INTEGER NOT NULL, " +
                "`position` INTEGER NOT NULL, " +
                "`isCustom` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `body_measures` (" +
                "`id` TEXT NOT NULL, " +
                "`typeId` TEXT NOT NULL, " +
                "`atMillis` INTEGER NOT NULL, " +
                "`cm` REAL NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_body_measures_typeId` " +
                "ON `body_measures` (`typeId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_body_measures_atMillis` " +
                "ON `body_measures` (`atMillis`)"
        )

        listOf(
            Triple("mt-braco", "Braço", 0),
            Triple("mt-peito", "Peito", 0),
            Triple("mt-cintura", "Cintura", 1),
            Triple("mt-coxa", "Coxa", 0),
        ).forEachIndexed { position, (id, name, lowerIsBetter) ->
            db.execSQL(
                "INSERT OR IGNORE INTO `measure_types` " +
                    "(`id`, `name`, `lowerIsBetter`, `position`, `isCustom`) " +
                    "VALUES (?, ?, ?, ?, 0)",
                arrayOf<Any>(id, name, lowerIsBetter, position),
            )
        }
    }
}

/**
 * Preferencias: saem meta semanal, unidade e o rotulo fixo do lembrete;
 * entram os dias e a hora do lembrete.
 *
 * A tabela e recriada em vez de alterada coluna por coluna porque tres saem e
 * tres entram - um ALTER por vez deixaria o esquema divergindo do exportado no
 * meio do caminho.
 *
 * [remindersOn] volta para desligado de proposito. O interruptor antigo
 * gravava a escolha e nao agendava nada; manter "ligado" faria o app afirmar
 * que ha um lembrete marcado para um horario que nunca existiu.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // A antiga sai do caminho primeiro para que a nova nasca com o nome
        // definitivo. Criar com nome provisorio e renomear depois faria o
        // SQLite guardar o DDL reescrito por ele, e nao o texto exportado.
        db.execSQL("ALTER TABLE `preferences` RENAME TO `preferences_antiga`")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `preferences` (" +
                "`id` INTEGER NOT NULL, " +
                "`darkTheme` INTEGER NOT NULL, " +
                "`remindersOn` INTEGER NOT NULL, " +
                "`reminderDays` INTEGER NOT NULL, " +
                "`reminderHour` INTEGER NOT NULL, " +
                "`reminderMinute` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "INSERT INTO `preferences` " +
                "(`id`, `darkTheme`, `remindersOn`, `reminderDays`, " +
                "`reminderHour`, `reminderMinute`) " +
                "SELECT `id`, `darkTheme`, 0, 0, 18, 0 FROM `preferences_antiga`"
        )
        db.execSQL("DROP TABLE `preferences_antiga`")
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
    MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
)
