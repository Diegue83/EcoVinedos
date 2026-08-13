const express = require('express');
const router = express.Router();
const {
  obtenerEventos,
  crearEvento,
  actualizarEvento,
  eliminarEvento
} = require('../controllers/eventoController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

// Obtener eventos es público (para la TV y la App)
router.get('/', obtenerEventos);

// Solo administradores o superusuarios pueden gestionar eventos
router.post('/', protegerRuta, permitirRoles('superusuario', 'administrador'), crearEvento);
router.put('/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), actualizarEvento);
router.delete('/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), eliminarEvento);

module.exports = router;
