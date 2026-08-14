const express = require('express');
const router = express.Router();
const {
  obtenerCavas,
  crearCava,
  eliminarCava,
  crearSeccion,
  actualizarSeccion,
  eliminarSeccion
} = require('../controllers/cavaController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

/**
 * Rutas para la gestión de Cavas y sus Secciones.
 * El enólogo y el superusuario pueden gestionar estas entidades.
 */

// Obtener todas las cavas (con sus secciones integradas)
router.get('/', obtenerCavas);

// Gestión de Cavas (Entidad Principal)
router.post('/', protegerRuta, permitirRoles('superusuario', 'enologo'), crearCava);
router.delete('/:id', protegerRuta, permitirRoles('superusuario', 'enologo'), eliminarCava);

// Gestión de Secciones
router.post('/secciones', protegerRuta, permitirRoles('superusuario', 'enologo'), crearSeccion);
router.put('/secciones/:id', protegerRuta, permitirRoles('superusuario', 'enologo'), actualizarSeccion);
router.delete('/secciones/:id', protegerRuta, permitirRoles('superusuario', 'enologo'), eliminarSeccion);

module.exports = router;
