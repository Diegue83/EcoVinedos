const Cava = require('../models/Cava');
const SeccionCava = require('../models/SeccionCava');
const { publicarListaParcelas } = require('../mqtt/connecction');

// --- Controladores de Cavas (Entidad Principal) ---

// @desc    Obtener todas las cavas con sus secciones
// @route   GET /api/cavas
const obtenerCavas = async (req, res, next) => {
  try {
    // Buscamos todas las cavas
    const cavas = await Cava.find().lean();

    // Para cada cava, buscamos sus secciones
    const cavasConSecciones = await Promise.all(cavas.map(async (cava) => {
      const secciones = await SeccionCava.find({ cava: cava._id });
      return { ...cava, secciones };
    }));

    res.json(cavasConSecciones);
  } catch (error) {
    next(error);
  }
};

// @desc    Crear una nueva cava
// @route   POST /api/cavas
const crearCava = async (req, res, next) => {
  try {
    const cava = await Cava.create(req.body);
    publicarListaParcelas();
    res.status(201).json(cava);
  } catch (error) {
    next(error);
  }
};

// @desc    Eliminar una cava y sus secciones
// @route   DELETE /api/cavas/:id
const eliminarCava = async (req, res, next) => {
  try {
    await SeccionCava.deleteMany({ cava: req.params.id });
    const cava = await Cava.findByIdAndDelete(req.params.id);
    if (!cava) return res.status(404).json({ mensaje: 'Cava no encontrada' });
    publicarListaParcelas();
    res.json({ mensaje: 'Cava y sus secciones eliminadas correctamente' });
  } catch (error) {
    next(error);
  }
};

// --- Controladores de Secciones ---

// @desc    Crear una nueva sección en una cava
// @route   POST /api/cavas/secciones
const crearSeccion = async (req, res, next) => {
  try {
    const seccion = await SeccionCava.create(req.body);
    publicarListaParcelas();
    res.status(201).json(seccion);
  } catch (error) {
    next(error);
  }
};

// @desc    Actualizar una sección (incluyendo vinculación de sensor o botellas)
// @route   PUT /api/cavas/secciones/:id
const actualizarSeccion = async (req, res, next) => {
  try {
    const { botellasActuales, nombre, tipo, capacidadBotellas, cava } = req.body;

    // Si solo viene botellasActuales, permitimos la actualización parcial
    // Pero si el usuario mandó todo el objeto, lo usamos
    const updateData = {};
    if (botellasActuales !== undefined) updateData.botellasActuales = Number(botellasActuales);
    if (nombre) updateData.nombre = nombre;
    if (tipo) updateData.tipo = tipo;
    if (capacidadBotellas !== undefined) updateData.capacidadBotellas = Number(capacidadBotellas);
    if (cava) updateData.cava = cava;

    console.log(`📝 Actualizando sección ${req.params.id}:`, updateData);

    const seccion = await SeccionCava.findByIdAndUpdate(
      req.params.id,
      updateData,
      { new: true, runValidators: true }
    );

    if (!seccion) return res.status(404).json({ mensaje: 'Sección no encontrada' });

    console.log(`✅ Sección actualizada: ${seccion.nombre}, Botellas: ${seccion.botellasActuales}`);

    // Disparar actualización MQTT para que Móvil/TV se enteren al instante
    publicarListaParcelas();

    res.json(seccion);
  } catch (error) {
    console.error("❌ Error actualizando sección:", error);
    next(error);
  }
};

// @desc    Eliminar una sección específica
// @route   DELETE /api/cavas/secciones/:id
const eliminarSeccion = async (req, res, next) => {
  try {
    const seccion = await SeccionCava.findByIdAndDelete(req.params.id);
    if (!seccion) return res.status(404).json({ mensaje: 'Sección no encontrada' });
    publicarListaParcelas();
    res.json({ mensaje: 'Sección eliminada correctamente' });
  } catch (error) {
    next(error);
  }
};

module.exports = {
  obtenerCavas,
  crearCava,
  eliminarCava,
  crearSeccion,
  actualizarSeccion,
  eliminarSeccion
};
