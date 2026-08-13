const Cava = require('../models/Cava');

// @desc    Obtener todas las cavas
// @route   GET /api/cavas
// @access  Public
exports.obtenerCavas = async (req, res, next) => {
  try {
    const cavas = await Cava.find();
    res.status(200).json(cavas);
  } catch (error) {
    next(error);
  }
};

// @desc    Vincular sensor a una cava
// @route   PUT /api/cavas/:id/sensor
// @access  Private
exports.vincularSensor = async (req, res, next) => {
  try {
    const { sensorId } = req.body;
    const cava = await Cava.findByIdAndUpdate(
      req.params.id,
      { sensorId },
      { new: true, runValidators: true }
    );

    if (!cava) {
      return res.status(404).json({ message: 'Cava no encontrada' });
    }

    res.status(200).json(cava);
  } catch (error) {
    next(error);
  }
};

// @desc    Actualizar número de botellas
// @route   PUT /api/cavas/:id/botellas
// @access  Private
exports.actualizarBotellas = async (req, res, next) => {
  try {
    const { botellasActuales } = req.body;
    const cava = await Cava.findByIdAndUpdate(
      req.params.id,
      { botellasActuales },
      { new: true, runValidators: true }
    );

    if (!cava) {
      return res.status(404).json({ message: 'Cava no encontrada' });
    }

    res.status(200).json(cava);
  } catch (error) {
    next(error);
  }
};
