const Parcela = require('../models/Parcela');
const { publicarListaParcelas } = require('../mqtt/connecction');

// @desc    Crear parcela
// @route   POST /api/parcelas
const crearParcela = async (req, res, next) => {
  try {
    const { nombreParcela, variedad, areaM2, umbralHumedad, umbralTemp, indiceMaduracion, fechaCosecha, activa } =
      req.body;

    console.log(req.body)

    const parcela = await Parcela.create({
      nombreParcela,
      variedad,
      areaM2,
      umbralHumedad,
      umbralTemp,
      indiceMaduracion,
      fechaCosecha,
      activa,
    });

    await publicarListaParcelas(); // Llamada a la función para enviar la parcela al broker MQTT

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
      'nombreParcela',
      'variedad',
      'areaM2',
      'umbralHumedad',
      'umbralTemp',
      'indiceMaduracion',
      'fechaCosecha',
      'activa',
      'humedad',
      'temperatura',
      'humedadSuelo',
      'brix',
      'ph',
      'acidez',
      'phSuelo',
      'estado',
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
    await publicarListaParcelas(); // Llamada a la función para enviar la lista de parcelas actualizada al broker MQTT
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
    await publicarListaParcelas(); // Llamada a la función para enviar la lista de parcelas actualizada al broker MQTT
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