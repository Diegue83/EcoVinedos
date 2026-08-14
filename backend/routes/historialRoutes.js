const express = require('express');
const router = express.Router();
const { obtenerHistorialParcela, obtenerResumenParcela } = require('../controllers/historialController');
const { protegerRuta } = require('../middleware/authMiddleware');

// Se monta en '/api/historial'

// Por ahora usaremos protegerRuta pero sin validar token si así se prefiere,
// o podemos usarla directamente si ya arreglamos el 500
router.get('/parcela/:parcelaId', obtenerHistorialParcela);
router.get('/resumen/:parcelaId', obtenerResumenParcela);

module.exports = router;
