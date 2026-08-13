const express = require('express');
const router = express.Router();
const {
  obtenerCavas,
  guardarCava,
  actualizarBotellas,
  vincularSensor,
  eliminarCava
} = require('../controllers/cavaController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

// Obtener cavas es público para la TV
router.get('/', obtenerCavas);

// Gestión protegida
router.post('/', protegerRuta, permitirRoles('superusuario', 'enologo'), guardarCava);
router.put('/:id/botellas', protegerRuta, permitirRoles('superusuario', 'enologo'), actualizarBotellas);
router.put('/:id/sensor', protegerRuta, permitirRoles('superusuario', 'enologo'), vincularSensor);
router.delete('/:id', protegerRuta, permitirRoles('superusuario', 'enologo'), eliminarCava);

module.exports = router;
