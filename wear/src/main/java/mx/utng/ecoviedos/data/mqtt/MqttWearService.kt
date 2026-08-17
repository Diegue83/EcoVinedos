package mx.utng.ecoviedos.data.mqtt

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import mx.utng.ecoviedos.data.ParcelaRepository
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.presentation.AlertaActivity
import mx.utng.ecoviedos.presentation.MainActivity

class MqttWearService : Service() {
    private var mqttManager: MqttManager? = null
    private val CHANNEL_ID = "mqtt_service_channel"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        initializeMqtt()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio de Monitoreo",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EcoViñedos")
            .setContentText("Monitoreando parcelas en tiempo real")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun initializeMqtt() {
        mqttManager = MqttManager(
            context = this,
            onSensorsUpdated = { id, hum, temp, humsuel, riego, tiempo ->
                updateParcelaLocalmente(id, hum, temp, humsuel, riego, tiempo)
            },
            onRiegoStatusReceived = { id, activo, tiempo ->
                updateRiegoLocalmente(id, activo, tiempo)
            },
            onStatusChanged = { status ->
                Log.d("MqttWearService", "MQTT Status: $status")
            }
        )
        mqttManager?.connect()
    }

    private fun updateParcelaLocalmente(id: String, hum: Float, temp: Float, humsuel: Float, riego: Boolean, tiempo: Int) {
        val currentParcelas = ParcelaRepository.parcelas.value.toMutableList()
        val index = currentParcelas.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldParcela = currentParcelas[index]
            val nuevaRiegoActivo = if (oldParcela.riegoActivo && !riego) true else riego
            
            val updatedParcela = oldParcela.copy(
                humedad = hum,
                temperatura = temp,
                humedadSuelo = humsuel,
                riegoActivo = nuevaRiegoActivo,
                tiempoRestanteRiego = if (nuevaRiegoActivo && !riego) oldParcela.tiempoRestanteRiego else tiempo
            )
            currentParcelas[index] = updatedParcela
            ParcelaRepository.updateParcelas(currentParcelas.toList(), this)
            
            if (updatedParcela.esHumedadCritica()) {
                showUrgentAlert(updatedParcela)
            }
        }
    }

    private fun updateRiegoLocalmente(id: String, activo: Boolean, tiempo: Int) {
        val currentParcelas = ParcelaRepository.parcelas.value.toMutableList()
        val index = currentParcelas.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedParcela = currentParcelas[index].copy(
                riegoActivo = activo,
                tiempoRestanteRiego = tiempo
            )
            currentParcelas[index] = updatedParcela
            ParcelaRepository.updateParcelas(currentParcelas.toList(), this)
        }
    }

    private fun showUrgentAlert(parcela: Parcela) {
        val alertChannelId = "critical_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(alertChannelId, "Alertas Críticas", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(this, AlertaActivity::class.java).apply {
            putExtra("parcela_id", parcela.id)
            putExtra("parcela", parcela.nombreParcela)
            putExtra("variedad", parcela.variedad)
            putExtra("humedad", "${parcela.humedadSuelo.toInt()}%")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, parcela.id.hashCode(), fullScreenIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, alertChannelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("¡Humedad Crítica!")
            .setContentText("Parcela ${parcela.nombreParcela} requiere riego.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        notificationManager.notify(parcela.id.hashCode(), notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mqttManager?.disconnect()
        super.onDestroy()
    }
}
