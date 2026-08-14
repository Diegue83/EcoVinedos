package mx.utng.ecoviedos.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Modelo de respuesta tras una carga exitosa de archivo.
 *
 * @property imageUrl URL pública donde se puede acceder a la imagen subida.
 */
data class UploadResponse(
    val imageUrl: String
)

/**
 * Interfaz de Retrofit para el servicio de carga de archivos (Imágenes).
 *
 * Este servicio se comunica con el endpoint del backend que utiliza Multer para
 * el procesamiento de archivos multipart/form-data.
 */
interface UploadService {
    /**
     * Sube una imagen al servidor.
     *
     * @param token Token de autenticación del usuario.
     * @param image Parte del cuerpo multipart que contiene el archivo de imagen.
     *              Se recomienda usar un nombre de campo "image" para coincidir con la configuración del servidor.
     * @return [Response] que contiene la [UploadResponse] con la URL de la imagen.
     */
    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>
}

