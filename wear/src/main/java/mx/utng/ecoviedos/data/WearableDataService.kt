package mx.utng.ecoviedos.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.presentation.AlertaActivity
import mx.utng.ecoviedos.presentation.MainActivity
import java.util.Date

class WearableDataService : WearableListenerService() {
    private val gson = Gson()

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("WearableDataService", "¡MENSAJE RECIBIDO DESDE EL MÓVIL! Path: ${messageEvent.path}")
        
        if (messageEvent.path == "/parcelas_message") {
            val json = String(messageEvent.data, Charsets.UTF_8)
            
            try {
                val itemType = object : TypeToken<List<ParcelaMap>>() {}.type
                val parcelasMobile: List<ParcelaMap> = gson.fromJson(json, itemType)
                
                val parcelasWear = parcelasMobile.map { m ->
                    Parcela(
                        id = m.id,
                        nombreParcela = m.nombreParcela ?: "Parcela ${m.id}",
                        variedad = m.variedad ?: "",
                        areaM2 = m.areaM2,
                        umbralHumedad = m.umbralHumedad,
                        umbralTemp = m.umbralTemp,
                        umbralHumedadSuelo = m.umbralHumedadSuelo ?: 40f,
                        indiceMaduracion = m.indiceMaduracion,
                        fechaCosecha = m.fechaCosecha ?: Date(),
                        activa = m.activa,
                        humedad = m.humedad,
                        temperatura = m.temperatura,
                        humedadSuelo = m.humedadSuelo ?: 0f,
                        riegoActivo = m.riegoActivo ?: false,
                        tiempoRestanteRiego = (m.tiempoRestanteRiego ?: 0) * 60, // Sincronizar en segundos
                        tipoRiego = m.tipoRiego ?: "MANUAL"
                    )
                }

                Handler(Looper.getMainLooper()).post {
                    ParcelaRepository.updateParcelas(parcelasWear)
                    Log.d("WearableDataService", "Sincronización por mensaje completada en Main Thread: ${parcelasWear.size} parcelas.")
                    checkCriticalSoilMoisture(parcelasWear)
                }
            } catch (e: Exception) {
                Log.e("WearableDataService", "Error al procesar mensaje JSON", e)
            }
        }
    }

    private fun checkCriticalSoilMoisture(parcelas: List<Parcela>) {
        val criticalParcels = parcelas.filter { it.esHumedadCritica() }
        if (criticalParcels.isNotEmpty()) {
            val mostCritical = criticalParcels.minBy { it.humedadSuelo }
            showUrgentAlert(mostCritical)
        }
    }

    private fun showUrgentAlert(parcela: Parcela) {
        val channelId = "critical_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertas Críticas", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir AlertaActivity directamente (Full Screen Intent)
        val fullScreenIntent = Intent(this, AlertaActivity::class.java).apply {
            putExtra("parcela_id", parcela.id)
            putExtra("parcela", parcela.nombreParcela)
            putExtra("variedad", parcela.variedad)
            putExtra("humedad", "${parcela.humedadSuelo.toInt()}%")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent normal para cuando tocan la notificación manualmente
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(this, 1, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("¡Humedad Crítica!")
            .setContentText("Parcela ${parcela.nombreParcela} requiere riego.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true) // Esto hace que salte la actividad
            .setContentIntent(mainPendingIntent)
            .build()

        notificationManager.notify(parcela.id.hashCode(), notification)
    }
}
