package com.example.verdevivo.network

import com.example.verdevivo.model.ClimaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("forecast")
    fun obtenerClima(
        @Query("latitude") latitud: Double = 18.88,
        @Query("longitude") longitud: Double = -99.17,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,precipitation"
    ): Call<ClimaResponse>
}