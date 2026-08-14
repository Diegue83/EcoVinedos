package mx.utng.ecoviedos.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import kotlinx.coroutines.flow.first
import mx.utng.ecoviedos.MainActivity
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.remote.RetrofitClient

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        val sessionManager = SessionManager(applicationContext)
        val token = sessionManager.token.first()

        if (token.isNullOrBlank()) {
            return ListenableWorker.Result.success()
        }

        return try {
            val response = RetrofitClient.notificacionService.obtenerMisNotificaciones("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val newNotifications = response.body()!!.filter { it.estado == "no leida" }
                
                if (newNotifications.isNotEmpty()) {
                    val mostRecent = newNotifications.first()
                    showNotification(
                        mostRecent.titulo, 
                        if (newNotifications.size > 1) "Tienes ${newNotifications.size} avisos nuevos" else mostRecent.mensaje
                    )
                }
            }
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error checking notifications", e)
            ListenableWorker.Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val context = applicationContext
        val channelId = "system_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Avisos del Viñedo", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "notifications")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(999, notification)
    }
}
