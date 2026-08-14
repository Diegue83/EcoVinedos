const mongoose = require('mongoose');

/**
 * Modelo que representa una sección específica dentro de una cava.
 * Cada sección puede tener sus propias condiciones de temperatura y humedad,
 * así como un sensor vinculado.
 */
const seccionCavaSchema = new mongoose.Schema(
  {
    cava: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Cava',
      required: [true, 'La sección debe pertenecer a una cava']
    },
    nombre: {
      type: String,
      required: [true, 'El nombre de la sección es obligatorio'],
      trim: true
    },
    tipo: {
      type: String,
      enum: ['ROBLE', 'ACERO', 'PRIVADA'],
      default: 'ROBLE'
    },
    temperatura: {
      type: Number,
      default: 0
    },
    humedad: {
      type: Number,
      default: 0
    },
    capacidadBotellas: {
      type: Number,
      default: 0
    },
    botellasActuales: {
      type: Number,
      default: 0
    },
    sensorId: {
      type: String,
      default: null
    },
    estado: {
      type: String,
      enum: ['OPTIMO', 'REVISAR', 'CRITICO'],
      default: 'OPTIMO'
    },
    ultimaLectura: {
      type: Date,
      default: Date.now
    }
  },
  { versionKey: false, timestamps: true }
);

module.exports = mongoose.model('SeccionCava', seccionCavaSchema);
