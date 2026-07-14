const Riego = require('../models/Riego');

// @desc    Crear registro de riego
// @route   POST /api/riegos
const crearRiego = async (req, res, next) => {
  try {
    const { parcela, fecha, duracion, litros, estado } = req.body;

    const riego = await Riego.create({ parcela, fecha, duracion, litros, estado });
    const riegoConDatos = await riego.populate('parcela', 'nombre ubicacion');

    res.status(201).json(riegoConDatos);
  } catch (error) {
    next(error);
  }
};

// @desc    Obtener todos los riegos (opcionalmente filtrados por parcela o estado)
// @route   GET /api/riegos
// @route   GET /api/riegos?parcela=ID&estado=completado
const obtenerRiegos = async (req, res, next) => {
  try {
    const filtro = {};
    if (req.query.parcela) filtro.parcela = req.query.parcela;
    if (req.query.estado) filtro.estado = req.query.estado;

    const riegos = await Riego.find(filtro)
      .populate('parcela', 'nombre ubicacion')
      .sort({ fecha: -1 });

    res.json(riegos);
  } catch (error) {
    next(error);
  }
};

// @desc    Obtener un riego por ID
// @route   GET /api/riegos/:id
const obtenerRiegoPorId = async (req, res, next) => {
  try {
    const riego = await Riego.findById(req.params.id).populate('parcela', 'nombre ubicacion');
    if (!riego) {
      return res.status(404).json({ mensaje: 'Riego no encontrado' });
    }
    res.json(riego);
  } catch (error) {
    next(error);
  }
};

// @desc    Actualizar riego (ej. cambiar estado a "completado")
// @route   PUT /api/riegos/:id
const actualizarRiego = async (req, res, next) => {
  try {
    const camposPermitidos = ['fecha', 'duracion', 'litros', 'estado'];

    const riego = await Riego.findById(req.params.id);
    if (!riego) {
      return res.status(404).json({ mensaje: 'Riego no encontrado' });
    }

    camposPermitidos.forEach((campo) => {
      if (req.body[campo] !== undefined) {
        riego[campo] = req.body[campo];
      }
    });

    const riegoActualizado = await riego.save();
    res.json(riegoActualizado);
  } catch (error) {
    next(error);
  }
};

// @desc    Eliminar riego
// @route   DELETE /api/riegos/:id
const eliminarRiego = async (req, res, next) => {
  try {
    const riego = await Riego.findById(req.params.id);
    if (!riego) {
      return res.status(404).json({ mensaje: 'Riego no encontrado' });
    }
    await riego.deleteOne();
    res.json({ mensaje: 'Riego eliminado correctamente' });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  crearRiego,
  obtenerRiegos,
  obtenerRiegoPorId,
  actualizarRiego,
  eliminarRiego
};