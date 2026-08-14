const HistorialSensor = require('../models/HistorialSensor');
const ResumenDiario = require('../models/ResumenDiario');
const Parcela = require('../models/Parcela');
const Notificacion = require('../models/Notificacion');

/**
 * Genera promedios diarios de las lecturas de los sensores para cada parcela.
 *
 * Consulta todas las lecturas del día anterior en el historial detallado,
 * calcula el promedio de humedad y temperatura, y guarda un resumen en la
 * colección ResumenDiario para análisis a largo plazo.
 *
 * @returns {Promise<void>}
 */
async function generarResumenesDiarios() {
    console.log("🕒 Iniciando generación de resúmenes diarios...");
    try {
        const ayer = new Date();
        ayer.setDate(ayer.getDate() - 1);
        ayer.setHours(0, 0, 0, 0);

        const hoy = new Date();
        hoy.setHours(0, 0, 0, 0);

        const parcelas = await Parcela.find();

        for (const parcela of parcelas) {
            const lecturas = await HistorialSensor.find({
                parcela: parcela._id,
                fecha: { $gte: ayer, $lt: hoy }
            });

            if (lecturas.length > 0) {
                const promedio = {
                    humedadAire: lecturas.reduce((acc, l) => acc + l.humedadAire, 0) / lecturas.length,
                    temperaturaAire: lecturas.reduce((acc, l) => acc + l.temperaturaAire, 0) / lecturas.length,
                    humedadSuelo: lecturas.reduce((acc, l) => acc + l.humedadSuelo, 0) / lecturas.length,
                    consumoAgua: lecturas.reduce((acc, l) => acc + (l.consumoAgua || 0), 0)
                };

                await ResumenDiario.findOneAndUpdate(
                    { parcela: parcela._id, fecha: ayer },
                    {
                        humedadAirePromedio: promedio.humedadAire,
                        temperaturaAirePromedio: promedio.temperaturaAire,
                        humedadSueloPromedio: promedio.humedadSuelo,
                        consumoAguaTotal: promedio.consumoAgua
                    },
                    { upsert: true }
                );
            }
        }
        console.log("✅ Resúmenes diarios generados con éxito");
    } catch (error) {
        console.error("❌ Error generando resúmenes:", error.message);
    }
}

/**
 * Elimina las notificaciones marcadas como 'descartada' que tengan más de una semana.
 */
async function limpiarNotificacionesDescartadas() {
    console.log("🕒 Iniciando limpieza de notificaciones descartadas...");
    try {
        const unaSemanaAtras = new Date();
        unaSemanaAtras.setDate(unaSemanaAtras.getDate() - 7);

        const resultado = await Notificacion.deleteMany({
            estado: 'descartada',
            updatedAt: { $lt: unaSemanaAtras }
        });

        console.log(`✅ Se eliminaron ${resultado.deletedCount} notificaciones descartadas antiguas`);
    } catch (error) {
        console.error("❌ Error limpiando notificaciones:", error.message);
    }
}

/**
 * Inicia el temporizador para la ejecución de tareas programadas (Cron).
 */
function iniciarTareasProgramadas() {
    // Primera ejecución en 1 minuto para pruebas tras reinicio del servidor
    setTimeout(() => {
        generarResumenesDiarios();
        limpiarNotificacionesDescartadas();
    }, 60000);

    // Intervalo de 24 horas
    setInterval(generarResumenesDiarios, 24 * 60 * 60 * 1000);
    setInterval(limpiarNotificacionesDescartadas, 24 * 60 * 60 * 1000);
}

module.exports = { iniciarTareasProgramadas };
