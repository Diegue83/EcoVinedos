const express = require('express');
const router = express.Router();
const {
  crearRiego,
  obtenerRiegos,
  obtenerRiegoPorId,
  actualizarRiego,
  eliminarRiego
} = require('../controllers/riegoController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

// Todos pueden ver historial de riego
router.get('/riego', protegerRuta, obtenerRiegos);
router.get('/riego/:id', protegerRuta, obtenerRiegoPorId);

// Solo superusuarios y administradores pueden controlar o programar riego
router.post('/riego', protegerRuta, permitirRoles('superusuario', 'administrador'), crearRiego);
router.put('/riego/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), actualizarRiego);
router.delete('/riego/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), eliminarRiego);

module.exports = router;
