const mongoose = require("mongoose");

const muestraSchema = new mongoose.Schema({
    parcela: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Parcela',
        required: true
    },
    brix: { type: Number, required: true },
    ph: { type: Number, required: true },
    acidez: { type: Number, required: true },
    phSuelo: { type: Number, required: true },
    observaciones: { type: String, default: "" },
    fecha: { type: Date, default: Date.now }
}, {
    versionKey: false,
    timestamps: true
});

module.exports = mongoose.model("Muestra", muestraSchema);
