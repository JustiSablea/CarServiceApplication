package com.example.test_2.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Brand::class,
            parentColumns = ["id"],
            childColumns = ["brandId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Model(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brandId: Int
)
