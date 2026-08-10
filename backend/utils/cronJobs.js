const HistorialSensor = require('../models/HistorialSensor');
const ResumenDiario = require('../models/ResumenDiario');
const Parcela = require('../models/Parcela');

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
                    humedadSuelo: lecturas.reduce((acc, l) => acc + l.humedadSuelo, 0) / lecturas.length
                };

                await ResumenDiario.findOneAndUpdate(
                    { parcela: parcela._id, fecha: ayer },
                    {
                        humedadAirePromedio: promedio.humedadAire,
                        temperaturaAirePromedio: promedio.temperaturaAire,
                        humedadSueloPromedio: promedio.humedadSuelo
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

// Ejecutar cada 24 horas
function iniciarTareasProgramadas() {
    // Primera ejecución en 1 minuto para pruebas (opcional)
    setTimeout(generarResumenesDiarios, 60000);

    // Intervalo de 24 horas
    setInterval(generarResumenesDiarios, 24 * 60 * 60 * 1000);
}

module.exports = { iniciarTareasProgramadas };
