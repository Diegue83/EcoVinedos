const express = require('express');
const router = express.Router();
const upload = require('../utils/uploadConfig');
const { protegerRuta } = require('../middleware/authMiddleware');

// Ruta para subir una sola imagen
router.post('/image', protegerRuta, upload.single('image'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ mensaje: 'No se subió ningún archivo' });
  }

  // Devolver la URL de la imagen
  const imageUrl = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;
  res.json({ imageUrl });
});

module.exports = router;
