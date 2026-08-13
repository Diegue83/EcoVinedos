const mongoose = require("mongoose");

const historialSensorSchema = new mongoose.Schema({
    parcela: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Parcela',
        required: true
    },
    humedadAire: { type: Number, required: true },
    temperaturaAire: { type: Number, required: true },
    humedadSuelo: { type: Number, required: true },
    consumoAgua: { type: Number, default: 0 },
    fecha: { type: Date, default: Date.now }
}, {
    versionKey: false,
    timestamps: false
});

// TTL index to automatically remove documents after 3 months (approx 90 days)
// 90 days * 24 hours * 60 minutes * 60 seconds
historialSensorSchema.index({ fecha: 1 }, { expireAfterSeconds: 90 * 24 * 60 * 60 });

module.exports = mongoose.model("HistorialSensor", historialSensorSchema);
