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
router.get('/usuarios', obtenerUsuarios);
router.get('/usuarios/:id', obtenerUsuarioPorId);

// Solo administradores pueden crear, editar o eliminar usuarios
//router.post('/usuarios', crearUsuario); 
router.post('/usuarios', crearUsuario);
router.put('/usuarios/:id', permitirRoles('administrador'), actualizarUsuario);
router.delete('/usuarios/:id', permitirRoles('administrador'), eliminarUsuario);

module.exports = router;