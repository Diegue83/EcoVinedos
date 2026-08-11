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
router.get('/riegos', protegerRuta, obtenerRiegos);
router.get('/riegos/:id', protegerRuta, obtenerRiegoPorId);

// Solo superusuarios y administradores pueden controlar o programar riego
router.post('/riegos', protegerRuta, permitirRoles('superusuario', 'administrador'), crearRiego);
router.put('/riegos/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), actualizarRiego);
router.delete('/riegos/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), eliminarRiego);

module.exports = router;
