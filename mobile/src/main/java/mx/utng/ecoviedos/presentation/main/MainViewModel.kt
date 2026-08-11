package mx.utng.ecoviedos.presentation.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.WearableDataSender
import mx.utng.ecoviedos.data.RiegoAlarmReceiver
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.mqtt.MqttConfig
import mx.utng.ecoviedos.data.mqtt.MqttManager
import mx.utng.ecoviedos.data.repository.ParcelaRepository
import mx.utng.ecoviedos.domain.model.Parcela
import mx.utng.ecoviedos.data.remote.ParcelaRequest
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first

/**
 * ViewModel principal de la aplicación.
 * 
 * Gestiona el estado global de las parcelas, la comunicación con Wearable,
 * la sincronización vía MQTT y las peticiones HTTP al servidor.
 */
class MainViewModel(application: Application) : AndroidViewModel(application), MessageClient.OnMessageReceivedListener {
    private val _parcelas = MutableStateFlow<List<Parcela>>(emptyList())
    val parcelas: StateFlow<List<Parcela>> = _parcelas.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _mqttStatus = MutableStateFlow("Desconectado")
    val mqttStatus: StateFlow<String> = _mqttStatus.asStateFlow()

    private val _isMqttConnected = MutableStateFlow(false)
    val isMqttConnected: StateFlow<Boolean> = _isMqttConnected.asStateFlow()
    
    private var authToken: String? = null
    
    private val wearableDataSender = WearableDataSender(application)
    private val parcelaRepository = ParcelaRepository()
    private val sessionManager = SessionManager(application)
    private var mqttManager: MqttManager? = null

    val sessionToken: Flow<String?> = sessionManager.token
    val sessionRol: Flow<String?> = sessionManager.rol

    private val prefs = application.getSharedPreferences("EcoViñedosPrefs", Context.MODE_PRIVATE)

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        Wearable.getMessageClient(application).addListener(this)
        initializeMqtt()
        startLocalTimer()
        
