package mx.utng.ecoviedos.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sesion")

/**
 * Gestor de la sesión persistente del usuario utilizando Jetpack DataStore Preferences.
 *
 * Esta clase encapsula el acceso a los datos de autenticación y configuración local,
 * proporcionando flujos reactivos ([Flow]) para observar cambios en tiempo real.
 *
 * @param context Contexto necesario para acceder al DataStore.
 */
class SessionManager(private val context: Context) {
    companion object {
        /** Clave para almacenar el token de autenticación JWT. */
        val TOKEN_KEY = stringPreferencesKey("token")
        /** Clave para el identificador único del usuario en la base de datos. */
        val USER_ID_KEY = stringPreferencesKey("userId")
        /** Clave para el nombre completo del usuario. */
        val NOMBRE_KEY = stringPreferencesKey("nombre")
        /** Clave para el rol asignado (e.g., "admin", "enologo", "trabajador"). */
        val ROL_KEY = stringPreferencesKey("rol")
        /** Clave para la dirección IP del broker MQTT configurado manualmente. */
        val MQTT_IP_KEY = stringPreferencesKey("mqttIp")
    }

    /**
     * Almacena de forma persistente la información de sesión tras un inicio de sesión exitoso.
     *
     * @param token Token JWT recibido del servidor.
     * @param userId Identificador del usuario.
     * @param nombre Nombre del usuario para mostrar en la UI.
     * @param rol Rol del usuario para control de acceso.
     */
    suspend fun guardarSesion(token: String, userId: String, nombre: String, rol: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[NOMBRE_KEY] = nombre
            prefs[ROL_KEY] = rol
        }
    }

    /**
     * Guarda la dirección IP del servidor MQTT.
     *
     * @param ip Dirección IP o nombre de dominio del broker.
     */
    suspend fun guardarMqttIp(ip: String) {
        context.dataStore.edit { prefs ->
            prefs[MQTT_IP_KEY] = ip
        }
    }

    /** Flujo que emite el token de sesión actual. */
    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    /** Flujo que emite el ID del usuario actual. */
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    /** Flujo que emite el rol del usuario actual. */
    val rol: Flow<String?> = context.dataStore.data.map { it[ROL_KEY] }
    /** Flujo que emite la IP del broker MQTT configurada. */
    val mqttIp: Flow<String?> = context.dataStore.data.map { it[MQTT_IP_KEY] }

    /**
     * Elimina todos los datos de la sesión actual, efectivamente cerrando la sesión del usuario.
     */
    suspend fun cerrarSesion() {
        context.dataStore.edit { it.clear() }
    }
}
