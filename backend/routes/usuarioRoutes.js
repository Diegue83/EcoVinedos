const express = require('express');
const router = express.Router();
const {
  login,
  crearUsuario,
  obtenerUsuarios,
  obtenerUsuarioPorId,
  actualizarUsuario,
  eliminarUsuario,
  solicitarRecuperacion,
  verificarCodigo,
  restablecerContraseña
} = require('../controllers/usuarioController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

// Rutas públicas
router.post('/login', login);
router.post('/auth/forgot-password', solicitarRecuperacion);
router.post('/auth/verify-code', verificarCodigo);
router.post('/auth/reset-password', restablecerContraseña);

// Solo superusuarios pueden gestionar usuarios
router.get('/usuarios', protegerRuta, permitirRoles('superusuario'), obtenerUsuarios);
router.get('/usuarios/:id', protegerRuta, permitirRoles('superusuario'), obtenerUsuarioPorId);
router.post('/usuarios', protegerRuta, permitirRoles('superusuario'), crearUsuario);
router.put('/usuarios/:id', protegerRuta, permitirRoles('superusuario'), actualizarUsuario);
router.delete('/usuarios/:id', protegerRuta, permitirRoles('superusuario'), eliminarUsuario);

module.exports = router;
