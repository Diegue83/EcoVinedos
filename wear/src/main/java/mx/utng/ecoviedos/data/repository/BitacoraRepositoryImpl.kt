package mx.utng.ecoviedos.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.repository.BitacoraRepository

class BitacoraRepositoryImpl : BitacoraRepository {

    private val bitacoras = mutableListOf<Bitacora>()
    private val mutex = Mutex()

    override suspend fun guardarBitacora(bitacora: Bitacora) {
        mutex.withLock {
            bitacoras.add(bitacora)
        }
    }

    override suspend fun obtenerBitacoraPorId(id: Int): Bitacora? {
        return mutex.withLock {
            bitacoras.find { it.id == id }
        }
    }

    override suspend fun obtenerBitacorasPorParcela(idParcela: String): List<Bitacora> {
        return mutex.withLock {
            bitacoras.filter { it.idParcela == idParcela }
        }
    }

    override suspend fun obtenerTodasLasBitacoras(): List<Bitacora> {
        return mutex.withLock {
            bitacoras.toList()
        }
    }

    override suspend fun actualizarBitacora(bitacora: Bitacora) {
        mutex.withLock {
            val index = bitacoras.indexOfFirst { it.id == bitacora.id }
            if (index != -1) {
                bitacoras[index] = bitacora
            }
        }
    }

    override suspend fun eliminarBitacora(id: Int) {
        mutex.withLock {
            bitacoras.removeAll { it.id == id }
        }
    }
}