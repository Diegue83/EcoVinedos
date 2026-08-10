const express = require('express');
const router = express.Router();
const Notificacion = require('../models/Notificacion');

// Obtener todas las notificaciones
router.get('/notificaciones', async (req, res, next) => {
    try {
        const notificaciones = await Notificacion.find()
            .sort({ fecha: -1 })
            .limit(50);
        res.json(notificaciones);
    } catch (error) {
        next(error);
    }
});

// Marcar como leída
router.put('/notificaciones/:id/leer', async (req, res, next) => {
    try {
        const notificacion = await Notificacion.findByIdAndUpdate(
            req.params.id,
            { leida: true },
            { new: true }
        );
        res.json(notificacion);
    } catch (error) {
        next(error);
    }
});

// Eliminar todas las leídas
router.delete('/notificaciones/limpiar', async (req, res, next) => {
    try {
        await Notificacion.deleteMany({ leida: true });
        res.json({ mensaje: "Notificaciones limpiadas" });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
