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

// Cualquier usuario autenticado puede ver, crear y actualizar riegos
// (un trabajador necesita poder programar riego y marcarlo como completado)
router.get('/riegos', protegerRuta, obtenerRiegos);
router.get('/riegos/:id', protegerRuta, obtenerRiegoPorId);
router.post('/riegos', protegerRuta, crearRiego);
router.put('/riegos/:id', protegerRuta, actualizarRiego);

// Solo administradores pueden eliminar registros de riego
router.delete('/riegos/:id', protegerRuta, permitirRoles('administrador'), eliminarRiego);

module.exports = router;