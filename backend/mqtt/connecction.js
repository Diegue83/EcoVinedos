const mqtt = require("mqtt");
const Parcela = require("../models/Parcela");
const Cava = require("../models/Cava");
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
    client.subscribe("vinedo/parcela/+/riego");
    client.subscribe("vinedo/parcela/+/control");
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

    // 0. Manejo de Riego (Control y Estado)
    if (topic.startsWith("vinedo/parcela/") && (topic.endsWith("/riego") || topic.endsWith("/control"))) {
        try {
            const parts = topic.split("/");
            const parcelaId = parts[2];
            const data = JSON.parse(payload);

            const comando = data.comando;
            const duracion = data.duracion || 0;
            const estado = data.estado;
            const modo = data.modo; // "AUTO" o "MANUAL"

            const update = {};
            if (comando === "ON" || estado === "ACTIVO") {
                update.riegoActivo = true;
                update.tiempoRestanteRiego = duracion; // Guardar en minutos
                if (modo) update.tipoRiego = modo;
            } else if (comando === "OFF" || estado === "INACTIVO") {
                update.riegoActivo = false;
                update.tiempoRestanteRiego = 0;
                if (modo) update.tipoRiego = modo;
            }

            if (Object.keys(update).length > 0) {
                await Parcela.findByIdAndUpdate(parcelaId, update);
                console.log(`💧 Riego actualizado para parcela ${parcelaId}: ${update.riegoActivo ? 'ON' : 'OFF'}`);
            }
        } catch (err) {
            console.error("Error procesando riego MQTT:", err.message);
        }
    }

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
            const riegoActivo = data.riegoActivo || false;
            const tiempoRestante = data.tiempoRestante || 0;

            // Preparar campos de actualización
            const updateFields = {
                humedad: humAire,
                temperatura: tempAire,
                humedadSuelo: humSuelo,
                ultimaConexion: new Date()
            };

            // Solo actualizar riego si viene como TRUE (protección contra falsos negativos de stats)
            // Si viene FALSE, mantenemos lo que esté en la base de datos (se apaga via /control o /riego)
            if (riegoActivo === true) {
                updateFields.riegoActivo = true;
                updateFields.tiempoRestanteRiego = tiempoRestante;
            }

            // 1. Intentar actualizar como Parcela
            let target = await Parcela.findByIdAndUpdate(parcelaId, updateFields, { new: true });

            // 2. Si no es parcela, intentar como Cava
            if (!target) {
                target = await Cava.findByIdAndUpdate(parcelaId, {
                    temperatura: tempAire,
                    humedad: humAire,
                    ultimaLectura: new Date()
                }, { new: true });

                if (target) {
                    console.log(`🍷 Datos de cava actualizados: ${target.nombre}`);
                    return; // No guardamos historial de cava en la misma colección por ahora o sí?
                }
            }
            

            const ahora = Date.now();
            const ultimo = ultimosGuardados.get(parcelaId) || 0;
            const QUINCE_MINUTOS = 15 * 60 * 1000;

            // Guardar en historial solo cada 15 minutos (Solo para parcelas por ahora)
            if (ahora - ultimo >= QUINCE_MINUTOS &&target && target.constructor.modelName === 'Parcela') {
                // Calcular consumo de agua en este intervalo de 15 min (si el riego estaba activo)
                let consumoIntervalo = 0;
                if (target.riegoActivo) {
                    const horas = 15 / 60; // 0.25 horas
                    consumoIntervalo = horas * target.consumoAguaM2 * target.areaM2;
                }

                await HistorialSensor.create({
                    parcela: parcelaId,
                    humedadAire: humAire,
                    temperaturaAire: tempAire,
                    humedadSuelo: humSuelo,
                    consumoAgua: consumoIntervalo
                });
                ultimosGuardados.set(parcelaId, ahora);
                console.log(`📊 Historial guardado para parcela ${parcelaId}. Consumo: ${consumoIntervalo.toFixed(2)}L`);
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
    riegoActivo: parcela.riegoActivo,
    tiempoRestanteRiego: parcela.tiempoRestanteRiego,
    tipoRiego: parcela.tipoRiego,
    consumoAguaM2: parcela.consumoAguaM2,
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