        viewModelScope.launch {
            sessionToken.collect { token ->
                authToken = token
                if (!token.isNullOrBlank()) {
                    cargarParcelas()
                } else {
                    _parcelas.value = emptyList()
                }
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual y libera recursos.
     */
    fun logout() {
        viewModelScope.launch {
            mqttManager?.disconnect()
            sessionManager.cerrarSesion()
        }
    }

    /**
     * Realiza una petición HTTP GET al servidor para obtener la lista actualizada de parcelas.
     * 
     * Sincroniza los datos con la interfaz y con el dispositivo Wearable.
     */
    fun cargarParcelas() {
        Log.d("MainViewModel", "Iniciando carga de parcelas via HTTP...")
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val token = authToken ?: sessionManager.token.first()
                if (!token.isNullOrBlank()) {
                    val result = parcelaRepository.obtenerParcelas(token)
                    result.onSuccess { list ->
                        Log.d("MainViewModel", "HTTP GET exitoso: ${list.size} parcelas")
                        
                        // Parchear con tiempos locales de persistencia
                        val patchedList = list.map { parcela ->
                            val savedEnd = prefs.getLong("riego_end_${parcela.id}", 0L)
                            if (parcela.riegoActivo && savedEnd > 0) {
                                val diff = (savedEnd - System.currentTimeMillis()) / 1000
                                // Si la diferencia es positiva, usamos el tiempo local. 
                                // Si es negativa y es manual, también (para mostrar excedido).
                                if (diff > 0 || parcela.tipoRiego == "MANUAL") {
                                    parcela.copy(tiempoRestanteRiego = diff.toInt())
                                } else {
                                    parcela
                                }
                            } else {
                                parcela
                            }
                        }
                        
                        _parcelas.value = patchedList
                        wearableDataSender.sendParcelas(patchedList)
                    }
                    result.onFailure {
                        Log.e("MainViewModel", "HTTP GET error: ${it.message}")
                    }
                } else {
                    Log.w("MainViewModel", "Token no disponible para la carga")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Fallo crítico en cargarParcelas", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Actualiza la fecha de cosecha de una parcela.
     */
    fun actualizarFechaCosecha(parcela: Parcela, nuevaFecha: Date?) {
        viewModelScope.launch {
            val token = authToken ?: return@launch
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            
            val request = ParcelaRequest(
                nombreParcela = parcela.nombreParcela,
                variedad = parcela.variedad,
                areaM2 = parcela.areaM2.toDouble(),
                umbralHumedad = parcela.umbralHumedad.toDouble(),
                umbralTemp = parcela.umbralTemp.toDouble(),
                umbralHumedadSuelo = parcela.umbralHumedadSuelo.toDouble(),
                humedadOptimaSuelo = parcela.humedadOptimaSuelo.toDouble(),
                activa = parcela.activa,
                brix = parcela.brix?.toInt(),
                acidez = parcela.acidez,
                phSuelo = parcela.phSuelo,
                fechaCosecha = nuevaFecha?.let { isoFormat.format(it) }
            )

            parcelaRepository.actualizarParcela(token, parcela.id, request)
                .onSuccess {
                    cargarParcelas()
                }
                .onFailure {
                    Log.e("MainViewModel", "Error al actualizar fecha de cosecha", it)
                }
        }
    }

    /**
     * Inicializa el cliente MQTT y configura los callbacks para recibir telemetría.
     */
    private fun initializeMqtt() {
        mqttManager?.disconnect()
        
        mqttManager = MqttManager(
            context = getApplication(),
            onMessageReceived = { id, hum, temp,humsuel , riego, tiempo ->
                viewModelScope.launch(Dispatchers.Main) {
                    updateParcelaFromSensor(id, hum, temp,humsuel, riego, tiempo)
                }
            },
            onRiegoStatusReceived = { id, activo, tiempo ->
                viewModelScope.launch(Dispatchers.Main) {
                    updateRiegoStatus(id, activo, tiempo)
                }
            },
            onParcelListReceived = { _ ->
                viewModelScope.launch(Dispatchers.Main) {
                    cargarParcelas()
                }
            },
            onConnectionStatusChanged = { connected, message ->
                viewModelScope.launch(Dispatchers.Main) {
                    _isMqttConnected.value = connected
                    _mqttStatus.value = message ?: if (connected) "Conectado" else "Desconectado"
                }
            }
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            mqttManager?.connect()
        }
    }

    /**
     * Actualiza localmente los valores de una parcela recibidos por sensores vía MQTT.
     */
    private fun updateParcelaFromSensor(id: String, hum: Float, temp: Float,humsuel: Float, riego: Boolean, tiempo: Int) {
        val currentList = _parcelas.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldParcela = currentList[index]
            
            // Lógica de persistencia de tiempo
            var realTiempo = tiempo
            val savedEnd = prefs.getLong("riego_end_$id", 0L)
            if (riego && savedEnd > 0) {
                val diff = (savedEnd - System.currentTimeMillis()) / 1000
                realTiempo = diff.toInt()
            }

            // Lógica de notificaciones de riego
            if (oldParcela.riegoActivo && !riego) {
                showRiegoNotification(id, oldParcela.nombreParcela, "El riego automático ha finalizado correctamente.")
                prefs.edit().remove("riego_end_$id").apply()
            } else if (riego && realTiempo <= 0 && oldParcela.tiempoRestanteRiego > 0) {
                showRiegoNotification(id, oldParcela.nombreParcela, "¡Atención! El tiempo programado terminó. Detén el riego manual en la app.")
            }

            currentList[index] = oldParcela.copy(
                humedad = hum,
                temperatura = temp,
                humedadSuelo = humsuel,
                riegoActivo = riego,
                tiempoRestanteRiego = realTiempo,
                lastUpdated = System.currentTimeMillis()
            )
            _parcelas.value = currentList.toList()
            wearableDataSender.sendParcelas(currentList.toList())
        }
    }

    private fun showRiegoNotification(parcelaId: String, parcelaName: String, message: String) {
        val context = getApplication<Application>()
        val channelId = "riego_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "Notificaciones de Riego", android.app.NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Crear Intent para ir a la pantalla de riego
        val intent = android.content.Intent(context, mx.utng.ecoviedos.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "riego")
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Riego: $parcelaName")
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(parcelaId.hashCode(), notification)
    }

    private fun updateRiegoStatus(id: String, activo: Boolean, tiempo: Int) {
        val currentList = _parcelas.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            var realTiempo = tiempo
            if (activo) {
                val savedEnd = prefs.getLong("riego_end_$id", 0L)
                if (savedEnd > 0) {
                    realTiempo = ((savedEnd - System.currentTimeMillis()) / 1000).toInt()
                } else {
                    val newEnd = System.currentTimeMillis() + (tiempo * 1000L)
                    prefs.edit().putLong("riego_end_$id", newEnd).apply()
                }
            } else {
                prefs.edit().remove("riego_end_$id").apply()
            }

            currentList[index] = currentList[index].copy(
                riegoActivo = activo,
                tiempoRestanteRiego = realTiempo,
                lastUpdated = System.currentTimeMillis()
            )
            _parcelas.value = currentList.toList()
            wearableDataSender.sendParcelas(currentList.toList())
        }
    }

    private fun startLocalTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                val currentParcelas = _parcelas.value
                if (currentParcelas.any { it.riegoActivo }) {
                    val updatedList = currentParcelas.map { parcela ->
                        if (parcela.riegoActivo) {
                            // Usar el timestamp de fin guardado para mayor precisión y persistencia
                            val savedEnd = prefs.getLong("riego_end_${parcela.id}", 0L)
                            val nextTime = if (savedEnd > 0) {
                                ((savedEnd - System.currentTimeMillis()) / 1000).toInt()
                            } else {
                                parcela.tiempoRestanteRiego - 1
                            }

                            if (nextTime <= 0 && parcela.tiempoRestanteRiego > 0) {
                                if (parcela.tipoRiego == "AUTO") {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        showRiegoNotification(parcela.id, parcela.nombreParcela, "Riego automático finalizado.")
                                    }
                                    parcela.copy(tiempoRestanteRiego = 0, riegoActivo = false)
                                } else {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        showRiegoNotification(parcela.id, parcela.nombreParcela, "¡Tiempo agotado! Detén el riego manual.")
                                    }
                                    parcela.copy(tiempoRestanteRiego = -1)
                                }
                            } else if (nextTime < 0 && parcela.tipoRiego == "MANUAL") {
                                parcela.copy(tiempoRestanteRiego = nextTime)
                            } else if (nextTime > 0 || nextTime == 0) {
                                parcela.copy(tiempoRestanteRiego = nextTime)
                            } else {
                                parcela
                            }
                        } else {
                            parcela
                        }
                    }
                    _parcelas.value = updatedList
                }
            }
        }
    }

    /**
     * Envía una orden de activación o desactivación de riego vía MQTT.
     * 
     * @param parcelId Identificador de la parcela.
     * @param activo true para encender, false para apagar.
     * @param duracionMinutos Tiempo de riego en minutos.
     * @param modo "AUTO" o "MANUAL".
     */
    fun toggleRiego(parcelId: String, activo: Boolean, duracionMinutos: Int, modo: String = "AUTO") {
        Log.d("MainViewModel", "Toggle Riego: Parcela=$parcelId, Activo=$activo, Duracion=$duracionMinutos, Modo=$modo")
        
        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        if (activo) {
            val durationMillis = duracionMinutos * 60 * 1000L
            val endTime = System.currentTimeMillis() + durationMillis
            prefs.edit().putLong("riego_end_$parcelId", endTime).apply()
            
            // Programar alarma para notificación en segundo plano
            val intent = android.content.Intent(context, RiegoAlarmReceiver::class.java).apply {
                putExtra("parcela_id", parcelId)
                putExtra("parcela_nombre", _parcelas.value.find { it.id == parcelId }?.nombreParcela ?: "Parcela")
                putExtra("modo", modo)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context, parcelId.hashCode(), intent, 
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, endTime, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, endTime, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, endTime, pendingIntent)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error scheduling exact alarm", e)
                alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, endTime, pendingIntent)
            }
        } else {
            prefs.edit().remove("riego_end_$parcelId").apply()
            
            val intent = android.content.Intent(context, RiegoAlarmReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context, parcelId.hashCode(), intent, 
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            cancelRiegoNotification(parcelId)
        }
        
        mqttManager?.toggleRiego(parcelId, activo, duracionMinutos, modo)
    }

    private fun cancelRiegoNotification(parcelId: String) {
        val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(parcelId.hashCode())
    }

    /**
     * Reenvía la lista actual de parcelas al dispositivo Wearable conectado.
     */
    fun reloadParcelas() {
        wearableDataSender.sendParcelas(_parcelas.value)
    }

    override fun onMessageReceived(event: MessageEvent) {
        // Manejar mensajes entrantes desde el reloj
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager?.disconnect()
        Wearable.getMessageClient(getApplication()).removeListener(this)
    }
}
