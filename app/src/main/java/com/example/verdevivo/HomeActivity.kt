package com.example.verdevivo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.verdevivo.model.INaturalistResponse
import com.example.verdevivo.model.Planta
import com.example.verdevivo.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var tvEstado: TextView
    private lateinit var contenedorInterior: LinearLayout
    private lateinit var contenedorSuculentas: LinearLayout
    private lateinit var contenedorFlores: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        tvEstado = findViewById(R.id.tvEstado)
        contenedorInterior = findViewById(R.id.contenedorInterior)
        contenedorSuculentas = findViewById(R.id.contenedorSuculentas)
        contenedorFlores = findViewById(R.id.contenedorFlores)

        obtenerPlanta("monstera", contenedorInterior)
        obtenerPlanta("cactus", contenedorSuculentas)
        obtenerPlanta("rosa", contenedorFlores)
    }

    private fun obtenerPlanta(busqueda: String, contenedor: LinearLayout) {
        RetrofitClient.api.buscarPlanta(nombre = busqueda)
            .enqueue(object : Callback<INaturalistResponse?> {

                override fun onResponse(
                    call: Call<INaturalistResponse?>,
                    response: Response<INaturalistResponse?>
                ) {
                    val plantas = response.body()?.results ?: emptyList()

                    if (plantas.isNotEmpty()) {
                        tvEstado.visibility = View.GONE
                        mostrarPlantas(plantas, contenedor)
                    } else {
                        Toast.makeText(this@HomeActivity, "No hay resultados", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<INaturalistResponse?>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Error en el API", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun mostrarPlantas(plantas: List<Planta>, contenedor: LinearLayout) {

        contenedor.removeAllViews()

        for (planta in plantas) {
            val urlFoto = planta.default_photo?.medium_url

            if (urlFoto != null) {
                val vistaPlanta = layoutInflater.inflate(R.layout.item_planta, contenedor, false)

                val imgFoto = vistaPlanta.findViewById<ImageView>(R.id.imgFotoPlanta)
                val tvNombreComun = vistaPlanta.findViewById<TextView>(R.id.tvNombreComun)
                val tvNombreCientifico = vistaPlanta.findViewById<TextView>(R.id.tvNombreCientifico)

                val nombreComun = planta.preferred_common_name ?: "Planta sin nombre"
                val nombreCientifico = planta.name ?: "Especie no identificada"

                tvNombreComun.text = nombreComun
                tvNombreCientifico.text = nombreCientifico

                Glide.with(this)
                    .load(urlFoto)
                    .centerCrop()
                    .into(imgFoto)

                vistaPlanta.setOnClickListener {
                    val intent = Intent(this@HomeActivity, PlantaDetailActivity::class.java)

                    intent.putExtra("ID_PLANTA", planta.id ?: 0L)
                    intent.putExtra("NOMBRE_COMUN", nombreComun)
                    intent.putExtra("NOMBRE_CIENTIFICO", nombreCientifico)
                    intent.putExtra("OBSERVACIONES_PLANTA", planta.observations_count ?: 0)
                    intent.putExtra("FOTO_PLANTA", urlFoto)

                    startActivity(intent)
                }

                contenedor.addView(vistaPlanta)
            }
        }
    }
}