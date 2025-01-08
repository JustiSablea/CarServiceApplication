package com.example.test_2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var themeSwitch: Switch
    private lateinit var fontSizeSpinner: Spinner
    private lateinit var logoutButton: Button
    private lateinit var clearCacheButton: Button
    private lateinit var aboutTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        themeSwitch = view.findViewById(R.id.themeSwitch)
        fontSizeSpinner = view.findViewById(R.id.fontSizeSpinner)
        logoutButton = view.findViewById(R.id.logoutButton)
        clearCacheButton = view.findViewById(R.id.clearCacheButton)
        aboutTextView = view.findViewById(R.id.aboutTextView)

        setupThemeSwitch()
        setupFontSizeSpinner()
        setupLogoutButton()
        setupClearCacheButton()
        setupAboutSection()
    }

    private fun setupThemeSwitch() {
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Проверяем, действительно ли изменилось состояние
            if (isChecked != isDarkMode) {
                sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }
    }


    private fun setupFontSizeSpinner() {
        val fontSizes = listOf("Маленький", "Средний", "Большой")
        fontSizeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            fontSizes
        )

        // Устанавливаем сохранённый размер шрифта
        val currentFontSize = sharedPreferences.getInt("font_size", 1)
        fontSizeSpinner.setSelection(currentFontSize)

        fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val savedFontSize = sharedPreferences.getInt("font_size", 1)
                if (savedFontSize != position) {
                    updateFontSize(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun updateFontSize(size: Int) {
        val scale = when (size) {
            0 -> 0.85f // Маленький
            1 -> 1.0f  // Средний
            2 -> 1.15f // Большой
            else -> 1.0f
        }

        val configuration = resources.configuration
        if (configuration.fontScale == scale) return // Если шрифт уже установлен, ничего не делаем

        configuration.fontScale = scale
        val metrics = resources.displayMetrics
        resources.updateConfiguration(configuration, metrics)

        sharedPreferences.edit().putInt("font_size", size).apply()

        // Перезапускаем активность для применения нового размера шрифта
        requireActivity().recreate()
    }


    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun setupClearCacheButton() {
        clearCacheButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                clearAppCache()
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Кэш очищен", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearAppCache() {
        val cacheDir = requireContext().cacheDir
        cacheDir.deleteRecursively()
    }

    private fun setupAboutSection() {
        val aboutText = """
            Спасибо, что пользуетесь нашим приложением!
            
            Разработчики:
            - Прилепин Максим
            - Azzmaks@mail.ru
            
            Мы всегда рады вашей обратной связи!
        """.trimIndent()
        aboutTextView.text = aboutText
    }
}
