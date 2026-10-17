package com.serie.app.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.serie.app.MainActivity
import com.serie.app.R
import com.serie.app.domain.Preferences
import com.serie.app.domain.Reminder
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Agenda e dispara o lembrete de treino.
 *
 * Um alarme so fica pendente por vez, com o instante do proximo disparo: o
 * `setRepeating` semanal nao serve porque os dias marcados nao sao
 * equidistantes. Quem recebe o disparo reagenda o seguinte.
 *
 * O alarme e inexato de proposito. Exato exige permissao que o Android reserva
 * a despertadores e agendas, e um lembrete de treino que chega as 18:04 em vez
 * das 18:00 cumpre o mesmo papel.
 */
object ReminderScheduler {

    const val CHANNEL_ID = "lembretes-treino"
    private const val NOTIFICATION_ID = 1
    private const val REQUEST_CODE = 1
    private const val ACTION_FIRE = "com.serie.app.LEMBRETE"

    /** Liga, religa ou cancela o alarme conforme a preferencia atual. */
    fun apply(context: Context, preferences: Preferences, now: LocalDateTime = LocalDateTime.now()) {
        val next = Reminder.nextTrigger(preferences, now)
        val alarms = context.getSystemService(AlarmManager::class.java)
        val pending = firePendingIntent(context)
        if (next == null) {
            alarms?.cancel(pending)
            return
        }
        val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarms?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
    }

    /** Mostra a notificacao. Sem permissao, nao faz nada - e nao estoura. */
    fun notifyNow(context: Context) {
        if (!canNotify(context)) return
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hora do treino")
            .setContentText("Abra a série e registre as séries de hoje.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /**
     * O canal e criado na subida do processo, e nao ao ligar o lembrete: sem
     * ele a notificacao e descartada em silencio no Android 8 ou mais novo.
     */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lembretes de treino",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Avisa nos dias e no horário escolhidos no perfil."
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    /** O usuario concedeu a permissao de notificar? Antes do 13, sempre. */
    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun firePendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_FIRE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
