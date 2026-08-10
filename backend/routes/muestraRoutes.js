const express = require('express');
const router = express.Router();
const { registrarMuestra, obtenerHistorialPorParcela } = require('../controllers/muestraController');
const { protegerRuta } = require('../middleware/authMiddleware');

// Todos los roles pueden registrar muestras y ver historial (superusuario, administrador, trabajador)
router.post('/muestras', protegerRuta, registrarMuestra);
router.get('/muestras/parcela/:parcelaId', protegerRuta, obtenerHistorialPorParcela);

module.exports = router;
