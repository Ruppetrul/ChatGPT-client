package com.example.chatgpt

import okhttp3.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface OpenAiApi {
    @Headers("Content-Type: application/json")
    @GET("models")
    suspend fun getModels(@Header("Authorization: Bearer ") apiKey: String): List<Model>

}
