package com.example.gymlab.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 trỏ đến localhost của máy tính từ Emulator
    // Nếu dùng máy thật, hãy đổi thành IP của máy tính (ví dụ: 192.168.1.x)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val instance: AuthApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(AuthApi::class.java)
    }
}