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
            val names = criticalParcels.joinToString { it.nombreParcela }
            showNotification(
                "¡Humedad Crítica!", 
                "Humedad baja en suelo: $names. Toca para ver detalles."
            )
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "critical_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertas Críticas", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1, notification)
    }
}

data class ParcelaMap(
    val id: String,
    val nombreParcela: String?,
    val variedad: String?,
    val areaM2: Int,
    val umbralHumedad: Float,
    val umbralTemp: Float,
    val umbralHumedadSuelo: Float? = null,
    val indiceMaduracion: Float,
    val fechaCosecha: Date?,
    val activa: Boolean,
    val humedad: Float,
    val temperatura: Float,
    val humedadSuelo: Float? = null,
    val riegoActivo: Boolean? = null,
    val tiempoRestanteRiego: Int? = null,
    val tipoRiego: String? = null
)
