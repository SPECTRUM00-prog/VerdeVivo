package com.example.verdevivo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.verdevivo.model.INaturalistResponse
import com.example.verdevivo.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var tvResultado: TextView
    private lateinit var imgPlanta: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        tvResultado = findViewById(R.id.tvResultado)
        imgPlanta = findViewById(R.id.imgPlanta)

        obtenerPlanta()
    }

    private fun obtenerPlanta() {
        RetrofitClient.api.buscarPlanta("monstera").enqueue(object : Callback<INaturalistResponse> {
            override fun onResponse(
                call: Call<INaturalistResponse>,
                response: Response<INaturalistResponse>
            ) {
                val plantas = response.body()?.results

                if (!plantas.isNullOrEmpty()) {
                    val planta = plantas[1] // Tomamos la Monstera deliciosa

                    tvResultado.text = "Nombre común: ${planta.preferred_common_name}\n\nEspecie: ${planta.name}"

                    Glide.with(this@HomeActivity)
                        .load(planta.default_photo?.medium_url)
                        .into(imgPlanta)
                } else {
                    Toast.makeText(this@HomeActivity, "No hay resultados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<INaturalistResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error en el API", Toast.LENGTH_SHORT).show()
            }
        })
    }
}