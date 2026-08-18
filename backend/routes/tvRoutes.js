const express = require('express');
const router = express.Router();
const { getPairingCode, checkStatus, linkTV, unlinkTV } = require('../controllers/tvController');
const { protegerRuta } = require('../middleware/authMiddleware');

// Public routes for TV
router.post('/pair-code', getPairingCode);
router.get('/status/:deviceId', checkStatus);
router.post('/unlink', unlinkTV);

// Protected route for Mobile App
router.post('/link', protegerRuta, linkTV);

module.exports = router;
