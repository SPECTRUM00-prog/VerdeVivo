package com.example.verdevivo.model

data class Planta(
    val id: Long?,
    val name: String?, // Nombre científico
    val preferred_common_name: String?, // Nombre común
    val default_photo: FotoDePlanta?,
    val observations_count: Int?
)

data class FotoDePlanta(
    val medium_url: String?,
    val square_url: String?
)

