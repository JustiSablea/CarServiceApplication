package com.example.test_2.data

import java.io.Serializable

data class User(
    val id: Int = 0,
    val name: String,
    val surname: String,
    val dob: String,
    val phone: String,
    val email: String,
    val password: String
) : Serializable
