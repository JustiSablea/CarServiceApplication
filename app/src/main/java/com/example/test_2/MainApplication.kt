package com.example.test_2

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.test_2.data.Brand
import com.example.test_2.data.Model
import com.example.test_2.data.ServiceEntity
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("3483aa2d-b273-46b9-a80d-952a92b32c17")
        MapKitFactory.initialize(this)

        // Загрузка данных с сервера
        populateDatabaseFromApi()
        Log.d("MapKit", "MapKit инициализирован")

        val isDarkMode = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun populateDatabaseFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            fetchServices()
            fetchBrands()
            fetchModels()
        }
    }

    private fun fetchServices() {
        ApiClient.apiService.getServices().enqueue(object : Callback<List<ServiceEntity>> {
            override fun onResponse(call: Call<List<ServiceEntity>>, response: Response<List<ServiceEntity>>) {
                if (response.isSuccessful) {
                    val services = response.body() ?: emptyList()
                    println("Загружены автосервисы: $services")
                } else {
                    println("Ошибка загрузки автосервисов: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<ServiceEntity>>, t: Throwable) {
                println("Ошибка сети при загрузке автосервисов: ${t.message}")
            }
        })
    }

    private fun fetchBrands() {
        ApiClient.apiService.getBrands().enqueue(object : Callback<List<Brand>> {
            override fun onResponse(call: Call<List<Brand>>, response: Response<List<Brand>>) {
                if (response.isSuccessful) {
                    val brands = response.body() ?: emptyList()
                    println("Загружены бренды: $brands")
                } else {
                    println("Ошибка загрузки брендов: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Brand>>, t: Throwable) {
                println("Ошибка сети при загрузке брендов: ${t.message}")
            }
        })
    }

    private fun fetchModels() {
        ApiClient.apiService.getBrands().enqueue(object : Callback<List<Brand>> {
            override fun onResponse(call: Call<List<Brand>>, response: Response<List<Brand>>) {
                if (response.isSuccessful) {
                    val brands = response.body() ?: emptyList()
                    brands.forEach { brand ->
                        ApiClient.apiService.getModelsByBrand(brand.id).enqueue(object : Callback<List<Model>> {
                            override fun onResponse(call: Call<List<Model>>, response: Response<List<Model>>) {
                                if (response.isSuccessful) {
                                    val models = response.body() ?: emptyList()
                                    println("Загружены модели для бренда ${brand.name}: $models")
                                } else {
                                    println("Ошибка загрузки моделей для бренда ${brand.name}: ${response.message()}")
                                }
                            }

                            override fun onFailure(call: Call<List<Model>>, t: Throwable) {
                                println("Ошибка сети при загрузке моделей для бренда ${brand.name}: ${t.message}")
                            }
                        })
                    }
                } else {
                    println("Ошибка загрузки брендов для моделей: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Brand>>, t: Throwable) {
                println("Ошибка сети при загрузке брендов для моделей: ${t.message}")
            }
        })
    }
}
