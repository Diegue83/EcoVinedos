package mx.utng.ecoviedos.data

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.utng.ecoviedos.domain.model.Parcela
import java.util.Date

class WearableDataService : MessageClient.OnMessageReceivedListener {
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

                // Garantizar que la actualización del repositorio se realice en el hilo principal (Main Thread)
                // para que Jetpack Compose detecte el cambio de estado e inicie la recomposición inmediatamente.
                Handler(Looper.getMainLooper()).post {
                    ParcelaRepository.updateParcelas(parcelasWear)
                    Log.d("WearableDataService", "Sincronización por mensaje completada en Main Thread: ${parcelasWear.size} parcelas.")
                }
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
