const express = require('express');
const router = express.Router();
const {
  obtenerCavas,
  vincularSensor,
  actualizarBotellas
} = require('../controllers/cavaController');
const { protegerRuta } = require('../middleware/authMiddleware');

router.get('/cavas', obtenerCavas);
router.put('/cavas/:id/sensor', protegerRuta, vincularSensor);
router.put('/cavas/:id/botellas', protegerRuta, actualizarBotellas);

module.exports = router;
