/**
 * Configuración de Multer con Cloudinary para la gestión de carga de archivos en la nube.
 *
 * Utiliza Cloudinary como motor de almacenamiento remoto, lo que permite que las
 * imágenes sean accesibles mediante una URL segura (HTTPS) y persistente.
 */
const multer = require('multer');
const cloudinary = require('cloudinary').v2;
const { CloudinaryStorage } = require('multer-storage-cloudinary');

// Configuración de las credenciales de Cloudinary desde variables de entorno
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET
});

// Configuración del motor de almacenamiento en Cloudinary
const storage = new CloudinaryStorage({
  cloudinary: cloudinary,
  params: {
    folder: 'ecovinedos_assets',
    allowed_formats: ['jpg', 'png', 'jpeg', 'webp'],
    transformation: [{ width: 1000, height: 1000, crop: 'limit' }]
  },
});

/**
 * Middleware de Multer configurado con almacenamiento en la nube.
 */
const upload = multer({
  storage: storage,
  limits: { fileSize: 5 * 1024 * 1024 } // Límite de 5MB
});

module.exports = upload;


