package com.example.verdevivo

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.verdevivo.model.ClimaResponse
import com.example.verdevivo.network.OpenMeteoClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClimaActivity : AppCompatActivity() {

    private lateinit var tvTemperatura: TextView
    private lateinit var tvHumedadLluvia: TextView
    private lateinit var tvConsejoRiego: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clima)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvHumedadLluvia = findViewById(R.id.tvHumedadLluvia)
        tvConsejoRiego = findViewById(R.id.tvConsejoRiego)

        cargarClima()
    }

    private fun cargarClima() {
        OpenMeteoClient.api.obtenerClima().enqueue(object : Callback<ClimaResponse> {
            override fun onResponse(call: Call<ClimaResponse>, response: Response<ClimaResponse>) {
                val clima = response.body()?.current
                if (clima != null) {
                    val temp = clima.temperature_2m ?: 0.0
                    val hum = clima.relative_humidity_2m ?: 0
                    val lluvia = clima.precipitation ?: 0.0

                    tvTemperatura.text = "$temp °C"
                    tvHumedadLluvia.text = "Humedad: $hum%   •   Lluvia: $lluvia mm"

                    // Consejo automático con if/else como en tu Mockup 3
                    if (lluvia > 0.0) {
                        tvConsejoRiego.text = "Hay presencia de lluvia, tus plantas de exterior no requieren riego hoy."
                    } else if (temp > 25.0) {
                        tvConsejoRiego.text = "Clima cálido, riega tus plantas por la tarde para evitar evaporación."
                    } else {
                        tvConsejoRiego.text = "Clima templado, revisa la tierra antes de regar."
                    }
                }
            }

            override fun onFailure(call: Call<ClimaResponse>, t: Throwable) {
                Toast.makeText(this@ClimaActivity, "Error al consultar el clima", Toast.LENGTH_SHORT).show()
            }
        })
    }
}