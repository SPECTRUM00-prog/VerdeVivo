package com.example.verdevivo.network

import com.example.verdevivo.model.INaturalistResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface INaturalistApi {

    @GET("taxa")
    fun buscarPlanta(
        @Query("q") nombre: String,
        @Query("taxon_id") taxonId: Int = 47126
    ): Call<INaturalistResponse>

}
