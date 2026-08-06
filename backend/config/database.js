const mongoose = require("mongoose");
const { MongoClient, ServerApiVersion } = require('mongodb');

const connectDB = async () => {
  try {

    await mongoose.connect(process.env.MONGODB_URI,{});

    console.log("✅ MongoDB Atlas conectado correctamente");
  } catch (error) {
    console.error(error);
  }
};

module.exports = connectDB;