package mx.utng.ecoviedos.domain.repository

import android.app.Application
import android.content.Context
import mx.utng.ecoviedos.domain.model.Bitacora
import java.io.File

interface BitacoraRepository {

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
    fun obtenerAudiosPorParcela(idParcela: String): List<File>

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