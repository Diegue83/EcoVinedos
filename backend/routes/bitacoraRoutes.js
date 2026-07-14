const express = require('express');
const router = express.Router();
const {
  crearBitacora,
  obtenerBitacoras,
  obtenerBitacoraPorId,
  actualizarBitacora,
  eliminarBitacora
} = require('../controllers/bitacoraController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

// Cualquier usuario autenticado puede ver y crear entradas (registran su propia actividad)
router.get('/bitacoras', protegerRuta, obtenerBitacoras);
router.get('/bitacoras/:id', protegerRuta, obtenerBitacoraPorId);
router.post('/bitacoras', protegerRuta, crearBitacora);

// Solo administradores pueden editar o eliminar el historial
router.put('/bitacoras/:id', protegerRuta, permitirRoles('administrador'), actualizarBitacora);
router.delete('/bitacoras/:id', protegerRuta, permitirRoles('administrador'), eliminarBitacora);

module.exports = router;