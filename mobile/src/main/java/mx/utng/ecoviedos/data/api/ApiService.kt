package mx.utng.ecoviedos.data.api

import mx.utng.ecoviedos.domain.model.Parcela
import retrofit2.http.GET

interface ApiService {
    @GET("api/parcelas")
    suspend fun getParcelas(): List<Parcela>
}
