package com.example.gymlab.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Đã đổi sang cổng 3001
    private const val BASE_URL = "http://10.0.2.2:3001/"

    val instance: AuthApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(AuthApi::class.java)
    }
}