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

// Se monta en '/api/riegos'

// Todos pueden ver historial de riego
router.get('/', protegerRuta, obtenerRiegos);
router.get('/:id', protegerRuta, obtenerRiegoPorId);

// Solo superusuarios y administradores pueden controlar o programar riego
router.post('/', protegerRuta, permitirRoles('superusuario', 'administrador'), crearRiego);
router.put('/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), actualizarRiego);
router.delete('/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), eliminarRiego);

module.exports = router;
