const Cava = require('../models/Cava');

// @desc    Obtener todas las cavas
// @route   GET /api/cavas
const obtenerCavas = async (req, res, next) => {
  try {
    const cavas = await Cava.find();
    res.json(cavas);
  } catch (error) {
    next(error);
  }
};

// @desc    Crear o actualizar sección de cava
// @route   POST /api/cavas
const guardarCava = async (req, res, next) => {
  try {
    const { _id, nombre, tipo, capacidadBotellas, botellasActuales, sensorId } = req.body;

    let cava;
    if (_id) {
      cava = await Cava.findByIdAndUpdate(_id, req.body, { new: true, runValidators: true });
    } else {
      cava = await Cava.create(req.body);
    }

    res.status(201).json(cava);
  } catch (error) {
    next(error);
  }
};

// @desc    Actualizar botellas
// @route   PUT /api/cavas/:id/botellas
const actualizarBotellas = async (req, res, next) => {
  try {
    const { botellasActuales } = req.body;
    const cava = await Cava.findByIdAndUpdate(req.params.id, { botellasActuales }, { new: true });
    if (!cava) return res.status(404).json({ mensaje: 'Cava no encontrada' });
    res.json(cava);
  } catch (error) {
    next(error);
  }
};

// @desc    Vincular sensor BLE a cava
// @route   PUT /api/cavas/:id/sensor
const vincularSensor = async (req, res, next) => {
  try {
    const { sensorId } = req.body;
    const cava = await Cava.findByIdAndUpdate(req.params.id, { sensorId }, { new: true });
    if (!cava) return res.status(404).json({ mensaje: 'Cava no encontrada' });
    res.json(cava);
  } catch (error) {
    next(error);
  }
};

// @desc    Eliminar sección de cava
// @route   DELETE /api/cavas/:id
const eliminarCava = async (req, res, next) => {
  try {
    const cava = await Cava.findByIdAndDelete(req.params.id);
    if (!cava) return res.status(404).json({ mensaje: 'Cava no encontrada' });
    res.json({ mensaje: 'Eliminado correctamente' });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  obtenerCavas,
  guardarCava,
  actualizarBotellas,
  vincularSensor,
  eliminarCava
};
