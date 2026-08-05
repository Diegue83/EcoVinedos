const mqtt = require("mqtt");
const Parcela = require("../models/Parcela");

const BROKER = "mqtt://192.168.7.93:1883";

const client = mqtt.connect(BROKER, {
    reconnectPeriod: 3000,
    clean: true
});

client.on("connect", () => {
    console.log("✅ Conectado al broker MQTT");
    publicarListaParcelas()
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
    temperatura: parcela.temperatura
});

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