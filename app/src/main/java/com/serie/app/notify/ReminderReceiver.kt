package com.serie.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.serie.app.SerieApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Recebe o disparo do alarme e tambem o reinicio do aparelho.
 *
 * Os alarmes do AlarmManager nao sobrevivem a um boot nem a uma atualizacao do
 * app, entao esses dois eventos chegam aqui so para reagendar. O disparo
 * normal notifica e agenda o proximo, porque o alarme pendente e sempre um so.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val fired = intent.action !in BOOT_ACTIONS
        val app = context.applicationContext as? SerieApplication ?: return
        // goAsync porque a preferencia vem do banco: devolver antes da leitura
        // deixaria o processo ser morto no meio e o lembrete sumiria daqui
        // para frente.
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (fired) ReminderScheduler.notifyNow(context)
                val preferences = app.repository.preferences.first() ?: return@launch
                ReminderScheduler.apply(context, preferences)
            } finally {
                result.finish()
            }
        }
    }

    private companion object {
        val BOOT_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
