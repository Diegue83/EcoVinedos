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

// Se monta en '/api/bitacoras'

// Cualquier usuario autenticado puede ver y crear entradas (registran su propia actividad)
router.get('/', protegerRuta, obtenerBitacoras);
router.get('/:id', protegerRuta, obtenerBitacoraPorId);
router.post('/', protegerRuta, crearBitacora);

// Solo administradores pueden editar o eliminar el historial
router.put('/:id', protegerRuta, permitirRoles('administrador'), actualizarBitacora);
router.delete('/:id', protegerRuta, permitirRoles('administrador'), eliminarBitacora);

module.exports = router;