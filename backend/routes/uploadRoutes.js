/**
 * Rutas para la carga de archivos multimedia.
 *
 * Utiliza el middleware Multer para procesar subidas de imágenes
 * y las expone a través de una carpeta estática configurada en server.js.
 */
const express = require('express');
const router = express.Router();
const upload = require('../utils/uploadConfig');
const { protegerRuta } = require('../middleware/authMiddleware');

/**
 * @route POST /api/upload/image
 * @desc Sube una sola imagen a Cloudinary
 * @access Privado (Requiere Token JWT)
 */
router.post('/image', protegerRuta, upload.single('image'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ mensaje: 'No se subió ningún archivo' });
  }

  // Cloudinary devuelve la URL segura en req.file.path o req.file.secure_url
  const imageUrl = req.file.path || req.file.secure_url;
  res.json({ imageUrl });
});


module.exports = router;

