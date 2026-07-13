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

// Rutas protegidas (requieren token)
router.get('/usuarios', protegerRuta, obtenerUsuarios);
router.get('/usuarios/:id', protegerRuta, obtenerUsuarioPorId);

// Solo administradores pueden crear, editar o eliminar usuarios
router.post('/usuarios', protegerRuta, permitirRoles('administrador'), crearUsuario);
router.put('/usuarios/:id', protegerRuta, permitirRoles('administrador'), actualizarUsuario);
router.delete('/usuarios/:id', protegerRuta, permitirRoles('administrador'), eliminarUsuario);

module.exports = router;