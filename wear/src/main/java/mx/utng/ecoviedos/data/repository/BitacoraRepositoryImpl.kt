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
        TODO("Not yet implemented")
    }

    override suspend fun obtenerBitacorasPorParcela(idParcela: String): List<Bitacora> {
        return mutex.withLock {
            bitacoras.filter { it.idParcela == idParcela }
        }
    }

    override suspend fun obtenerTodasLasBitacoras(): List<Bitacora> {
        TODO("Not yet implemented")
    }

    override suspend fun actualizarBitacora(bitacora: Bitacora) {
        TODO("Not yet implemented")
    }

    override suspend fun eliminarBitacora(id: Int) {
        TODO("Not yet implemented")
    }
}