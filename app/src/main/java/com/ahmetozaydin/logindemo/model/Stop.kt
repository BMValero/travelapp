package com.ahmetozaydin.logindemo.model

data class Stop(
    val atco_code: String?,
    val atco_latitude: Double?,
    val atco_longitude: Double?,
    val destinations: List<String?>?,
    val direction: String?,
    val identifier: String?,
    val latitude: Double?,
    val locality: Any?,
    val longitude: Double?,
    val name: String?,
    val orientation: Int?,
    val service_type: String?,
    val services: List<String?>?,
    val stop_id: Int?
)