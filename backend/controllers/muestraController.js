const Muestra = require('../models/Muestra');
const Parcela = require('../models/Parcela');

// @desc    Registrar una nueva muestra de campo
// @route   POST /api/muestras
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

        res.status(201).json(muestra);
    } catch (error) {
        next(error);
    }
};

// @desc    Obtener historial de muestras de una parcela
// @route   GET /api/muestras/parcela/:parcelaId
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
