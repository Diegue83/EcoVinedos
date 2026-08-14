const Notificacion = require('../models/Notificacion');
const Usuario = require('../models/Usuario');

// @desc    Obtener notificaciones del usuario autenticado
// @route   GET /api/notificaciones
const obtenerMisNotificaciones = async (req, res, next) => {
    try {
        const notificaciones = await Notificacion.find({
            usuario: req.usuario._id,
            estado: { $ne: 'descartada' }
        }).sort({ fecha: -1 });

        res.json(notificaciones);
    } catch (error) {
        next(error);
    }
};

// @desc    Cambiar estado de notificación (leida, no leida, descartada)
// @route   PUT /api/notificaciones/:id/estado
const cambiarEstado = async (req, res, next) => {
    try {
        const { estado } = req.body;
        const notificacion = await Notificacion.findOneAndUpdate(
            { _id: req.params.id, usuario: req.usuario._id },
            { estado },
            { new: true }
        );

        if (!notificacion) return res.status(404).json({ mensaje: "Notificación no encontrada" });
        res.json(notificacion);
    } catch (error) {
        next(error);
    }
};

// @desc    Crear notificación (Uso interno o sistema)
const crearNotificacion = async (usuarioId, tipo, titulo, mensaje, parcelaId = null) => {
    try {
        const notificacion = await Notificacion.create({
            usuario: usuarioId,
            tipo,
            titulo,
            mensaje,
            parcela: parcelaId
        });

        return notificacion;
    } catch (error) {
        console.error("Error creando notificación:", error.message);
    }
};

module.exports = {
    obtenerMisNotificaciones,
    cambiarEstado,
    crearNotificacion
};
