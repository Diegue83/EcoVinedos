const jwt = require('jsonwebtoken');
const Usuario = require('../models/Usuario');

// Verifica que el usuario haya enviado un token válido
const protegerRuta = async (req, res, next) => {
  let token;

  if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
    try {
      token = req.headers.authorization.split(' ')[1];
      const decoded = jwt.verify(token, process.env.JWT_SECRET);

      req.usuario = await Usuario.findById(decoded.id);
      if (!req.usuario) {
        return res.status(401).json({ mensaje: 'Usuario no encontrado' });
      }

      return next();
    } catch (error) {
      return res.status(401).json({ mensaje: 'Token no válido o expirado' });
    }
  }

  return res.status(401).json({ mensaje: 'No autorizado, falta el token' });
};

// Restringe el acceso según el rol del usuario
const permitirRoles = (...roles) => {
  return (req, res, next) => {
    if (!roles.includes(req.usuario.rol)) {
      return res.status(403).json({ mensaje: 'No tienes permisos para esta acción' });
    }
    next();
  };
};

module.exports = { protegerRuta, permitirRoles };