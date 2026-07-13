const mongoose = require('mongoose');

const parcelaSchema = new mongoose.Schema(
  {
    nombre: {
      type: String,
      required: [true, 'El nombre de la parcela es obligatorio'],
      trim: true
    },
    ubicacion: {
      type: String,
      required: [true, 'La ubicación es obligatoria'],
      trim: true
    },
    superficie: {
      type: Number,
      required: [true, 'La superficie es obligatoria'],
      min: [0, 'La superficie no puede ser negativa']
    },
    cultivo: {
      type: String,
      trim: true
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
    estado: {
      type: String,
      enum: ['activa', 'inactiva', 'mantenimiento'],
      default: 'activa'
    },
    umbralHumedad: {
      type: Number,
      min: 0,
      max: 100,
      default: 30
    },
    umbralTemp: {
      type: Number,
      default: 25
    },
    indiceMaduracion: {
      type: Number,
      min: 0,
      max: 1,
      default: 0
    },
    fechaCosecha: {
      type: Date
    },
    responsable: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Usuario',
      required: [true, 'La parcela debe tener un responsable asignado']
    },
    fechaRegistro: {
      type: Date,
      default: Date.now
    }
  },
  { versionKey: false }
);

module.exports = mongoose.model('Parcela', parcelaSchema);