package com.example.test_2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Brand(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
