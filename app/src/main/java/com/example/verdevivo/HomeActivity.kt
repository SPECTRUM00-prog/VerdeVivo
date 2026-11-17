package com.example.verdevivo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.verdevivo.model.INaturalistResponse
import com.example.verdevivo.model.Planta
import com.example.verdevivo.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var tvEstado: TextView
    private lateinit var etBuscarPlanta: EditText
    private lateinit var btnBuscar: Button

    // Contenedores de búsqueda y categorías
    private lateinit var layoutResultadosBusqueda: LinearLayout
    private lateinit var tvTituloBusqueda: TextView
    private lateinit var btnLimpiarBusqueda: TextView
    private lateinit var contenedorBusqueda: LinearLayout
    private lateinit var layoutCategorias: LinearLayout

    private lateinit var contenedorInterior: LinearLayout
    private lateinit var contenedorSuculentas: LinearLayout
    private lateinit var contenedorFlores: LinearLayout

    // Barra de navegación inferior
    private lateinit var navClima: LinearLayout
    private lateinit var navJardin: LinearLayout
    private lateinit var navLuz: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Ajuste para evitar que el contenido choque con la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        tvEstado = findViewById(R.id.tvEstado)
        etBuscarPlanta = findViewById(R.id.etBuscarPlanta)
        btnBuscar = findViewById(R.id.btnBuscar)

        layoutResultadosBusqueda = findViewById(R.id.layoutResultadosBusqueda)
        tvTituloBusqueda = findViewById(R.id.tvTituloBusqueda)
        btnLimpiarBusqueda = findViewById(R.id.btnLimpiarBusqueda)
        contenedorBusqueda = findViewById(R.id.contenedorBusqueda)
        layoutCategorias = findViewById(R.id.layoutCategorias)

        contenedorInterior = findViewById(R.id.contenedorInterior)
        contenedorSuculentas = findViewById(R.id.contenedorSuculentas)
        contenedorFlores = findViewById(R.id.contenedorFlores)

        // Enlace de la barra de navegación inferior
        navClima = findViewById(R.id.navClima)
        navJardin = findViewById(R.id.navJardin)
        navLuz = findViewById(R.id.navLuz)

        navClima.setOnClickListener {
            startActivity(Intent(this, ClimaActivity::class.java))
        }

        navJardin.setOnClickListener {
            startActivity(Intent(this, MiJardinActivity::class.java))
        }

        navLuz.setOnClickListener {
            startActivity(Intent(this, LuzActivity::class.java))
        }

        // BÚSQUEDA OPTIMIZADA: Oculta las categorías de abajo para ahorrar recursos
        btnBuscar.setOnClickListener {
            val texto = etBuscarPlanta.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe el nombre de una planta", Toast.LENGTH_SHORT).show()
            } else {
                tvTituloBusqueda.text = "Resultados para \"$texto\""
                layoutResultadosBusqueda.visibility = View.VISIBLE
                layoutCategorias.visibility = View.GONE // Oculta las 3 categorías

                obtenerPlanta(texto, contenedorBusqueda)
            }
        }

        // Restaurar las 3 categorías originales
        btnLimpiarBusqueda.setOnClickListener {
            etBuscarPlanta.text.clear()
            layoutResultadosBusqueda.visibility = View.GONE
            layoutCategorias.visibility = View.VISIBLE // Vuelven a aparecer
        }

        // Cargar las 3 categorías iniciales
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
                        Toast.makeText(this@HomeActivity, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<INaturalistResponse?>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Error al conectar con la API", Toast.LENGTH_SHORT).show()
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