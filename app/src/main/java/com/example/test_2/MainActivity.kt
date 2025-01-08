package com.example.test_2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.test_2.data.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigationView: BottomNavigationView

    // Храним текущего пользователя
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId < 0) {
            // Не авторизован
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Грузим пользователя по ID
        fetchCurrentUser(userId)
    }

    // Запрос getUserById(...)
    private fun fetchCurrentUser(userId: Int) {
        ApiClient.apiService.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    currentUser = response.body()
                    if (currentUser != null) {
                        initBottomNav()
                    } else {
                        // На всякий случай
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Ошибка сети
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        })
    }

    // Когда пользователь загружен, инициализируем навигацию.
    private fun initBottomNav() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(ServiceListFragment())
                    true
                }
                R.id.navigation_profile -> {
                    // Передаём currentUser в ProfileFragment
                    val profileFragment = ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putSerializable("user", currentUser)
                        }
                    }
                    loadFragment(profileFragment)
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        // По умолчанию "Home"
        bottomNavigationView.selectedItemId = R.id.navigation_home
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
