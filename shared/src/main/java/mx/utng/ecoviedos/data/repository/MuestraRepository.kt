package mx.utng.ecoviedos.data.repository

import mx.utng.ecoviedos.data.remote.MuestraRequest
import mx.utng.ecoviedos.data.remote.MuestraResponse
import mx.utng.ecoviedos.data.remote.RetrofitClient

/**
 * Repositorio para la gestión de muestras analíticas y de laboratorio en las parcelas.
 *
 * Facilita el registro de parámetros de maduración (Grados Brix, pH, acidez) y el seguimiento
 * de la calidad del fruto a lo largo del tiempo para determinar el momento óptimo de cosecha.
 */
class MuestraRepository {

    /**
     * Registra una nueva muestra técnica en el servidor.
     *
     * @param token Token de autenticación del usuario (usualmente enólogo o administrador).
     * @param request Datos técnicos de la muestra encapsulados en [MuestraRequest].
     * @return [Result] con el objeto [MuestraResponse] confirmado por el backend.
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
     * Recupera el historial cronológico de muestras tomadas en una parcela específica.
     *
     * @param token Token de autenticación del usuario.
     * @param parcelaId Identificador único de la parcela a consultar.
     * @return [Result] con la lista completa de [MuestraResponse] para dicha parcela.
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

