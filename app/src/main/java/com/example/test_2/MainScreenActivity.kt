package com.example.test_2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.test_2.data.ServiceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Загружаем начальный фрагмент
        if (savedInstanceState == null) {
            loadFragment(ServiceListFragment()) // Фрагмент со списком сервисов по умолчанию
        }

        // Настраиваем нижнюю навигацию
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    loadFragment(ServiceListFragment())
                    true
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        // Инициализация данных с сервера
        fetchServicesFromApi()
    }

    private fun fetchServicesFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            ApiClient.apiService.getServices().enqueue(object : Callback<List<ServiceEntity>> {
                override fun onResponse(call: Call<List<ServiceEntity>>, response: Response<List<ServiceEntity>>) {
                    if (response.isSuccessful) {
                        val services = response.body() ?: emptyList()
                        println("Загружены автосервисы: $services")
                    } else {
                        println("Ошибка загрузки автосервисов: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<List<ServiceEntity>>, t: Throwable) {
                    println("Ошибка сети при загрузке автосервисов: ${t.message}")
                }
            })
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
