const HistorialSensor = require('../models/HistorialSensor');
const ResumenDiario = require('../models/ResumenDiario');

/**
 * Obtiene el historial de lecturas de sensores (cada 15 min) de una parcela.
 *
 * @param {Object} req - Solicitud HTTP. Requiere parcelaId en params y opcionalmente limit en query.
 * @param {Object} res - Respuesta HTTP.
 * @param {Function} next - Middleware de error.
 * @returns {Promise<void>}
 */
const obtenerHistorialParcela = async (req, res, next) => {
    try {
        const { parcelaId } = req.params;
        const { limit = 100 } = req.query;

        const historial = await HistorialSensor.find({ parcela: parcelaId })
            .sort({ fecha: -1 })
            .limit(parseInt(limit));

        res.json(historial);
    } catch (error) {
        next(error);
    }
};

/**
 * Obtiene el resumen de promedios diarios de una parcela.
 *
 * @param {Object} req - Solicitud HTTP. Requiere parcelaId en params.
 * @param {Object} res - Respuesta HTTP.
 * @param {Function} next - Middleware de error.
 * @returns {Promise<void>}
 */
const obtenerResumenParcela = async (req, res, next) => {
    try {
        const { parcelaId } = req.params;

        const resumen = await ResumenDiario.find({ parcela: parcelaId })
            .sort({ fecha: -1 });

        res.json(resumen);
    } catch (error) {
        next(error);
    }
};

module.exports = {
    obtenerHistorialParcela,
    obtenerResumenParcela
};
