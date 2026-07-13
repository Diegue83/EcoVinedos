const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const usuarioSchema = new mongoose.Schema(
  {
    nombre: {
      type: String,
      required: [true, 'El nombre es obligatorio'],
      trim: true
    },
    correo: {
      type: String,
      required: [true, 'El correo es obligatorio'],
      unique: true,
      lowercase: true,
      trim: true,
      match: [/^\S+@\S+\.\S+$/, 'Correo no válido']
    },
    contraseña: {
      type: String,
      required: [true, 'La contraseña es obligatoria'],
      minlength: 6,
      select: false // no se devuelve en las consultas por defecto
    },
    rol: {
      type: String,
      enum: ['administrador', 'trabajador'],
      default: 'trabajador'
    },
    telefono: {
      type: String,
      trim: true
    },
    fechaRegistro: {
      type: Date,
      default: Date.now
    }
  },
  { versionKey: false }
);

// Hashear la contraseña antes de guardar, solo si fue modificada
usuarioSchema.pre('save', async function (next) {
  if (!this.isModified('contraseña')) return next();
  const salt = await bcrypt.genSalt(10);
  this.contraseña = await bcrypt.hash(this.contraseña, salt);
  next();
});

// Método para comparar contraseña en el login
usuarioSchema.methods.compararContraseña = async function (contraseñaIngresada) {
  return await bcrypt.compare(contraseñaIngresada, this.contraseña);
};

module.exports = mongoose.model('Usuario', usuarioSchema);