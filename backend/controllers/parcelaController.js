const Parcela = require('../models/Parcela');

// @desc    Crear parcela
// @route   POST /api/parcelas
const crearParcela = async (req, res, next) => {
  try {
    const { nombre, ubicacion, superficie, cultivo, humedad, temperatura, estado, responsable } =
      req.body;

    const parcela = await Parcela.create({
      nombre,
      ubicacion,
      superficie,
      cultivo,
      humedad,
      temperatura,
      estado,
      responsable
    });

    res.status(201).json(parcela);
  } catch (error) {
    next(error);
  }
};

// @desc    Obtener todas las parcelas
// @route   GET /api/parcelas
const obtenerParcelas = async (req, res, next) => {
  try {
    const parcelas = await Parcela.find().populate('responsable', 'nombre correo rol');
    res.json(parcelas);
  } catch (error) {
    next(error);
  }
};

// @desc    Obtener una parcela por ID
// @route   GET /api/parcelas/:id
const obtenerParcelaPorId = async (req, res, next) => {
  try {
    const parcela = await Parcela.findById(req.params.id).populate(
      'responsable',
      'nombre correo rol'
    );
    if (!parcela) {
      return res.status(404).json({ mensaje: 'Parcela no encontrada' });
    }
    res.json(parcela);
  } catch (error) {
    next(error);
  }
};

// @desc    Actualizar parcela
// @route   PUT /api/parcelas/:id
const actualizarParcela = async (req, res, next) => {
  try {
    const camposPermitidos = [
      'nombre',
      'ubicacion',
      'superficie',
      'cultivo',
      'humedad',
      'temperatura',
      'estado',
      'responsable'
    ];

    const parcela = await Parcela.findById(req.params.id);
    if (!parcela) {
      return res.status(404).json({ mensaje: 'Parcela no encontrada' });
    }

    camposPermitidos.forEach((campo) => {
      if (req.body[campo] !== undefined) {
        parcela[campo] = req.body[campo];
      }
    });

    const parcelaActualizada = await parcela.save();
    res.json(parcelaActualizada);
  } catch (error) {
    next(error);
  }
};

// @desc    Eliminar parcela
// @route   DELETE /api/parcelas/:id
const eliminarParcela = async (req, res, next) => {
  try {
    const parcela = await Parcela.findById(req.params.id);
    if (!parcela) {
      return res.status(404).json({ mensaje: 'Parcela no encontrada' });
    }
    await parcela.deleteOne();
    res.json({ mensaje: 'Parcela eliminada correctamente' });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  crearParcela,
  obtenerParcelas,
  obtenerParcelaPorId,
  actualizarParcela,
  eliminarParcela
};