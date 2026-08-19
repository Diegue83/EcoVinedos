package mx.utng.ecoviedos.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.repository.BitacoraRepository

/**
 * Implementación en memoria del repositorio de bitácoras para Wear OS.
 *
 * Utiliza un [Mutex] para garantizar la seguridad entre hilos durante las operaciones
 * de lectura y escritura en la lista persistente de eventos.
 */
class BitacoraRepositoryImpl : BitacoraRepository {

    private val bitacoras = mutableListOf<Bitacora>()
    private val mutex = Mutex()

    /**
     * Almacena una nueva entrada en la bitácora local.
     */
    override suspend fun guardarBitacora(bitacora: Bitacora) {
        mutex.withLock {
            bitacoras.add(bitacora)
        }
    }

    /**
     * Busca una bitácora por su identificador único.
     */
    override suspend fun obtenerBitacoraPorId(id: Int): Bitacora? {
        return mutex.withLock {
            bitacoras.find { it.id == id }
        }
    }

    /**
     * Filtra las bitácoras asociadas a una parcela específica.
     */
    override suspend fun obtenerBitacorasPorParcela(idParcela: String): List<Bitacora> {
        return mutex.withLock {
            bitacoras.filter { it.idParcela == idParcela }
        }
    }

    /**
     * Recupera todos los registros históricos.
     */
    override suspend fun obtenerTodasLasBitacoras(): List<Bitacora> {
        return mutex.withLock {
            bitacoras.toList()
        }
    }

    /**
     * Reemplaza una bitácora existente con datos actualizados.
     */
    override suspend fun actualizarBitacora(bitacora: Bitacora) {
        mutex.withLock {
            val index = bitacoras.indexOfFirst { it.id == bitacora.id }
            if (index != -1) {
                bitacoras[index] = bitacora
            }
        }
    }

    /**
     * Borra permanentemente una bitácora del repositorio.
     */
    override suspend fun eliminarBitacora(id: Int) {
        mutex.withLock {
            bitacoras.removeAll { it.id == id }
        }
    }

    /**
     * Devuelve los archivos de audio grabados para una parcela.
     */
    override fun obtenerAudiosPorParcela(idParcela: String): List<java.io.File> {
        return emptyList()
    }
}
