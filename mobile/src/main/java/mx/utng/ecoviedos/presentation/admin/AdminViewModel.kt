package mx.utng.ecoviedos.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoviedos.data.api.RetrofitClient
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

class AdminViewModel : ViewModel() {

    fun addParcel(nombre: String, variedad: String, area: Int, humedad: Float, temp: Float) {
        viewModelScope.launch {
            try {
                val nueva = Parcela(
                    id = System.currentTimeMillis().toString(),
                    nombreParcela = nombre,
                    variedad = variedad,
                    areaM2 = area,
                    umbralHumedad = humedad,
                    umbralTemp = temp,
                    indiceMaduracion = 0f,
                    fechaCosecha = Date(),
                    activa = true
                )
                // RetrofitClient.apiService.createParcel(nueva) // Listo para activar
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun addUser(nombre: String, correo: String, rol: String) {
        viewModelScope.launch {
            try {
                // Lógica para enviar al servidor Node.js
                // val user = User(nombre, correo, rol)
                // RetrofitClient.apiService.createUser(user)
            } catch (e: Exception) { }
        }
    }
}
