package com.example.gymlab.api

import retrofit2.http.GET

interface ApiService {
    @GET("get_users.php")
    suspend fun getUsers(): List<User>
}