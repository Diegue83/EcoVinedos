package mx.utng.ecoviedos.domain.usecase

import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.repository.BitacoraRepository

class ObtenerTodasLasBitacorasUseCase(
    private val repository: BitacoraRepository
) {
    suspend operator fun invoke(): List<Bitacora> {
        return repository.obtenerTodasLasBitacoras()
    }
}