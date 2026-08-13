const mongoose = require("mongoose");

const resumenDiarioSchema = new mongoose.Schema({
    parcela: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Parcela',
        required: true
    },
    humedadAirePromedio: { type: Number, required: true },
    temperaturaAirePromedio: { type: Number, required: true },
    humedadSueloPromedio: { type: Number, required: true },
    consumoAguaTotal: { type: Number, default: 0 },
    fecha: { type: Date, required: true }
}, {
    versionKey: false,
    timestamps: false
});

// TTL index to remove after 1 year
resumenDiarioSchema.index({ fecha: 1 }, { expireAfterSeconds: 365 * 24 * 60 * 60 });

module.exports = mongoose.model("ResumenDiario", resumenDiarioSchema);
