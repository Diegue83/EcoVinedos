const express = require('express');
const router = express.Router();
const {
  login,
  solicitarRecuperacion,
  verificarCodigo,
  restablecerContraseña
} = require('../controllers/usuarioController');

/**
 * Rutas de Autenticación Global.
 * Estas rutas son públicas y se montan directamente en /api
 */

router.post('/login', login);
router.post('/auth/forgot-password', solicitarRecuperacion);
router.post('/auth/verify-code', verificarCodigo);
router.post('/auth/reset-password', restablecerContraseña);

module.exports = router;
