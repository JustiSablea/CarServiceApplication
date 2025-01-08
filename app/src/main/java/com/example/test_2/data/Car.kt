package com.example.test_2.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brand: String,
    val model: String,
    @SerializedName("license_plate") val licensePlate: String,
    val color: String, // Цвет автомобиля
    @SerializedName("production_year") val productionYear: Int, // Год производства
    @SerializedName("user_id") val userId: Int
)

