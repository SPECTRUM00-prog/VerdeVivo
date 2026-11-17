package com.example.verdevivo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class NuevaPlantaActivity : AppCompatActivity() {

    private lateinit var cardTomarFoto: CardView
    private lateinit var imgFotoCapturada: ImageView
    private lateinit var ivIconoCamara: ImageView
    private lateinit var tvTextoCamara: TextView

    private lateinit var actvNombrePlanta: AutoCompleteTextView
    private lateinit var etEspeciePlanta: EditText
    private lateinit var etDiasRiego: EditText
    private lateinit var btnGuardarPlanta: Button
    private lateinit var dbHelper: DatabaseHelper

    // Catálogo nativo para autocompletar la especie y días de riego
    private val catalogoSugerencias = mapOf(
        "Tulipán" to Pair("Tulipa gesneriana", 2),
        "Monstera" to Pair("Monstera deliciosa", 4),
        "Rosa" to Pair("Rosa gallica", 3),
        "Cactus" to Pair("Echinocactus grusonii", 15),
        "Girasol" to Pair("Helianthus annuus", 2),
        "Hortensia" to Pair("Hydrangea macrophylla", 3),
        "Suculenta" to Pair("Echeveria elegans", 10),
        "Duranta" to Pair("Duranta erecta", 4),
        "Azahar de la China" to Pair("Pittosporum tobira", 5)
    )

    // Contrato nativo oficial de Android: abre la cámara y te da la foto tomada
    private val tomarFotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { fotoBitmap: Bitmap? ->
        if (fotoBitmap != null) {
            imgFotoCapturada.setImageBitmap(fotoBitmap)
            imgFotoCapturada.visibility = View.VISIBLE
            ivIconoCamara.visibility = View.GONE
            tvTextoCamara.visibility = View.GONE
        } else {
            Toast.makeText(this, "No se tomó la foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nueva_planta)

        cardTomarFoto = findViewById(R.id.cardTomarFoto)
        imgFotoCapturada = findViewById(R.id.imgFotoCapturada)
        ivIconoCamara = findViewById(R.id.ivIconoCamara)
        tvTextoCamara = findViewById(R.id.tvTextoCamara)

        actvNombrePlanta = findViewById(R.id.actvNombrePlanta)
        etEspeciePlanta = findViewById(R.id.etEspeciePlanta)
        etDiasRiego = findViewById(R.id.etDiasRiego)
        btnGuardarPlanta = findViewById(R.id.btnGuardarPlanta)
        dbHelper = DatabaseHelper(this)

        // Al tocar el recuadro, lanza la cámara de tu celular directamente
        cardTomarFoto.setOnClickListener {
            tomarFotoLauncher.launch(null)
        }

        // Configuración de sugerencias automáticas
        val listaNombres = catalogoSugerencias.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNombres)
        actvNombrePlanta.setAdapter(adapter)

        actvNombrePlanta.setOnItemClickListener { _, _, position, _ ->
            val nombreSeleccionado = adapter.getItem(position)
            val datos = catalogoSugerencias[nombreSeleccionado]
            if (datos != null) {
                etEspeciePlanta.setText(datos.first)
                etDiasRiego.setText(datos.second.toString())
            }
        }

        // Guardar en la base de datos local SQLite
        btnGuardarPlanta.setOnClickListener {
            val nombre = actvNombrePlanta.text.toString().trim()
            val especie = etEspeciePlanta.text.toString().trim()
            val dias = etDiasRiego.text.toString().toIntOrNull() ?: 4

            if (nombre.isEmpty() || especie.isEmpty()) {
                Toast.makeText(this, "Completa el nombre y la especie", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idUnico = System.currentTimeMillis()
            val guardado = dbHelper.guardarPlanta(idUnico, nombre, especie, "", dias)

            if (guardado) {
                Toast.makeText(this, "$nombre agregada a Mi Jardín", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al guardar en base de datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}