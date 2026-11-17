package com.example.verdevivo.model

data class ClimaResponse(
    val current: ClimaActual?
)

data class ClimaActual(
    val temperature_2m: Double?,
    val relative_humidity_2m: Int?,
    val precipitation: Double?
)