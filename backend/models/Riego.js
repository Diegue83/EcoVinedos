const mongoose = require('mongoose');

const riegoSchema = new mongoose.Schema(
  {
    parcela: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Parcela',
      required: [true, 'El riego debe estar asociado a una parcela']
    },
    fecha: {
      type: Date,
      default: Date.now
    },
    duracion: {
      type: Number, // minutos
      required: [true, 'La duración del riego es obligatoria'],
      min: [0, 'La duración no puede ser negativa']
    },
    litros: {
      type: Number,
      required: [true, 'La cantidad de litros es obligatoria'],
      min: [0, 'Los litros no pueden ser negativos']
    },
    estado: {
      type: String,
      enum: ['programado', 'en curso', 'completado', 'cancelado'],
      default: 'programado'
    }
  },
  { versionKey: false }
);

module.exports = mongoose.model('Riego', riegoSchema);