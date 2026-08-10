const Muestra = require('../models/Muestra');
const Parcela = require('../models/Parcela');
const { publicarListaParcelas } = require('../mqtt/connecction');

/**
 * Registra una nueva muestra de campo para una parcela.
 *
 * Crea un registro en el historial de muestras y actualiza los valores
 * actuales de la parcela (Brix, pH, Acidez, pH Suelo). También notifica
 * el cambio vía MQTT.
 *
 * @param {Object} req - Objeto de solicitud HTTP. Contiene parcelaId, brix, ph, acidez, phSuelo, observaciones.
 * @param {Object} res - Objeto de respuesta HTTP.
 * @param {Function} next - Función para pasar al siguiente middleware de error.
 * @returns {Promise<void>}
 */
const registrarMuestra = async (req, res, next) => {
    try {
        const { parcelaId, brix, ph, acidez, phSuelo, observaciones, fecha } = req.body;

        const parcela = await Parcela.findById(parcelaId);
        if (!parcela) {
            return res.status(404).json({ mensaje: 'Parcela no encontrada' });
        }

        const muestra = await Muestra.create({
            parcela: parcelaId,
            brix,
            ph,
            acidez,
            phSuelo,
            observaciones,
            fecha: fecha || Date.now()
        });

        // Actualizar los valores actuales en la parcela
        parcela.brix = brix;
        parcela.ph = ph;
        parcela.acidez = acidez;
        parcela.phSuelo = phSuelo;
        await parcela.save();

        // Notificar cambios vía MQTT para actualización en tiempo real
        await publicarListaParcelas();

        res.status(201).json(muestra);
    } catch (error) {
        next(error);
    }
};

/**
 * Obtiene el historial completo de muestras de una parcela específica.
 *
 * @param {Object} req - Objeto de solicitud HTTP. Requiere parcelaId en params.
 * @param {Object} res - Objeto de respuesta HTTP.
 * @param {Function} next - Función para pasar al siguiente middleware de error.
 * @returns {Promise<void>}
 */
const obtenerHistorialPorParcela = async (req, res, next) => {
    try {
        const muestras = await Muestra.find({ parcela: req.params.parcelaId })
            .sort({ fecha: -1 }); // De la más reciente a la más antigua
        res.json(muestras);
    } catch (error) {
        next(error);
    }
};

module.exports = {
    registrarMuestra,
    obtenerHistorialPorParcela
};
