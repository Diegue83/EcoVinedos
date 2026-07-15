package mx.utng.ecoviedos.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import mx.utng.ecoviedos.data.remote.ApiService

object RetrofitClient {
    // CAMBIA ESTA IP por la que te dio el comando ipconfig
    private const val BASE_URL = "http://192.168.7.93:3000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
