const jwt = require('jsonwebtoken');
const Usuario = require('../models/Usuario');
const { enviarEmailRecuperacion } = require('../utils/emailService');

const generarToken = (id) => {
  return jwt.sign({ id }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '7d'
  });
};

// @desc    Iniciar sesión
// @route   POST /api/login
const login = async (req, res, next) => {
  try {
    const { correo, contraseña, fcmToken } = req.body;

    if (!correo || !contraseña) {
      return res.status(400).json({ mensaje: 'Correo y contraseña son obligatorios' });
    }

    const usuario = await Usuario.findOne({ correo }).select('+contraseña');
    if (!usuario || !(await usuario.compararContraseña(contraseña))) {
      return res.status(401).json({ mensaje: 'Credenciales inválidas' });
    }

    // Actualizar token push si se proporciona
    if (fcmToken) {
        usuario.fcmToken = fcmToken;
        await usuario.save();
    }

    res.json({
      _id: usuario._id,
      nombre: usuario.nombre,
      correo: usuario.correo,
      rol: usuario.rol,
      token: generarToken(usuario._id)
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Crear usuario
// @route   POST /api/usuarios
const crearUsuario = async (req, res, next) => {
  try {
    const { nombre, correo, contraseña, rol, telefono } = req.body;

    const usuario = await Usuario.create({ nombre, correo, contraseña, rol, telefono });

    res.status(201).json({
      _id: usuario._id,
      nombre: usuario.nombre,
      correo: usuario.correo,
      rol: usuario.rol,
      telefono: usuario.telefono,
      fechaRegistro: usuario.fechaRegistro
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Obtener todos los usuarios
// @route   GET /api/usuarios
const obtenerUsuarios = async (req, res, next) => {
  try {
    const usuarios = await Usuario.find();
    res.json(usuarios);
  } catch (error) {
    next(error);
  }
};

// @desc    Obtener un usuario por ID
// @route   GET /api/usuarios/:id
const obtenerUsuarioPorId = async (req, res, next) => {
  try {
    const usuario = await Usuario.findById(req.params.id);
    if (!usuario) {
      return res.status(404).json({ mensaje: 'Usuario no encontrado' });
    }
    res.json(usuario);
  } catch (error) {
    next(error);
  }
};

// @desc    Actualizar usuario
// @route   PUT /api/usuarios/:id
const actualizarUsuario = async (req, res, next) => {
  try {
    const { nombre, correo, rol, telefono, contraseña } = req.body;

    const usuario = await Usuario.findById(req.params.id).select('+contraseña');
    if (!usuario) {
      return res.status(404).json({ mensaje: 'Usuario no encontrado' });
    }

    if (nombre) usuario.nombre = nombre;
    if (correo) usuario.correo = correo;
    if (rol) usuario.rol = rol;
    if (telefono) usuario.telefono = telefono;
    if (contraseña) usuario.contraseña = contraseña; // se re-hashea gracias al pre('save')

    const usuarioActualizado = await usuario.save();

    res.json({
      _id: usuarioActualizado._id,
      nombre: usuarioActualizado.nombre,
      correo: usuarioActualizado.correo,
      rol: usuarioActualizado.rol,
      telefono: usuarioActualizado.telefono
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Eliminar usuario
// @route   DELETE /api/usuarios/:id
const eliminarUsuario = async (req, res, next) => {
  try {
    const usuario = await Usuario.findById(req.params.id);
    if (!usuario) {
      return res.status(404).json({ mensaje: 'Usuario no encontrado' });
    }
    await usuario.deleteOne();
    res.json({ mensaje: 'Usuario eliminado correctamente' });
  } catch (error) {
    next(error);
  }
};

// @desc    Solicitar código de recuperación
// @route   POST /api/auth/forgot-password
const solicitarRecuperacion = async (req, res, next) => {
  try {
    const { correo } = req.body;
    const usuario = await Usuario.findOne({ correo });

    if (!usuario) {
      return res.status(404).json({ mensaje: 'No existe un usuario con ese correo' });
    }

    // Generar código de 6 dígitos
    const codigo = Math.floor(100000 + Math.random() * 900000).toString();
    usuario.resetPasswordCode = codigo;
    usuario.resetPasswordExpires = Date.now() + 10 * 60 * 1000; // 10 minutos
    await usuario.save();

    // Enviar el correo usando Nodemailer
    const enviado = await enviarEmailRecuperacion(correo, codigo);

    if (enviado) {
      res.json({ mensaje: 'Código enviado al correo' });
    } else {
      res.status(500).json({ mensaje: 'Error al enviar el correo. Inténtalo más tarde.' });
    }
  } catch (error) {
    next(error);
  }
};

// @desc    Verificar código de recuperación
// @route   POST /api/auth/verify-code
const verificarCodigo = async (req, res, next) => {
  try {
    const { correo, codigo } = req.body;
    const usuario = await Usuario.findOne({
      correo,
      resetPasswordCode: codigo,
      resetPasswordExpires: { $gt: Date.now() }
    });

    if (!usuario) {
      return res.status(400).json({ mensaje: 'Código inválido o expirado' });
    }

    res.json({ mensaje: 'Código verificado correctamente' });
  } catch (error) {
    next(error);
  }
};

// @desc    Restablecer contraseña
// @route   POST /api/auth/reset-password
const restablecerContraseña = async (req, res, next) => {
  try {
    const { correo, codigo, nuevaContraseña } = req.body;
    const usuario = await Usuario.findOne({
      correo,
      resetPasswordCode: codigo,
      resetPasswordExpires: { $gt: Date.now() }
    });

    if (!usuario) {
      return res.status(400).json({ mensaje: 'Código inválido o expirado' });
    }

    usuario.contraseña = nuevaContraseña;
    usuario.resetPasswordCode = undefined;
    usuario.resetPasswordExpires = undefined;
    await usuario.save();

    res.json({ mensaje: 'Contraseña actualizada correctamente' });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  login,
  crearUsuario,
  obtenerUsuarios,
  obtenerUsuarioPorId,
  actualizarUsuario,
  eliminarUsuario,
  solicitarRecuperacion,
  verificarCodigo,
  restablecerContraseña
};