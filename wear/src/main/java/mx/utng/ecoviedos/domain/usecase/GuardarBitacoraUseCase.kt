package mx.utng.ecoviedos.domain.usecase

import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.repository.BitacoraRepository

class GuardarBitacoraUseCase(
    private val repository: BitacoraRepository
) {

    suspend operator fun invoke(bitacora: Bitacora) {
        repository.guardarBitacora(bitacora)
    }
}