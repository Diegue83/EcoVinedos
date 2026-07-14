const Bitacora = require('../models/Bitacora');

// @desc    Crear entrada de bitácora
// @route   POST /api/bitacoras
const crearBitacora = async (req, res, next) => {
  try {
    const { parcela, accion, descripcion, fecha } = req.body;

    const entrada = await Bitacora.create({
      parcela,
      usuario: req.usuario._id, // se toma del token, no del body
      accion,
      descripcion,
      fecha
    });

    const entradaConDatos = await entrada.populate([
      { path: 'parcela', select: 'nombre ubicacion' },
      { path: 'usuario', select: 'nombre correo rol' }
    ]);

    res.status(201).json(entradaConDatos);
  } catch (error) {
    next(error);
  }
};

// @desc    Obtener todas las bitácoras (opcionalmente filtradas por parcela)
// @route   GET /api/bitacoras
// @route   GET /api/bitacoras?parcela=ID
const obtenerBitacoras = async (req, res, next) => {
  try {
    const filtro = {};
    if (req.query.parcela) filtro.parcela = req.query.parcela;

    const bitacoras = await Bitacora.find(filtro)
      .populate('parcela', 'nombre ubicacion')
      .populate('usuario', 'nombre correo rol')
      .sort({ fecha: -1 });

    res.json(bitacoras);
  } catch (error) {
    next(error);
  }
};

// @desc    Obtener una entrada por ID
// @route   GET /api/bitacoras/:id
const obtenerBitacoraPorId = async (req, res, next) => {
  try {
    const entrada = await Bitacora.findById(req.params.id)
      .populate('parcela', 'nombre ubicacion')
      .populate('usuario', 'nombre correo rol');

    if (!entrada) {
      return res.status(404).json({ mensaje: 'Entrada de bitácora no encontrada' });
    }
    res.json(entrada);
  } catch (error) {
    next(error);
  }
};

// @desc    Actualizar entrada de bitácora
// @route   PUT /api/bitacoras/:id
const actualizarBitacora = async (req, res, next) => {
  try {
    const { accion, descripcion, fecha } = req.body;

    const entrada = await Bitacora.findById(req.params.id);
    if (!entrada) {
      return res.status(404).json({ mensaje: 'Entrada de bitácora no encontrada' });
    }

    if (accion) entrada.accion = accion;
    if (descripcion !== undefined) entrada.descripcion = descripcion;
    if (fecha) entrada.fecha = fecha;

    const actualizada = await entrada.save();
    res.json(actualizada);
  } catch (error) {
    next(error);
  }
};

// @desc    Eliminar entrada de bitácora
// @route   DELETE /api/bitacoras/:id
const eliminarBitacora = async (req, res, next) => {
  try {
    const entrada = await Bitacora.findById(req.params.id);
    if (!entrada) {
      return res.status(404).json({ mensaje: 'Entrada de bitácora no encontrada' });
    }
    await entrada.deleteOne();
    res.json({ mensaje: 'Entrada de bitácora eliminada correctamente' });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  crearBitacora,
  obtenerBitacoras,
  obtenerBitacoraPorId,
  actualizarBitacora,
  eliminarBitacora
};