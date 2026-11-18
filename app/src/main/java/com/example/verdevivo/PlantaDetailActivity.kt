package com.example.verdevivo

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class PlantaDetailActivity : AppCompatActivity() {

    private lateinit var imgFotoDetalle: ImageView
    private lateinit var tvNombrePlantaDetalle: TextView
    private lateinit var tvEspeciePlantaDetalle: TextView
    private lateinit var tvDetallePlanta: TextView
    private lateinit var btnFavorito: ImageButton

    private lateinit var dbHelper: DatabaseHelper

    private var plantaFavorita = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_planta_detail)

        dbHelper = DatabaseHelper(this)

        imgFotoDetalle = findViewById(R.id.imgFotoDetalle)
        tvNombrePlantaDetalle = findViewById(R.id.tvNombrePlantaDetalle)
        tvEspeciePlantaDetalle = findViewById(R.id.tvEspeciePlantaDetalle)
        tvDetallePlanta = findViewById(R.id.tvDetallePlanta)
        btnFavorito = findViewById(R.id.btnFavorito)

        mostrarInformacionPlanta()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    //Metodo para obtener toda la informacion
    private fun mostrarInformacionPlanta() {

        val idPlanta = intent.getLongExtra("ID_PLANTA", 0L)
        val nombreComun = intent.getStringExtra("NOMBRE_COMUN") ?: "Planta sin nombre"
        val nombreCientifico = intent.getStringExtra("NOMBRE_CIENTIFICO") ?: "Especie no identificada"
        val observaciones = intent.getIntExtra("OBSERVACIONES_PLANTA", 0)
        val imagen = intent.getStringExtra("FOTO_PLANTA") ?: ""

        tvNombrePlantaDetalle.text = nombreComun
        tvEspeciePlantaDetalle.text = nombreCientifico
        tvDetallePlanta.text = if (observaciones > 0) {
            "Taxon ID: $idPlanta • $observaciones observaciones"
        } else {
            "Guardada en Mi Jardín"
        }

        Glide.with(this)
            .load(imagen)
            .centerCrop()
            .placeholder(R.drawable.bxs__leaf)
            .into(imgFotoDetalle)

        //Validacion para verificar si la planta es favorita o no
        if (idPlanta != 0L) {
            plantaFavorita = dbHelper.plantaEstaGuardada(idPlanta)
        }

        //Pintar el icono inicial correspondiente
        if (plantaFavorita) {
            btnFavorito.setImageResource(R.drawable.ic_guardado_activo)
        } else {
            btnFavorito.setImageResource(R.drawable.ic_guardar_bd)
        }

        btnFavorito.setOnClickListener {

            if (plantaFavorita) {

                val eliminado = dbHelper.eliminarPlanta(idPlanta)

                //Validar que la eliminacion sea exitosa
                if (eliminado) {
                    plantaFavorita = !plantaFavorita
                    btnFavorito.setImageResource(R.drawable.ic_guardar_bd)
                    Toast.makeText(this, "Planta $nombreComun eliminada de favoritos", Toast.LENGTH_SHORT).show()
                }

            } else {

                val guardado = dbHelper.guardarPlanta(
                    idPlanta = idPlanta,
                    nombreComun = nombreComun,
                    nombreCientifico = nombreCientifico,
                    fotoUrl = imagen
                )

                //Validar que el registro sea exitoso
                if (guardado) {
                    plantaFavorita = !plantaFavorita
                    btnFavorito.setImageResource(R.drawable.ic_guardado_activo)
                    Toast.makeText(this, "Planta $nombreComun agregada a favoritos", Toast.LENGTH_SHORT).show()
                }

            }

        }

    }

}