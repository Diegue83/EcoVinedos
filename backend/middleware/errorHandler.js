const errorHandler = (err, req, res, next) => {
  console.error(err.stack);

  // Error de ID inválido de Mongoose
  if (err.name === 'CastError') {
    return res.status(400).json({ mensaje: 'ID no válido' });
  }

  // Error de valor duplicado (ej. correo ya registrado)
  if (err.code === 11000) {
    return res.status(400).json({ mensaje: 'Ese correo ya está registrado' });
  }

  // Error de validación de Mongoose
  if (err.name === 'ValidationError') {
    const mensajes = Object.values(err.errors).map((e) => e.message);
    return res.status(400).json({ mensaje: mensajes.join(', ') });
  }

  res.status(err.statusCode || 500).json({
    mensaje: err.message || 'Error interno del servidor'
  });
};

module.exports = errorHandler;