const mongoose = require('mongoose');

const tvSessionSchema = new mongoose.Schema(
  {
    deviceId: {
      type: String,
      required: true,
      unique: true
    },
    pairingCode: {
      type: String,
      required: true,
      unique: true
    },
    isLinked: {
      type: Boolean,
      default: false
    },
    linkedBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Usuario'
    },
    expiresAt: {
      type: Date,
      required: true
    }
  },
  { versionKey: false, timestamps: true }
);

// TTL index to automatically remove expired sessions
tvSessionSchema.index({ expiresAt: 1 }, { expireAfterSeconds: 0 });

module.exports = mongoose.model('TVSession', tvSessionSchema);
