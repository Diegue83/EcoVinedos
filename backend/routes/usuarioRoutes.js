const express = require('express');
const router = express.Router();
const {
  obtenerUsuarios,
  obtenerUsuarioPorId,
  crearUsuario,
  actualizarUsuario,
  eliminarUsuario
} = require('../controllers/usuarioController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

// Se monta en '/api/usuarios'

// Solo superusuarios pueden gestionar usuarios
router.get('/', protegerRuta, permitirRoles('superusuario'), obtenerUsuarios);
router.get('/:id', protegerRuta, permitirRoles('superusuario'), obtenerUsuarioPorId);
router.post('/', protegerRuta, permitirRoles('superusuario'), crearUsuario);
router.put('/:id', protegerRuta, permitirRoles('superusuario'), actualizarUsuario);
router.delete('/:id', protegerRuta, permitirRoles('superusuario'), eliminarUsuario);

module.exports = router;
