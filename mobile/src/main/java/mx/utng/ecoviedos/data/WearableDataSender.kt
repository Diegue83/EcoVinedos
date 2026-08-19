package mx.utng.ecoviedos.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import mx.utng.ecoviedos.domain.model.Parcela

/**
 * Clase encargada de enviar datos a los dispositivos Wear OS vinculados.
 *
 * Utiliza el MessageClient de Google Play Services para enviar la lista de parcelas
 * serializada en JSON hacia los relojes inteligentes conectados.
 *
 * @property context Contexto de la aplicación.
 */
class WearableDataSender(private val context: Context) {
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val gson = Gson()

    /**
     * Envía la lista de parcelas actual a todos los nodos conectados.
     *
     * @param parcelas Lista de objetos [Parcela] a sincronizar.
     */
    fun sendParcelas(parcelas: List<Parcela>) {
        val json = gson.toJson(parcelas)
        val data = json.toByteArray(Charsets.UTF_8)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                Log.w("WearableDataSender", "No hay relojes conectados para enviar mensaje")
            }
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, "/parcelas_message", data)
                    .addOnSuccessListener { 
                        Log.d("WearableDataSender", "¡MENSAJE ENVIADO INSTANTÁNEAMENTE A: ${node.displayName}!") 
                    }
                    .addOnFailureListener { e -> 
                        Log.e("WearableDataSender", "Fallo al enviar mensaje", e) 
                    }
            }
        }
    }
}
