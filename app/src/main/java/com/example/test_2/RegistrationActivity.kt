package com.example.test_2

import ApiClient
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test_2.data.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val nameEdt = findViewById<EditText>(R.id.idEdtName)
        val surnameEdt = findViewById<EditText>(R.id.idEdtSurname)
        val dobEdt = findViewById<EditText>(R.id.idEdtDob)
        val phoneEdt = findViewById<EditText>(R.id.idEdtPhone)
        val emailEdt = findViewById<EditText>(R.id.idEdtEmail)
        val passwordEdt = findViewById<EditText>(R.id.idEdtPassword)
        val registerBtn = findViewById<Button>(R.id.idBtnRegister)
        val regLink = findViewById<TextView>(R.id.idRegLink)

        regLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Установка DatePicker для даты рождения
        dobEdt.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                dobEdt.setText("$selectedDay.${selectedMonth + 1}.$selectedYear")
            }, year, month, day).show()
        }

        registerBtn.setOnClickListener {
            val inputName = nameEdt.text.toString().trim()
            val inputSurname = surnameEdt.text.toString().trim()
            val inputDob = dobEdt.text.toString().trim()
            val inputPhone = phoneEdt.text.toString().trim()
            val inputEmail = emailEdt.text.toString().trim()
            val inputPassword = passwordEdt.text.toString().trim()

            if (TextUtils.isEmpty(inputName) || TextUtils.isEmpty(inputSurname) || TextUtils.isEmpty(inputDob) ||
                TextUtils.isEmpty(inputPhone) || TextUtils.isEmpty(inputEmail) || TextUtils.isEmpty(inputPassword)) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                val newUser = User(
                    name = inputName,
                    surname = inputSurname,
                    dob = inputDob,
                    phone = inputPhone,
                    email = inputEmail,
                    password = inputPassword
                )
                registerUser(newUser)
            }
        }
    }

    private fun registerUser(user: User) {
        ApiClient.apiService.createUser(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegistrationActivity, "Вы успешно зарегистрировались!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@RegistrationActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@RegistrationActivity, "Ошибка регистрации: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@RegistrationActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
