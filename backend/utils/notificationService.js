const Parcela = require('../models/Parcela');
const Notificacion = require('../models/Notificacion');

/**
 * Analiza el estado actual de todas las parcelas activas para generar alertas automáticas.
 *
 * Verifica tres condiciones críticas:
 * 1. Humedad por debajo del umbral configurado.
 * 2. Desconexión de nodos IoT (sin recibir datos por más de 30 minutos).
 * 3. Proximidad de la fecha de cosecha (menos de 7 días).
 *
 * @returns {Promise<void>}
 */
async function verificarAlertas() {
    try {
        const parcelas = await Parcela.find({ activa: true });
        const ahora = new Date();

        for (const parcela of parcelas) {
            // 1. Alertas de Humedad
            if (parcela.nodoVinculado && parcela.humedad < parcela.umbralHumedad) {
                const existe = await Notificacion.findOne({
                    parcela: parcela._id,
                    tipo: 'humedad',
                    leida: false,
                    fecha: { $gte: new Date(ahora.getTime() - 4 * 60 * 60 * 1000) } // Evitar spam cada 4h
                });

                if (!existe) {
                    await Notificacion.create({
                        tipo: 'humedad',
                        titulo: `Alerta de Humedad: ${parcela.nombreParcela}`,
                        mensaje: `La humedad actual (${parcela.humedad}%) es menor al umbral configurado (${parcela.umbralHumedad}%).`,
                        parcela: parcela._id
                    });
                }
            }

            // 2. Alertas de Desconexión (más de 30 min sin datos)
            if (parcela.nodoVinculado) {
                const diffMinutos = (ahora - parcela.ultimaConexion) / (1000 * 60);
                if (diffMinutos > 30) {
                    const existe = await Notificacion.findOne({
                        parcela: parcela._id,
                        tipo: 'desconexion',
                        leida: false,
                        fecha: { $gte: new Date(ahora.getTime() - 12 * 60 * 60 * 1000) } // Evitar spam cada 12h
                    });

                    if (!existe) {
                        const tiempoStr = diffMinutos > 60
                            ? `${Math.round(diffMinutos / 60)} horas`
                            : `${Math.round(diffMinutos)} minutos`;

                        await Notificacion.create({
                            tipo: 'desconexion',
                            titulo: `Nodo Desconectado: ${parcela.nombreParcela}`,
                            mensaje: `El nodo IoT lleva ${tiempoStr} sin enviar información.`,
                            parcela: parcela._id
                        });
                    }
                }
            }

            // 3. Alertas de Cosecha Próxima (menos de 7 días)
            if (parcela.fechaCosecha) {
                const diffDias = (parcela.fechaCosecha - ahora) / (1000 * 60 * 60 * 24);
                if (diffDias > 0 && diffDias <= 7) {
                    const existe = await Notificacion.findOne({
                        parcela: parcela._id,
                        tipo: 'cosecha',
                        leida: false,
                        fecha: { $gte: new Date(ahora.getTime() - 24 * 60 * 60 * 1000) }
                    });

                    if (!existe) {
                        await Notificacion.create({
                            tipo: 'cosecha',
                            titulo: `Cosecha Próxima: ${parcela.nombreParcela}`,
                            mensaje: `La fecha estimada de cosecha es el ${parcela.fechaCosecha.toLocaleDateString()}. Faltan ${Math.ceil(diffDias)} días.`,
                            parcela: parcela._id
                        });
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
