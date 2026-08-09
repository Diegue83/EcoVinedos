const express = require('express');
const router = express.Router();
const { registrarMuestra, obtenerHistorialPorParcela } = require('../controllers/muestraController');
const { protegerRuta } = require('../middleware/authMiddleware');

router.post('/muestras', registrarMuestra);
router.get('/muestras/parcela/:parcelaId', obtenerHistorialPorParcela);

module.exports = router;
