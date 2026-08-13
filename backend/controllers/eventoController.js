const Evento = require('../models/Evento');

// @desc    Obtener todos los eventos
// @route   GET /api/eventos
const obtenerEventos = async (req, res, next) => {
  try {
    const { tipo } = req.query;
    const filtro = tipo ? { tipo } : {};
    const eventos = await Evento.find(filtro).sort({ fecha: 1 });
    res.json(eventos);
  } catch (error) {
    next(error);
  }
};

// @desc    Crear un nuevo evento/atracción
// @route   POST /api/eventos
const crearEvento = async (req, res, next) => {
  try {
    const evento = await Evento.create(req.body);
    res.status(201).json(evento);
  } catch (error) {
    next(error);
  }
};

// @desc    Actualizar un evento
// @route   PUT /api/eventos/:id
const actualizarEvento = async (req, res, next) => {
  try {
    const evento = await Evento.findByIdAndUpdate(req.params.id, req.body, {
      new: true,
      runValidators: true
    });
    if (!evento) return res.status(404).json({ mensaje: 'No encontrado' });
    res.json(evento);
  } catch (error) {
    next(error);
  }
};

// @desc    Eliminar un evento
// @route   DELETE /api/eventos/:id
const eliminarEvento = async (req, res, next) => {
  try {
    const evento = await Evento.findByIdAndDelete(req.params.id);
    if (!evento) return res.status(404).json({ mensaje: 'No encontrado' });
    res.json({ mensaje: 'Eliminado correctamente' });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  obtenerEventos,
  crearEvento,
  actualizarEvento,
  eliminarEvento
};
