const express = require('express');
const router = express.Router();
const {
  login,
  crearUsuario,
  obtenerUsuarios,
  obtenerUsuarioPorId,
  actualizarUsuario,
  eliminarUsuario
} = require('../controllers/usuarioController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

// Ruta pública
router.post('/login', login);

// Solo superusuarios pueden gestionar usuarios
router.get('/usuarios', protegerRuta, permitirRoles('superusuario'), obtenerUsuarios);
router.get('/usuarios/:id', protegerRuta, permitirRoles('superusuario'), obtenerUsuarioPorId);
router.post('/usuarios', protegerRuta, permitirRoles('superusuario'), crearUsuario);
router.put('/usuarios/:id', protegerRuta, permitirRoles('superusuario'), actualizarUsuario);
router.delete('/usuarios/:id', protegerRuta, permitirRoles('superusuario'), eliminarUsuario);

module.exports = router;
