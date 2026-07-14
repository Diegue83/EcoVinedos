package mx.utng.ecoviedos.domain.usecase

import android.content.Context
import mx.utng.ecoviedos.domain.model.Bitacora
import mx.utng.ecoviedos.domain.repository.BitacoraRepository
import java.io.File

class ObtenerBitacorasUseCase(
    private val repository: BitacoraRepository
) {

    operator fun invoke( idParcela: String): List<File>  {
        return repository.obtenerAudiosPorParcela(idParcela)
    }
}