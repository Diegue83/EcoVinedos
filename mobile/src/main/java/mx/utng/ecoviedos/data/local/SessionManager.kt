package mx.utng.ecoviedos.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sesion")

class SessionManager(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
        val USER_ID_KEY = stringPreferencesKey("userId")
        val NOMBRE_KEY = stringPreferencesKey("nombre")
        val ROL_KEY = stringPreferencesKey("rol")
        val MQTT_IP_KEY = stringPreferencesKey("mqttIp")
    }

    suspend fun guardarSesion(token: String, userId: String, nombre: String, rol: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[NOMBRE_KEY] = nombre
            prefs[ROL_KEY] = rol
        }
    }

    suspend fun guardarMqttIp(ip: String) {
        context.dataStore.edit { prefs ->
            prefs[MQTT_IP_KEY] = ip
        }
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    val rol: Flow<String?> = context.dataStore.data.map { it[ROL_KEY] }
    val mqttIp: Flow<String?> = context.dataStore.data.map { it[MQTT_IP_KEY] }

    suspend fun cerrarSesion() {
        context.dataStore.edit { it.clear() }
    }
}