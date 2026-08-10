package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.MuestraRequest
import mx.utng.ecoviedos.data.remote.MuestraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la gestión de muestras de laboratorio de las parcelas.
 */
class MuestraRepository {

    /**
     * Registra una nueva muestra (Brix, pH, acidez) para una parcela.
     * 
     * @param token Token de autenticación del usuario.
     * @param request Datos de la muestra a registrar.
     * @return Resultado con la muestra registrada o error.
     */
    suspend fun registrarMuestra(token: String, request: MuestraRequest): Result<MuestraResponse> {
        return try {
            val response = RetrofitClient.muestraService.registrarMuestra("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al registrar muestra: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el historial de muestras de una parcela específica.
     * 
     * @param token Token de autenticación del usuario.
     * @param parcelaId Identificador de la parcela.
     * @return Resultado con el listado de muestras o error.
     */
    suspend fun obtenerHistorial(token: String, parcelaId: String): Result<List<MuestraResponse>> {
        return try {
            val response = RetrofitClient.muestraService.obtenerHistorialPorParcela("Bearer $token", parcelaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener historial: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
