const express = require('express');
const router = express.Router();
const { getPairingCode, checkStatus, linkTV } = require('../controllers/tvController');
const { protegerRuta } = require('../middleware/authMiddleware');

// Public routes for TV
router.post('/pair-code', getPairingCode);
router.get('/status/:deviceId', checkStatus);

// Protected route for Mobile App
router.post('/link', protegerRuta, linkTV);

module.exports = router;
