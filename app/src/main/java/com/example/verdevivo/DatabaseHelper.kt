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
    DATABASE_VERSION,
) {

    companion object {
        private const val DATABASE_NAME = "verdevivo.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_USUARIOS = "usuarios"
        private const val COL_ID = "id"
        private const val COL_NOMBRE = "nombre"
        private const val COL_CORREO = "correo"
        private const val COL_PASSWORD = "password"

        const val TABLE_JARDIN = "jardin"
        const val COL_JARDIN_ID = "id"
        const val COL_ID_PLANTA = "id_planta"
        const val COL_NOMBRE_COMUN = "nombre_comun"
        const val COL_NOMBRE_CIENTIFICO = "nombre_cientifico"
        const val COL_FOTO_URL = "foto_url"
        const val COL_DIAS_RIEGO = "dias_riego"

        //Crear tabla de usuarios
        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE $TABLE_USUARIOS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE TEXT NOT NULL,
                $COL_CORREO TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL
            )
        """

        //Crear tabla de favoritos
        private const val CREATE_TABLE_JARDIN = """
            CREATE TABLE $TABLE_JARDIN (
                $COL_JARDIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ID_PLANTA INTEGER UNIQUE NOT NULL,
                $COL_NOMBRE_COMUN TEXT NOT NULL,
                $COL_NOMBRE_CIENTIFICO TEXT,
                $COL_FOTO_URL TEXT,
                $COL_DIAS_RIEGO INTEGER DEFAULT 4
            )
        """
    }

    //METODO PARA CREAR LA BASE DE DATOS
    override fun onCreate(db: SQLiteDatabase?) {
        //Crear tabla de SQL
        db?.execSQL(CREATE_TABLE_USUARIOS)
        db?.execSQL(CREATE_TABLE_JARDIN)
    }

    //METODO PARA ACTUALIZAR LA BASE DE DATOS
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //Verificar que la version sea menor a 2
        if (oldVersion < 2) {
            db?.execSQL(CREATE_TABLE_JARDIN)
        }
    }

    //METODO PARA INSERTAR REGISTRO
    fun registrarUsuario(nombre: String, correo: String, password: String): Boolean {
        //Crear variable para que la base de datos se pueda modificar
        val db = writableDatabase

        //Crear variable para recibir los datos que escribe el usuario
        val datos = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, password)
        }

        //Insertar datos en la tabla
        val resultado = db.insert(TABLE_USUARIOS, null, datos)

        //Cerrar conexion con la BD
        db.close()

        //Comparar el valor del resultado de la insercion y regresarlo como boolean
        return resultado != -1L
    }

    //Metodo para consultar a un usuario
    fun iniciarSesion(correo: String, password: String): Boolean {
        //Crear variable para que la base de datos se pueda leer
        val db = readableDatabase

        //Crear variable para realizar la consulta
        val query_sql = """
            SELECT * FROM $TABLE_USUARIOS
            WHERE $COL_CORREO = ? AND $COL_PASSWORD = ?
        """.trimIndent()

        //Crear cursor para recorrer la informacion
        val cursor = db.rawQuery(
            query_sql, arrayOf(correo, password)
        )

        //Crear variable de tipo boolean que permita saber si el cursor encontro un registro
        val usuarioEncontrado = cursor.count > 0

        cursor.close()
        db.close()

        return usuarioEncontrado
    }

    //METODO PARA REGISTRAR INFORMACIÓN PROVENIENTE DEL API
    fun guardarPlanta(
        idPlanta: Long,
        nombreComun: String,
        nombreCientifico: String,
        fotoUrl: String,
        diasRiego: Int = 4
    ): Boolean {
        //Declarar uso de la BD
        val db = writableDatabase

        val datos = ContentValues().apply {
            put(COL_ID_PLANTA, idPlanta)
            put(COL_NOMBRE_COMUN, nombreComun)
            put(COL_NOMBRE_CIENTIFICO, nombreCientifico)
            put(COL_FOTO_URL, fotoUrl)
            put(COL_DIAS_RIEGO, diasRiego)
        }

        //Insercion en la tabla
        val resultado = db.insertWithOnConflict(
            TABLE_JARDIN,
            null,
            datos,
            SQLiteDatabase.CONFLICT_IGNORE
        )

        return resultado != -1L
    }

    //METODO PARA ELIMINAR UN REGISTRO
    fun eliminarPlanta(idPlanta: Long): Boolean {
        val db = writableDatabase

        val filasAfectadas = db.delete(
            TABLE_JARDIN,
            "$COL_ID_PLANTA = ?",
            arrayOf(idPlanta.toString())
        )
        return filasAfectadas > 0
    }

    //Metodo para verificar que un registro existe
    fun plantaEstaGuardada(idPlanta: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_JARDIN_ID FROM $TABLE_JARDIN WHERE $COL_ID_PLANTA = ?",
            arrayOf(idPlanta.toString())
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    //Metodo para obtener las plantas guardadas en Mi Jardín
    fun obtenerPlantasJardin(): List<PlantaJardin> {
        val lista = mutableListOf<PlantaJardin>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_JARDIN", null)

        while (cursor.moveToNext()) {
            val idPlanta = cursor.getLong(1)
            val nombreComun = cursor.getString(2)
            val especie = cursor.getString(3)
            val fotoUrl = cursor.getString(4)
            val diasRiego = cursor.getInt(5)

            lista.add(PlantaJardin(idPlanta, nombreComun, especie, fotoUrl, diasRiego))
        }

        cursor.close()
        return lista
    }
}