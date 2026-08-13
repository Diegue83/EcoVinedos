const TVSession = require('../models/TVSession');

// Generate a random 6-character alphanumeric code
const generateCode = () => {
  return Math.random().toString(36).substring(2, 8).toUpperCase();
};

// @desc    Get or create pairing code for TV
// @route   POST /api/tv/pair-code
const getPairingCode = async (req, res, next) => {
  try {
    const { deviceId } = req.body;
    if (!deviceId) return res.status(400).json({ mensaje: 'deviceId es requerido' });

    let session = await TVSession.findOne({ deviceId });

    // Si la sesión existe y aún es válida (no ha expirado), devolver la actual
    if (session && session.expiresAt > Date.now()) {
      return res.json(session);
    }

    // Solo si no existe o ya expiró, crear/renovar el código
    const pairingCode = generateCode();
    const expiresAt = new Date(Date.now() + 15 * 60 * 1000); // 15 mins

    if (session) {
      session.pairingCode = pairingCode;
      session.expiresAt = expiresAt;
      await session.save();
    } else {
      session = await TVSession.create({
        deviceId,
        pairingCode,
        expiresAt
      });
    }

    res.json(session);
  } catch (error) {
    next(error);
  }
};

// @desc    Check TV status
// @route   GET /api/tv/status/:deviceId
const checkStatus = async (req, res, next) => {
  try {
    const session = await TVSession.findOne({ deviceId: req.params.deviceId });
    if (!session) return res.status(404).json({ mensaje: 'Sesión no encontrada o expirada' });
    res.json(session);
  } catch (error) {
    next(error);
  }
};

// @desc    Link TV code (From Mobile)
// @route   POST /api/tv/link
const linkTV = async (req, res, next) => {
  try {
    const { pairingCode } = req.body;
    const userId = req.usuario._id;

    const session = await TVSession.findOne({ pairingCode, isLinked: false });

    if (!session) {
      return res.status(404).json({ mensaje: 'Código inválido o ya vinculado' });
    }

    session.isLinked = true;
    session.linkedBy = userId;
    // Extend expiration or remove it for linked sessions
    session.expiresAt = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000); // 1 year
    await session.save();

    res.json({ mensaje: 'TV vinculada correctamente', deviceId: session.deviceId });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getPairingCode,
  checkStatus,
  linkTV
};
