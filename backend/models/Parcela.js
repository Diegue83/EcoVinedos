const mongoose = require("mongoose");

const parcelaSchema = new mongoose.Schema(
{
    nombreParcela: {
        type: String,
        required: [true, "El nombre de la parcela es obligatorio"],
        trim: true
    },

    variedad: {
        type: String,
        required: [true, "La variedad es obligatoria"],
        trim: true
    },

    areaM2: {
        type: Number,
        required: [true, "El área es obligatoria"],
        min: [0, "El área no puede ser negativa"]
    },

    umbralHumedad: {
        type: Number,
        required: true,
        min: 0,
        max: 100,
        default: 60
    },

    umbralTemp: {
        type: Number,
        required: true,
        default: 30
    },

    indiceMaduracion: {
        type: Number,
        required: true,
        min: 0,
        max: 100,
        default: 0
    },

    fechaCosecha: {
        type: Date
    },

    activa: {
        type: Boolean,
        default: true
    },

    humedad: {
        type: Number,
        min: 0,
        max: 100,
        default: 0
    },

    temperatura: {
        type: Number,
        default: 0
    },

    humedadSuelo: {
        type: Number,
        default: 0
    },

    brix: {
        type: Number,
        default: 0
    },

    ph: {
        type: Number,
        default: 0
    },

    acidez: {
        type: Number,
        default: 0
    },

    phSuelo: {
        type: Number,
        default: 0
    },
},
{
    versionKey: false,
    timestamps: true
});

module.exports = mongoose.model("Parcela", parcelaSchema);