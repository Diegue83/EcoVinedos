const mongoose = require('mongoose');

/**
 * Modelo que representa una Cava (Bodega) en el sistema.
 * Una cava es una entidad de alto nivel que contiene múltiples secciones.
 */
const cavaSchema = new mongoose.Schema(
  {
    nombre: {
      type: String,
      required: [true, 'El nombre de la cava es obligatorio'],
      unique: true,
      trim: true
    },
    ubicacion: {
      type: String,
      required: [true, 'La ubicación es obligatoria'],
      trim: true
    },
    descripcion: {
      type: String,
      trim: true
    }
  },
  { versionKey: false, timestamps: true }
);

module.exports = mongoose.model('Cava', cavaSchema);
