const express = require('express');
const router = express.Router();
const {
  crearParcela,
  obtenerParcelas,
  obtenerParcelaPorId,
  actualizarParcela,
  eliminarParcela
} = require('../controllers/parcelaController');
const { protegerRuta, permitirRoles } = require('../middleware/authMiddleware');

// Cualquier usuario autenticado puede consultar parcelas
router.get('/parcelas', protegerRuta, obtenerParcelas);
router.get('/parcelas/:id', protegerRuta, obtenerParcelaPorId);

// Solo administradores pueden crear, editar o eliminar parcelas
router.post('/parcelas', protegerRuta, permitirRoles('administrador'), crearParcela);
router.put('/parcelas/:id', protegerRuta, permitirRoles('administrador'), actualizarParcela);
router.delete('/parcelas/:id', protegerRuta, permitirRoles('administrador'), eliminarParcela);

module.exports = router;