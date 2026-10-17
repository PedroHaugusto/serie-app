package com.serie.app

import android.app.Application
import com.serie.app.data.SerieRepository
import com.serie.app.data.local.SerieDatabase
import com.serie.app.notify.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Dono do banco e do repositorio enquanto o processo vive.
 *
 * E injecao de dependencia manual: com um repositorio so, um container de DI
 * custaria mais em configuracao do que resolve. Se a lista crescer, este e o
 * ponto onde um Hilt entraria.
 */
class SerieApplication : Application() {

    val repository: SerieRepository by lazy {
        SerieRepository(SerieDatabase.get(this))
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.createChannel(this)
        // O alarme e reagendado a partir da preferencia, e nao de dentro do
        // ViewModel: assim qualquer mudanca - um toque na tela, a migracao do
        // banco, o primeiro seed - chega ao AlarmManager por um caminho so, e
        // o ViewModel continua sem depender de Context.
        scope.launch {
            repository.preferences
                .filterNotNull()
                .distinctUntilChanged()
                .collect { ReminderScheduler.apply(this@SerieApplication, it) }
        }
    }
}
