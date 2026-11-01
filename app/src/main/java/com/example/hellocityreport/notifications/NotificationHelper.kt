package com.example.hellocityreport.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hellocityreport.MainActivity
import com.example.hellocityreport.R

/**
 * Notificaciones locales simples (la opción más sencilla).
 * Muestra una notificación cuando se crea una incidencia.
 */
object NotificationHelper {

    const val CHANNEL_ID = "incidencias_channel"
    private const val CHANNEL_NAME = "Incidencias"
    private const val CHANNEL_DESC = "Notificaciones de incidencias creadas"

    /** Crea el canal de notificaciones (requerido en Android 8+). */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = CHANNEL_DESC }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /** Muestra una notificación de "incidencia creada". */
    fun showIncidenciaCreada(context: Context, titulo: String) {
        createChannel(context)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Incidencia creada")
            .setContentText(titulo.ifBlank { "Tu reporte se guardó correctamente." })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}
