const Parcela = require('../models/Parcela');
const { publicarListaParcelas } = require('../mqtt/connecction');

/**
 * Crea una nueva parcela en el sistema.
 *
 * Registra la información geográfica y técnica de la parcela y notifica
 * el cambio a los clientes conectados vía MQTT.
 *
 * @param {Object} req - Solicitud HTTP con los datos de la parcela.
 * @param {Object} res - Respuesta HTTP.
 * @param {Function} next - Middleware de error.
 * @returns {Promise<void>}
 */
const crearParcela = async (req, res, next) => {
  try {
    const { nombreParcela, variedad, areaM2, umbralHumedad, umbralTemp, indiceMaduracion, fechaCosecha, activa } =
      req.body;

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

    await publicarListaParcelas();

    res.status(201).json(parcela);
  } catch (error) {
    next(error);
  }
};

/**
 * Obtiene el listado de todas las parcelas registradas.
 *
 * @param {Object} req - Solicitud HTTP.
 * @param {Object} res - Respuesta HTTP con la lista de parcelas.
 * @param {Function} next - Middleware de error.
 * @returns {Promise<void>}
 */
const obtenerParcelas = async (req, res, next) => {
  try {
    const parcelas = await Parcela.find();
    res.json(parcelas);
  } catch (error) {
    next(error);
  }
};

/**
 * Obtiene los detalles de una parcela mediante su ID.
 *
 * @param {Object} req - Solicitud HTTP con el ID en params.
 * @param {Object} res - Respuesta HTTP.
 * @param {Function} next - Middleware de error.
 * @returns {Promise<void>}
 */
const obtenerParcelaPorId = async (req, res, next) => {
  try {
    const parcela = await Parcela.findById(req.params.id);
    if (!parcela) {
      return res.status(404).json({ mensaje: 'Parcela no encontrada' });
    }
    res.json(parcela);
  } catch (error) {
    next(error);
  }
};

/**
 * Actualiza la información de una parcela existente.
 *
 * Permite modificar umbrales, estados o metadatos técnicos. Al finalizar,
 * publica la lista actualizada por MQTT.
 *
 * @param {Object} req - Solicitud HTTP con los campos a actualizar.
 * @param {Object} res - Respuesta HTTP.
 * @param {Function} next - Middleware de error.
 * @returns {Promise<void>}
 */
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
    await publicarListaParcelas();
    res.json(parcelaActualizada);
  } catch (error) {
    next(error);
  }
};

/**
 * Elimina una parcela del sistema.
 *
 * @param {Object} req - Solicitud HTTP con el ID en params.
 * @param {Object} res - Respuesta HTTP.
 * @param {Function} next - Middleware de error.
 * @returns {Promise<void>}
 */
const eliminarParcela = async (req, res, next) => {
  try {
    const parcela = await Parcela.findById(req.params.id);
    if (!parcela) {
      return res.status(404).json({ mensaje: 'Parcela no encontrada' });
    }
    await parcela.deleteOne();
    await publicarListaParcelas();
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
