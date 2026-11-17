package com.example.verdevivo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Modelo sencillo para las plantas guardadas en SQLite
data class PlantaJardin(
    val idPlanta: Long,
    val nombreComun: String,
    val nombreCientifico: String,
    val fotoUrl: String,
    val diasRiego: Int
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "verdevivo.db"
        private const val DATABASE_VERSION = 2

        // Tabla usuarios
        private const val TABLE_USUARIOS = "usuarios"
        private const val COL_ID = "id"
        private const val COL_NOMBRE = "nombre"
        private const val COL_CORREO = "correo"
        private const val COL_PASSWORD = "password"

        // Tabla jardin
        private const val TABLE_JARDIN = "jardin"
        private const val COL_JARDIN_ID = "id"
        private const val COL_ID_PLANTA = "id_planta"
        private const val COL_NOMBRE_COMUN = "nombre_comun"
        private const val COL_NOMBRE_CIENTIFICO = "nombre_cientifico"
        private const val COL_FOTO_URL = "foto_url"
        private const val COL_DIAS_RIEGO = "dias_riego"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val crearTablaUsuarios = """
            CREATE TABLE IF NOT EXISTS $TABLE_USUARIOS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE TEXT NOT NULL,
                $COL_CORREO TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(crearTablaUsuarios)

        crearTablaJardin(db)
    }

    private fun crearTablaJardin(db: SQLiteDatabase?) {
        val crearTablaJardin = """
            CREATE TABLE IF NOT EXISTS $TABLE_JARDIN (
                $COL_JARDIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ID_PLANTA INTEGER NOT NULL UNIQUE,
                $COL_NOMBRE_COMUN TEXT NOT NULL,
                $COL_NOMBRE_CIENTIFICO TEXT NOT NULL,
                $COL_FOTO_URL TEXT NOT NULL,
                $COL_DIAS_RIEGO INTEGER DEFAULT 4
            )
        """.trimIndent()
        db?.execSQL(crearTablaJardin)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            crearTablaJardin(db)
        }
    }

    // --- METODOS DE USUARIOS ---
    fun registrarUsuario(nombre: String, correo: String, password: String): Boolean {
        val db = writableDatabase
        val datos = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, password)
        }
        val resultado = db.insert(TABLE_USUARIOS, null, datos)
        return resultado != -1L
    }

    fun iniciarSesion(correo: String, password: String): Boolean {
        val db = readableDatabase
        val query_sql = """
            SELECT * FROM $TABLE_USUARIOS
            WHERE $COL_CORREO = ? AND $COL_PASSWORD = ?
        """.trimIndent()

        val cursor = db.rawQuery(query_sql, arrayOf(correo, password))
        val usuarioEncontrado = cursor.moveToFirst()
        cursor.close()
        return usuarioEncontrado
    }

    // --- METODOS DE MI JARDIN ---
    fun guardarPlanta(idPlanta: Long, nombreComun: String, nombreCientifico: String, fotoUrl: String, diasRiego: Int = 4): Boolean {
        val db = writableDatabase
        val datos = ContentValues().apply {
            put(COL_ID_PLANTA, idPlanta)
            put(COL_NOMBRE_COMUN, nombreComun)
            put(COL_NOMBRE_CIENTIFICO, nombreCientifico)
            put(COL_FOTO_URL, fotoUrl)
            put(COL_DIAS_RIEGO, diasRiego)
        }
        val resultado = db.insert(TABLE_JARDIN, null, datos)
        return resultado != -1L
    }

    fun eliminarPlanta(idPlanta: Long): Boolean {
        val db = writableDatabase
        val borradas = db.delete(TABLE_JARDIN, "$COL_ID_PLANTA = ?", arrayOf(idPlanta.toString()))
        return borradas > 0
    }

    fun plantaEstaGuardada(idPlanta: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_JARDIN WHERE $COL_ID_PLANTA = ?",
            arrayOf(idPlanta.toString())
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    fun obtenerPlantasJardin(): List<PlantaJardin> {
        val lista = mutableListOf<PlantaJardin>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_JARDIN ORDER BY $COL_JARDIN_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val idPlanta = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID_PLANTA))
                val nombreComun = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_COMUN))
                val nombreCientifico = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CIENTIFICO))
                val fotoUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOTO_URL))
                val diasRiego = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DIAS_RIEGO))

                lista.add(PlantaJardin(idPlanta, nombreComun, nombreCientifico, fotoUrl, diasRiego))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
}