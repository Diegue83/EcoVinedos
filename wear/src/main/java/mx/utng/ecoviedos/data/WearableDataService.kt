package mx.utng.ecoviedos.data

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.utng.ecoviedos.domain.model.Parcela
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
                        indiceMaduracion = m.indiceMaduracion,
                        fechaCosecha = m.fechaCosecha ?: Date(),
                        activa = m.activa,
                        humedad = m.humedad,
                        temperatura = m.temperatura
                    )
                }
                ParcelaRepository.updateParcelas(parcelasWear)
                Log.d("WearableDataService", "Sincronización por mensaje completada: ${parcelasWear.size} parcelas.")
            } catch (e: Exception) {
                Log.e("WearableDataService", "Error al procesar mensaje JSON", e)
            }
        }
    }
}

data class ParcelaMap(
    val id: String,
    val nombreParcela: String?,
    val variedad: String?,
    val areaM2: Int,
    val umbralHumedad: Float,
    val umbralTemp: Float,
    val indiceMaduracion: Float,
    val fechaCosecha: Date?,
    val activa: Boolean,
    val humedad: Float,
    val temperatura: Float
)
