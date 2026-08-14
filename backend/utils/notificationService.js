const Parcela = require('../models/Parcela');
const Notificacion = require('../models/Notificacion');
const Usuario = require('../models/Usuario');
const { crearNotificacion } = require('../controllers/notificacionController');

/**
 * Analiza el estado actual de todas las parcelas activas para generar alertas automáticas.
 */
async function verificarAlertas() {
    try {
        const parcelas = await Parcela.find({ activa: true });
        const ahora = new Date();
        const usuarios = await Usuario.find({ rol: { $in: ['administrador', 'trabajador', 'superusuario'] } });

        for (const parcela of parcelas) {
            // 1. Alertas de Humedad
            if (parcela.nodoVinculado && parcela.humedad < parcela.umbralHumedad) {
                for (const usuario of usuarios) {
                    const existe = await Notificacion.findOne({
                        usuario: usuario._id,
                        parcela: parcela._id,
                        tipo: 'humedad',
                        estado: { $ne: 'descartada' },
                        fecha: { $gte: new Date(ahora.getTime() - 4 * 60 * 60 * 1000) }
                    });

                    if (!existe) {
                        await crearNotificacion(
                            usuario._id,
                            'humedad',
                            `Alerta de Humedad: ${parcela.nombreParcela}`,
                            `La humedad actual (${parcela.humedad}%) es menor al umbral configurado (${parcela.umbralHumedad}%).`,
                            parcela._id
                        );
                    }
                }
            }

            // 2. Alertas de Desconexión
            if (parcela.nodoVinculado) {
                const diffMinutos = (ahora - parcela.ultimaConexion) / (1000 * 60);
                if (diffMinutos > 30) {
                    for (const usuario of usuarios) {
                        const existe = await Notificacion.findOne({
                            usuario: usuario._id,
                            parcela: parcela._id,
                            tipo: 'desconexion',
                            estado: { $ne: 'descartada' },
                            fecha: { $gte: new Date(ahora.getTime() - 12 * 60 * 60 * 1000) }
                        });

                        if (!existe) {
                            const tiempoStr = diffMinutos > 60
                                ? `${Math.round(diffMinutos / 60)} horas`
                                : `${Math.round(diffMinutos)} minutos`;

                            await crearNotificacion(
                                usuario._id,
                                'desconexion',
                                `Nodo Desconectado: ${parcela.nombreParcela}`,
                                `El nodo IoT lleva ${tiempoStr} sin enviar información.`,
                                parcela._id
                            );
                        }
                    }
                }
            }

            // 3. Alertas de Cosecha Próxima
            if (parcela.fechaCosecha) {
                const diffDias = (parcela.fechaCosecha - ahora) / (1000 * 60 * 60 * 24);
                if (diffDias > 0 && diffDias <= 7) {
                    for (const usuario of usuarios) {
                        const existe = await Notificacion.findOne({
                            usuario: usuario._id,
                            parcela: parcela._id,
                            tipo: 'cosecha',
                            estado: { $ne: 'descartada' },
                            fecha: { $gte: new Date(ahora.getTime() - 24 * 60 * 60 * 1000) }
                        });

                        if (!existe) {
                            await crearNotificacion(
                                usuario._id,
                                'cosecha',
                                `Cosecha Próxima: ${parcela.nombreParcela}`,
                                `La fecha estimada de cosecha es el ${parcela.fechaCosecha.toLocaleDateString()}. Faltan ${Math.ceil(diffDias)} días.`,
                                parcela._id
                            );
                        }
                    }
                }
            }
        }
    } catch (error) {
        console.error("Error verificando alertas:", error.message);
    }
}

/**
 * Inicia el servicio de verificación periódica de alertas del sistema.
 *
 * Configura un intervalo de 10 minutos para la monitorización automática.
 */
function iniciarVerificacionAlertas() {
    // Verificar cada 10 minutos
    setInterval(verificarAlertas, 10 * 60 * 1000);
    // Ejecución inicial tras arranque del servidor
    verificarAlertas();
}

module.exports = { iniciarVerificacionAlertas };
