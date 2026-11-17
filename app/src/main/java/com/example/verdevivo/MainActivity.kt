package com.example.verdevivo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnIngresar: Button
    private lateinit var btnGoogle: ImageButton
    private lateinit var dbHelper: DatabaseHelper

    // Variables para autenticación con Firebase y Google
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnGoogle = findViewById(R.id.btnGoogle)
        dbHelper = DatabaseHelper(this)

        // Inicializar Firebase Auth y CredentialManager
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // Evento para iniciar sesión con SQLite (Normal)
        btnIngresar.setOnClickListener {
            iniciarSesion()
        }

        // Evento para iniciar sesión con Google
        btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }

        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Metodo para iniciar sesion local con SQLite
    private fun iniciarSesion() {
        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val resultado = dbHelper.iniciarSesion(correo, password)

        if (resultado) {
            Toast.makeText(this, "Bienvenido a VerdeVivo", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. MÉTODO PARA AUTENTICAR EN FIREBASE
    private fun autenticarFirebase(idToken: String) {
        val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credencialFirebase).addOnCompleteListener(this) { inicio ->
            // Validar si el inicio de sesion con Firebase es exitoso
            if (inicio.isSuccessful) {
                val usuario = auth.currentUser
                Toast.makeText(this, "Bienvenido a VerdeVivo: ${usuario?.displayName}", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al validar con Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. MÉTODO PARA PROCESAR LA CREDENCIAL
    private fun procesarCredencial(credential: Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                // Variable para obtener la credencial de google
                val credencialGoogle = GoogleIdTokenCredential.createFrom(credential.data)
                autenticarFirebase(credencialGoogle.idToken)
            } catch (e: GoogleIdTokenParsingException) {
                Toast.makeText(this, "Credencial Inválida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 5. MÉTODO PARA INICIAR SESIÓN CON GOOGLE
    private fun iniciarSesionGoogle() {
        val inicioGoogle = GetSignInWithGoogleOption.Builder(
            getString(R.string.default_web_client_id)
        ).build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(inicioGoogle)
            .build()

        lifecycleScope.launch {
            try {
                // Validar que el resultado del inicio de sesion sea correcto
                val resultado = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = solicitud
                )
                procesarCredencial(resultado.credential)
            } catch (e: GetCredentialException) {
                Toast.makeText(this@MainActivity, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Metodo para mantener la sesion activa
    override fun onStart() {
        super.onStart()
        val usuarioActual = auth.currentUser
        // Validar si ya hay una sesión activa de Google
        if (usuarioActual != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}