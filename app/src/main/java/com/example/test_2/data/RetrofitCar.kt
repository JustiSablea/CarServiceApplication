package com.example.test_2.data

import com.google.gson.annotations.SerializedName

data class RetrofitCar(
    val id: Int,
    val brand: String,
    val model: String,

    @SerializedName("license_plate")
    val licensePlate: String,

    val color: String,

    @SerializedName("production_year")
    val productionYear: Int,

    @SerializedName("user_id")
    val userId: Int
)
