package mx.utng.ecoviedos.domain.repository

import mx.utng.ecoviedos.domain.model.Bitacora

interface git fetch origin
BitacoraRepository {

    /**
     * Guarda una nueva bitácora.
     */
    suspend fun guardarBitacora(bitacora: Bitacora)

    /**
     * Obtiene una bitácora por su identificador.
     */
    suspend fun obtenerBitacoraPorId(id: Int): Bitacora?

    /**
     * Obtiene todas las bitácoras registradas para una parcela.
     */
    suspend fun obtenerBitacorasPorParcela(idParcela: String): List<Bitacora>

    /**
     * Obtiene todas las bitácoras.
     */
    suspend fun obtenerTodasLasBitacoras(): List<Bitacora>

    /**
     * Actualiza una bitácora existente.
     */
    suspend fun actualizarBitacora(bitacora: Bitacora)

    /**
     * Elimina una bitácora por su identificador.
     */
    suspend fun eliminarBitacora(id: Int)
}