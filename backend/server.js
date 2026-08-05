require('dotenv').config();
const express = require('express');
const cors = require('cors');
const connectDB = require('./config/database');
const errorHandler = require('./middleware/errorHandler');
const usuarioRoutes = require('./routes/usuarioRoutes');
const parcelaRoutes = require('./routes/parcelaRoutes');
const bitacoraRoutes = require('./routes/bitacoraRoutes');
const riegoRoutes = require('./routes/riegoRoutes');
const morgan = require('morgan');

connectDB();

const app = express();

app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

// Rutas
app.use('/api', usuarioRoutes);
app.use('/api', parcelaRoutes);
app.use('/api', bitacoraRoutes);
app.use('/api', riegoRoutes);

app.get('/', (req, res) => {
  res.send('API de administración y riego de parcelas funcionando 🌱');
});

// Middleware de errores (siempre al final)
app.use(errorHandler);

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => console.log(`Servidor corriendo en el puerto ${PORT}`));