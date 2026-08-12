const nodemailer = require('nodemailer');

const enviarEmailRecuperacion = async (destinatario, codigo) => {
  try {
    // Configuración usando 'service: gmail' (Más compatible con Render)
    const transporter = nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
      },
      tls: {
        rejectUnauthorized: false
      }
    });

    const mailOptions = {
      from: `"EcoViñedos Soporte" <${process.env.EMAIL_USER}>`,
      to: destinatario,
      subject: 'Código de recuperación de contraseña - EcoViñedos',
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
          <h2 style="color: #2E7D32; text-align: center;">Recuperación de Contraseña</h2>
          <p>Hola,</p>
          <p>Has solicitado restablecer tu contraseña en <strong>EcoViñedos</strong>. Utiliza el siguiente código de verificación:</p>
          <div style="background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #3897F0; border-radius: 5px; margin: 20px 0;">
            ${codigo}
          </div>
          <p>Este código es válido por <strong>10 minutos</strong>. Si no solicitaste este cambio, puedes ignorar este correo de forma segura.</p>
          <hr style="border: 0; border-top: 1px solid #eeeeee; margin: 20px 0;">
          <p style="font-size: 12px; color: #888888; text-align: center;">
            Este es un mensaje automático, por favor no respondas a este correo.
          </p>
        </div>
      `
    };

    const info = await transporter.sendMail(mailOptions);
    console.log('Correo enviado: %s', info.messageId);
    return true;
  } catch (error) {
    console.error('Error enviando email:', error);
    return false;
  }
};

module.exports = {
  enviarEmailRecuperacion
};
