package mx.utng.ecoviedos.presentation.widget

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.first
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.shared.data.mqtt.MqttManager

class ToggleRiegoAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val prefs = PreferencesGlanceStateDefinition.getDataStore(context, glanceId.toString()).data.first()
        val parcelId = prefs[stringPreferencesKey("parcel_id")] ?: return
        
        val currentStatus = parameters[ActionParameters.Key<Boolean>("riego_status")] ?: false
        val riegoTime = parameters[ActionParameters.Key<Int>("riego_time")] ?: 5
        
        val sessionManager = SessionManager(context)
        val token = sessionManager.token.first() ?: return

        // Enviar comando vía MQTT usando el tiempo configurado
        val mqttManager = MqttManager(
            context = context, 
            onMessageReceived = { _, _, _, _, _, _ -> }, 
            onRiegoStatusReceived = { _, _, _ -> }, 
            onParcelListReceived = {}, 
            onCavaListReceived = {},
            onConnectionStatusChanged = { _, _ -> }
        )
        mqttManager.connect()
        
        // Damos un pequeño margen para conectar y enviamos
        kotlinx.coroutines.delay(500)
        mqttManager.toggleRiego(parcelId, !currentStatus, duracionMinutos = riegoTime, modo = "MANUAL")
        
        // Forzar actualización del widget
        ParcelaGlanceWidget().update(context, glanceId)
        
        // Desconectar después de enviar
        kotlinx.coroutines.delay(1000)
        mqttManager.disconnect()
    }
}
