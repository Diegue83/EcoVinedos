const mongoose = require('mongoose');

const bitacoraSchema = new mongoose.Schema(
  {
    parcela: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Parcela',
      required: [true, 'La bitácora debe estar asociada a una parcela']
    },
    usuario: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Usuario',
      required: [true, 'La bitácora debe indicar quién realizó la acción']
    },
    accion: {
      type: String,
      required: [true, 'La acción es obligatoria'],
      trim: true
      // Ejemplos: 'riego', 'fertilizacion', 'poda', 'inspeccion', 'alerta'
    },
    descripcion: {
      type: String,
      trim: true
    },
    fecha: {
      type: Date,
      default: Date.now
    }
  },
  { versionKey: false }
);

module.exports = mongoose.model('Bitacora', bitacoraSchema);