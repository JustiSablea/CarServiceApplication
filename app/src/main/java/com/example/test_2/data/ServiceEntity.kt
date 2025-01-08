package com.example.test_2.data

import com.google.gson.annotations.SerializedName


data class ServiceEntity(
    val name: String,
    val location: String,
    val description: String?,
    val hours: String,
    val supportedBrands: String,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("map_image_url") val mapImageUrl: String?
)
