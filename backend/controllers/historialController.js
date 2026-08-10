const HistorialSensor = require('../models/HistorialSensor');
const ResumenDiario = require('../models/ResumenDiario');

// @desc    Obtener historial reciente (15 min) de una parcela
// @route   GET /api/historial/parcela/:parcelaId
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

// @desc    Obtener resumen diario (1 año) de una parcela
// @route   GET /api/historial/resumen/:parcelaId
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
