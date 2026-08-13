const mongoose = require('mongoose');

const eventoSchema = new mongoose.Schema(
  {
    titulo: {
      type: String,
      required: [true, 'El título es obligatorio'],
      trim: true
    },
    descripcion: {
      type: String,
      required: [true, 'La descripción es obligatoria']
    },
    fecha: {
      type: Date,
      default: Date.now
    },
    imagenUrl: {
      type: String,
      trim: true
    },
    tipo: {
      type: String,
      enum: ['EVENT', 'TOURISM', 'NOTICE'],
      default: 'EVENT'
    },
    cupo: {
      type: Number,
      default: 0
    },
    ubicacion: {
      type: String,
      trim: true
    },
    organizador: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Usuario'
    }
  },
  { versionKey: false, timestamps: true }
);

module.exports = mongoose.model('Evento', eventoSchema);
