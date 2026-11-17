package com.example.verdevivo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class MiJardinActivity : AppCompatActivity() {

    private lateinit var contenedorJardin: LinearLayout
    private lateinit var tvJardinVacio: TextView
    private lateinit var btnAgregarManual: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mi_jardin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        contenedorJardin = findViewById(R.id.contenedorJardin)
        tvJardinVacio = findViewById(R.id.tvJardinVacio)
        btnAgregarManual = findViewById(R.id.btnAgregarManual)
        dbHelper = DatabaseHelper(this)

        btnAgregarManual.setOnClickListener {
            val intent = Intent(this, NuevaPlantaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPlantasGuardadas()
    }

    private fun cargarPlantasGuardadas() {
        contenedorJardin.removeAllViews()
        val plantas = dbHelper.obtenerPlantasJardin()

        if (plantas.isEmpty()) {
            tvJardinVacio.visibility = View.VISIBLE
            return
        }

        tvJardinVacio.visibility = View.GONE

        for (planta in plantas) {
            val vista = layoutInflater.inflate(R.layout.item_jardin, contenedorJardin, false)

            val img = vista.findViewById<ImageView>(R.id.imgJardinPlanta)
            val tvNombre = vista.findViewById<TextView>(R.id.tvJardinNombre)
            val tvEspecie = vista.findViewById<TextView>(R.id.tvJardinEspecie)
            val tvRiego = vista.findViewById<TextView>(R.id.tvJardinRiego)

            tvNombre.text = planta.nombreComun
            tvEspecie.text = planta.nombreCientifico
            tvRiego.text = "Regar cada ${planta.diasRiego} días"

            if (planta.fotoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(planta.fotoUrl)
                    .centerCrop()
                    .placeholder(R.drawable.bxs__leaf)
                    .into(img)
            } else {
                img.setImageResource(R.drawable.bxs__leaf)
            }

            vista.setOnClickListener {
                val intent = Intent(this, PlantaDetailActivity::class.java)
                intent.putExtra("ID_PLANTA", planta.idPlanta)
                intent.putExtra("NOMBRE_COMUN", planta.nombreComun)
                intent.putExtra("NOMBRE_CIENTIFICO", planta.nombreCientifico)
                intent.putExtra("FOTO_PLANTA", planta.fotoUrl)
                startActivity(intent)
            }

            contenedorJardin.addView(vista)
        }
    }
}