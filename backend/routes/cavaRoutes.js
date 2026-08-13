const express = require('express');
const router = express.Router();
const {
  obtenerCavas,
  vincularSensor,
  actualizarBotellas
} = require('../controllers/cavaController');
const { protect } = require('../middleware/auth');

router.route('/cavas').get(obtenerCavas);
router.route('/cavas/:id/sensor').put(protect, vincularSensor);
router.route('/cavas/:id/botellas').put(protect, actualizarBotellas);

module.exports = router;
