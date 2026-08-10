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

// Todos pueden ver parcelas si están autenticados
router.get('/parcelas', protegerRuta, obtenerParcelas);
router.get('/parcelas/:id', protegerRuta, obtenerParcelaPorId);

// Solo superusuarios y administradores pueden gestionar parcelas y nodos
router.post('/parcelas', protegerRuta, permitirRoles('superusuario', 'administrador'), crearParcela);
router.put('/parcelas/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), actualizarParcela);
router.delete('/parcelas/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), eliminarParcela);

module.exports = router;
