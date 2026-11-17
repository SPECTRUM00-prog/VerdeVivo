package com.example.verdevivo

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LuzActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var layoutLuz: LinearLayout
    private lateinit var tvTituloLuz: TextView
    private lateinit var tvValorLuz: TextView
    private lateinit var tvConsejoLuz: TextView

    private lateinit var sensorManager: SensorManager
    private var sensorLuz: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_luz)

        layoutLuz = findViewById(R.id.layoutLuz)
        tvTituloLuz = findViewById(R.id.tvTituloLuz)
        tvValorLuz = findViewById(R.id.tvValorLuz)
        tvConsejoLuz = findViewById(R.id.tvConsejoLuz)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorLuz = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (sensorLuz == null) {
            tvConsejoLuz.text = "Este teléfono no cuenta con sensor de luz :("
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0?.sensor?.type == Sensor.TYPE_LIGHT) {
            val luz = p0.values[0]
            tvValorLuz.text = "$luz lx"

            if (luz < 200) {
                layoutLuz.setBackgroundColor(Color.parseColor("#1B5E20"))
                tvTituloLuz.setTextColor(Color.WHITE)
                tvValorLuz.setTextColor(Color.WHITE)
                tvConsejoLuz.setTextColor(Color.WHITE)
                tvConsejoLuz.text = "Sombra / Poca luz\nIdeal para plantas de interior tolerantes."
            } else if (luz <= 1000) {
                layoutLuz.setBackgroundColor(Color.parseColor("#D7EED8"))
                tvTituloLuz.setTextColor(Color.parseColor("#1B5E20"))
                tvValorLuz.setTextColor(Color.parseColor("#1B5E20"))
                tvConsejoLuz.setTextColor(Color.parseColor("#1B5E20"))
                tvConsejoLuz.text = "Luz indirecta brillante\nIdeal para Monstera y Tulipanes."
            } else {
                layoutLuz.setBackgroundColor(Color.parseColor("#FFF9C4"))
                tvTituloLuz.setTextColor(Color.BLACK)
                tvValorLuz.setTextColor(Color.BLACK)
                tvConsejoLuz.setTextColor(Color.BLACK)
                tvConsejoLuz.text = "Sol directo intenso\nIdeal para Cactus, Suculentas y Rosas."
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onResume() {
        super.onResume()
        sensorLuz?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
}