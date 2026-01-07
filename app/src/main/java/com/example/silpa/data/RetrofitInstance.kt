package com.example.silpa.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // Jiak mengguhnakan emulator, ganti dengan 10.0.2.2 adalah IP localhost untuk emulator
    // Jika menggunakan device fisik (HP), ganti dengan IP Laptop (misal: 192.168.1.x)
    private const val BASE_URL = "http://192.168.0.24:8080/api/"

    fun getApi(context: Context): SilpaApiService {
        val sessionManager = SessionManager(context)

        // Interceptor untuk logging (melihat request/response di Logcat)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Interceptor untuk menyisipkan Token ke Header secara otomatis
        val authInterceptor = okhttp3.Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            val token = sessionManager.getToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SilpaApiService::class.java)
    }
}