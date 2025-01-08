package com.example.test_2.data

import com.google.gson.annotations.SerializedName

data class Appointment(
    val id: Int = 0,
    @SerializedName("user_id")
    val userId: Int,
    val brand: String,
    val model: String,
    @SerializedName("license_plate")
    val licensePlate: String,
    val date: Long,
    @SerializedName("problem_description")
    val problemDescription: String,
    @SerializedName("service_name")
    val serviceName: String
)
