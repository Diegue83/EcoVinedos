const mongoose = require("mongoose");
const { MongoClient, ServerApiVersion } = require('mongodb');

const connectDB = async () => {
  try {
    console.log(process.env.MONGO_URI_PROD);

    await mongoose.connect(process.env.MONGO_URI_PROD,{});

    console.log("✅ MongoDB Atlas conectado correctamente");
  } catch (error) {
    console.error(error);
  }
};

module.exports = connectDB;