package mx.utng.ecoviedos.domain.usecase

import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.repository.BitacoraRepository

class ObtenerBitacorasUseCase(
    private val repository: BitacoraRepository
) {

    suspend operator fun invoke(idParcela: String): List<Bitacora> {
        return repository.obtenerBitacorasPorParcela(idParcela)
    }
}