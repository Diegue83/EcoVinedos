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

// Se monta en '/api/parcelas'

// Todos pueden ver parcelas si están autenticados
router.get('/', protegerRuta, obtenerParcelas);
router.get('/:id', protegerRuta, obtenerParcelaPorId);

// Solo superusuarios y administradores pueden gestionar parcelas y nodos
router.post('/', protegerRuta, permitirRoles('superusuario', 'administrador'), crearParcela);
router.put('/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), actualizarParcela);
router.delete('/:id', protegerRuta, permitirRoles('superusuario', 'administrador'), eliminarParcela);

module.exports = router;
