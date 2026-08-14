const express = require('express');
const router = express.Router();
const {
    obtenerMisNotificaciones,
    cambiarEstado
} = require('../controllers/notificacionController');
const { protegerRuta } = require('../middleware/authMiddleware');

// Todas las rutas de notificaciones requieren autenticación
router.use(protegerRuta);

router.get('/notificaciones/', obtenerMisNotificaciones);
router.put('/notificaciones/:id/estado', cambiarEstado);

module.exports = router;
