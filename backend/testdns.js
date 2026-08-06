const dns = require("dns");

dns.setDefaultResultOrder("ipv4first");

dns.resolveSrv("_mongodb._tcp.testcluster.xekvusg.mongodb.net", (err, addresses) => {
    if (err) {
        console.error("Error DNS:", err);
    } else {
        console.log("SRV encontrado:");
        console.log(addresses);
    }
});