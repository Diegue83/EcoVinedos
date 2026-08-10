const mongoose = require("mongoose");

const notificacionSchema = new mongoose.Schema({
    tipo: {
        type: String,
        enum: ['cosecha', 'humedad', 'desconexion', 'sistema'],
        required: true
    },
    titulo: { type: String, required: true },
    mensaje: { type: String, required: true },
    parcela: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Parcela',
        required: false
    },
    leida: { type: Boolean, default: false },
    fecha: { type: Date, default: Date.now }
}, {
    versionKey: false,
    timestamps: true
});

// Auto-delete notifications after 30 days
notificacionSchema.index({ createdAt: 1 }, { expireAfterSeconds: 30 * 24 * 60 * 60 });

module.exports = mongoose.model("Notificacion", notificacionSchema);
