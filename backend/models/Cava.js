const mongoose = require('mongoose');

const cavaSchema = new mongoose.Schema(
  {
    nombre: {
      type: String,
      required: [true, 'El nombre de la cava es obligatorio'],
      trim: true
    },
    tipo: {
      type: String,
      enum: ['ROBLE', 'ACERO', 'PRIVADA'],
      default: 'ROBLE'
    },
    temperatura: {
      type: Number,
      default: 18
    },
    humedad: {
      type: Number,
      default: 80
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

module.exports = mongoose.model('Cava', cavaSchema);
