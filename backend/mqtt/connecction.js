const mqtt = require("mqtt");
const Parcela = require("../models/Parcela");
const HistorialSensor = require("../models/HistorialSensor");

const BROKER = "mqtts://" + process.env.MOSQUITTO_BROKER_URL;

/**
 * Cliente MQTT configurado para conectarse al broker especificado.
 */
const client = mqtt.connect(BROKER, {
    username: process.env.MQTT_USR,
    password: process.env.MQTT_PASS,
    reconnectPeriod: 3000,
    clean: true
});

/**
 * Almacena el timestamp del último guardado de historial por parcela para controlar
 * la frecuencia de 15 minutos.
 * @type {Map<string, number>}
 */
const ultimosGuardados = new Map();

client.on("connect", () => {
    console.log("✅ Conectado al broker MQTT");
    client.subscribe("vinedo/parcela/+/stats");
    client.subscribe("vinedo/nodo/vincular");
    publicarListaParcelas();
});

/**
 * Manejador principal de mensajes MQTT.
 *
 * Procesa estadísticas de sensores (cada 15 min) y eventos de vinculación de nodos.
 */
client.on("message", async (topic, message) => {
    const payload = message.toString();

    // 1. Manejo de Estadísticas de Sensores
    if (topic.startsWith("vinedo/parcela/") && topic.endsWith("/stats")) {
        try {
            const parts = topic.split("/");
            const parcelaId = parts[2];
            const data = JSON.parse(payload);
            const sensores = data.sensores || {};

            const humAire = sensores.humedad_aire || 0;
            const tempAire = sensores.temperatura_aire || 0;
            const humSuelo = sensores.humedad_suelo || 0;

            // Actualizar valores actuales en la parcela
            await Parcela.findByIdAndUpdate(parcelaId, {
                humedad: humAire,
                temperatura: tempAire,
                humedadSuelo: humSuelo,
                ultimaConexion: new Date()
            });

            // Guardar en historial solo cada 15 minutos
            const ahora = Date.now();
            const ultimo = ultimosGuardados.get(parcelaId) || 0;
            const QUINCE_MINUTOS = 15 * 60 * 1000;

            if (ahora - ultimo >= QUINCE_MINUTOS) {
                await HistorialSensor.create({
                    parcela: parcelaId,
                    humedadAire: humAire,
                    temperaturaAire: tempAire,
                    humedadSuelo: humSuelo
                });
                ultimosGuardados.set(parcelaId, ahora);
                console.log(`📊 Historial guardado para parcela ${parcelaId}`);
            }
        } catch (err) {
            console.error("Error procesando stats MQTT:", err.message);
        }
    }

    // 2. Manejo de Vinculación de Nodos
    if (topic === "vinedo/nodo/vincular") {
        try {
            const { parcelaId, nodoId, accion } = JSON.parse(payload);
            // accion: "vincular" o "desvincular"

            if (accion === "vincular") {
                await Parcela.findByIdAndUpdate(parcelaId, { nodoVinculado: nodoId });
                console.log(`🔗 Nodo ${nodoId} vinculado a parcela ${parcelaId}`);
            } else if (accion === "desvincular") {
                await Parcela.findByIdAndUpdate(parcelaId, { nodoVinculado: null });
                console.log(`🔓 Nodo desvinculado de parcela ${parcelaId}`);
            }
            publicarListaParcelas();
        } catch (err) {
            console.error("Error en vinculación MQTT:", err.message);
        }
    }
});

client.on("reconnect", () => {
    console.log("Reconectando...");
});

client.on("offline", () => {
    console.log("Cliente MQTT offline");
});

client.on("error", (err) => {
    console.error("Error MQTT:", err.message);
});

/**
 * Mapea un objeto de parcela de Mongoose a un objeto plano para envío MQTT.
 *
 * @param {Object} parcela - Objeto parcela de la base de datos.
 * @returns {Object} Objeto parcela formateado.
 */
const mapParcela = (parcela) => ({
    id: String(parcela._id),
    nombreParcela: parcela.nombreParcela,
    variedad: parcela.variedad,
    areaM2: parcela.areaM2,
    umbralHumedad: parcela.umbralHumedad,
    umbralTemp: parcela.umbralTemp,
    indiceMaduracion: parcela.indiceMaduracion,
    fechaCosecha: parcela.fechaCosecha,
    activa: parcela.activa,
    humedad: parcela.humedad,
    temperatura: parcela.temperatura,
    humedadSuelo: parcela.humedadSuelo,
    brix: parcela.brix,
    ph: parcela.ph,
    acidez: parcela.acidez,
    phSuelo: parcela.phSuelo,
    nodoVinculado: parcela.nodoVinculado
});

/**
 * Publica la lista completa de parcelas en el tópico de lista MQTT.
 *
 * @returns {Promise<void>}
 */
async function publicarListaParcelas() {

    if (!client.connected) {
        console.log("El cliente MQTT no está conectado");
        return;
    }

    try {

        const parcelas = await Parcela.find().lean();

        const lista = parcelas.map(mapParcela);

        client.publish(
            "vinedo/parcelas/lista",
            JSON.stringify(lista),
            {
                qos: 1,
                retain: true
            },
            (err) => {
                if (err) {
                    console.error("Error publicando parcelas:", err);
                } else {
                    console.log(`📤 Se publicaron ${lista.length} parcelas`);
                }
            }
        );

    } catch (err) {
        console.error("Error obteniendo parcelas:", err);
    }
}

module.exports = {
    client,
    publicarListaParcelas
};
