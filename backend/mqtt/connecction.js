const mqtt = require("mqtt");
const Parcela = require("../models/Parcela");
const SeccionCava = require("../models/SeccionCava");
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
            let target = await Parcela.findByIdAndUpdate(parcelaId, updateFields, { returnDocument: 'after' });

            // 2. Si no es parcela, intentar como Sección de Cava
            if (!target) {
                target = await SeccionCava.findByIdAndUpdate(parcelaId, {
                    temperatura: tempAire,
                    humedad: humAire,
                    ultimaLectura: new Date()
                }, { returnDocument: 'after' });

                if (target) {
                    console.log(`🍷 Datos de sección de cava actualizados: ${target.nombre}`);
                    return;
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
 * Mapea un objeto de parcela o sección de Mongoose a un objeto plano para envío MQTT.
 *
 * @param {Object} item - Objeto parcela o sección de la base de datos.
 * @param {String} type - Tipo del objeto ('PARCELA' o 'SECCION').
 * @returns {Object} Objeto formateado.
 */
const mapToMqtt = (item, type) => {
    const base = {
        _id: String(item._id),
        type: type,
        temperatura: item.temperatura,
        humedad: item.humedad,
        ultimaLectura: item.ultimaLectura || item.ultimaConexion
    };

    if (type === 'PARCELA') {
        return {
            ...base,
            nombreParcela: item.nombreParcela,
            variedad: item.variedad,
            areaM2: item.areaM2,
            umbralHumedad: item.umbralHumedad,
            umbralTemp: item.umbralTemp,
            indiceMaduracion: item.indiceMaduracion,
            fechaCosecha: item.fechaCosecha,
            activa: item.activa,
            humedadSuelo: item.humedadSuelo,
            brix: item.brix,
            ph: item.ph,
            acidez: item.acidez,
            phSuelo: item.phSuelo,
            riegoActivo: item.riegoActivo,
            tiempoRestanteRiego: item.tiempoRestanteRiego,
            tipoRiego: item.tipoRiego,
            consumoAguaM2: item.consumoAguaM2,
            nodoVinculado: item.nodoVinculado
        };
    } else {
        return {
            ...base,
            nombre: item.nombre,
            tipo: item.tipo,
            capacidadBotellas: item.capacidadBotellas,
            botellasActuales: item.botellasActuales,
            sensorId: item.sensorId,
            estado: item.estado
        };
    }
};

/**
 * Publica la lista completa de parcelas y secciones en el tópico de lista MQTT.
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
        const secciones = await SeccionCava.find().lean();

        const listaParcelas = parcelas.map(p => mapToMqtt(p, 'PARCELA'));
        const listaSecciones = secciones.map(s => mapToMqtt(s, 'SECCION'));

        // Publicar lista de parcelas
        client.publish(
            "vinedo/parcelas/lista",
            JSON.stringify(listaParcelas),
            { qos: 1, retain: true },
            (err) => { if (err) console.error("Error publicando parcelas:", err); }
        );

        // Publicar lista de secciones
        client.publish(
            "vinedo/secciones/lista",
            JSON.stringify(listaSecciones),
            { qos: 1, retain: true },
            (err) => { if (err) console.error("Error publicando secciones:", err); }
        );

        console.log(`📤 Publicadas ${listaParcelas.length} parcelas y ${listaSecciones.length} secciones`);

    } catch (err) {
        console.error("Error obteniendo datos para MQTT:", err);
    }
}

module.exports = {
    client,
    publicarListaParcelas
};
