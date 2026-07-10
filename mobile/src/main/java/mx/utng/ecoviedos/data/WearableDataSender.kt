package mx.utng.ecoviedos.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import mx.utng.ecoviedos.domain.model.Parcela

class WearableDataSender(private val context: Context) {
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val gson = Gson()

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
