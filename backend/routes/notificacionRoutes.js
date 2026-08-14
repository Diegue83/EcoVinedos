const express = require('express');
const router = express.Router();
const {
    obtenerMisNotificaciones,
    cambiarEstado
} = require('../controllers/notificacionController');
const { protegerRuta } = require('../middleware/authMiddleware');

// Se asume que este router se monta en '/api/notificaciones' en server.js

// Obtener notificaciones (requiere estar autenticado)
router.get('/', protegerRuta, obtenerMisNotificaciones);

// Cambiar estado (requiere estar autenticado)
router.put('/:id/estado', protegerRuta, cambiarEstado);

module.exports = router;
