package mx.utng.ecoviedos.tv.presentation

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generador de códigos QR para facilitar la vinculación de dispositivos.
 */
object QrGenerator {
    /**
     * Crea un objeto [Bitmap] que contiene el código QR del texto proporcionado.
     *
     * @param content Texto a codificar (normalmente el Pairing Code).
     * @param size Dimensiones del bitmap cuadrado.
     * @return Bitmap con el QR generado o null en caso de error.
     */
    fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
