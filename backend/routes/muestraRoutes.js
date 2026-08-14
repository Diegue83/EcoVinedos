const express = require('express');
const router = express.Router();
const { registrarMuestra, obtenerHistorialPorParcela } = require('../controllers/muestraController');
const { protegerRuta } = require('../middleware/authMiddleware');

// Se monta en '/api/muestras'

// Todos los roles pueden registrar muestras y ver historial (superusuario, administrador, trabajador)
router.post('/', protegerRuta, registrarMuestra);
router.get('/parcela/:parcelaId', protegerRuta, obtenerHistorialPorParcela);

module.exports = router;
