package com.example.test_2

import VehiclesAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_2.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class ProfileFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userDobTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var manageVehiclesButton: Button

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var vehiclesRecyclerView: RecyclerView

    private lateinit var vehiclesAdapter: VehiclesAdapter

    private var currentUser: User? = null
    private var currentUserId: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Инициализация View
        userNameTextView = view.findViewById(R.id.userNameTextView)
        userDobTextView = view.findViewById(R.id.userDobTextView)
        userPhoneTextView = view.findViewById(R.id.userPhoneTextView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        manageVehiclesButton = view.findViewById(R.id.manageVehiclesButton)

        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView)
        vehiclesRecyclerView = view.findViewById(R.id.vehiclesRecyclerView)

        ordersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        vehiclesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        vehiclesAdapter = VehiclesAdapter(emptyList()) { vehicle ->
            showEditVehicleDialog(vehicle) {
                reloadVehicles()
            }
        }
        vehiclesRecyclerView.adapter = vehiclesAdapter

        // Получаем пользователя из аргументов
        currentUser = arguments?.getSerializable("user") as? User

        editProfileButton.setOnClickListener {
            openEditProfileDialog()
        }
        manageVehiclesButton.setOnClickListener {
            openManageVehiclesDialog()
        }

        loadProfile()
        loadOrders()
        reloadVehicles()

        return view
    }

    private fun loadOrders() {
        ApiClient.apiService.getAppointmentsByUser(currentUserId).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    if (isAdded) { // Проверяем, прикреплён ли фрагмент
                        ordersRecyclerView.adapter = OrdersAdapter(orders) { appointment ->
                            openServiceDetailPage(appointment.serviceName)
                        }
                    }
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                if (isAdded) { // Проверяем, прикреплён ли фрагмент
                    Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun openServiceDetailPage(serviceName: String) {
        ApiClient.apiService.getServices().enqueue(object : Callback<List<ServiceEntity>> {
            override fun onResponse(call: Call<List<ServiceEntity>>, response: Response<List<ServiceEntity>>) {
                if (response.isSuccessful) {
                    val services = response.body() ?: emptyList()
                    val service = services.find { it.name == serviceName }
                    if (service != null) {
                        val fragment = ServiceDetailFragment.newInstance(service)
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Toast.makeText(requireContext(), "Сервис не найден", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ServiceEntity>>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    @SuppressLint("SetTextI18n")
    private fun loadProfile() {
        currentUser?.let { user ->
            userNameTextView.text = "${user.name} ${user.surname}"  // Если surname пуст, будет "Maxim " + "null"
            userDobTextView.text = user.dob
            userPhoneTextView.text = user.phone
        }
    }

    private fun reloadVehicles() {
        val userId = currentUser?.id ?: return
        ApiClient.apiService.getCarsByUser(userId).enqueue(object : Callback<List<RetrofitCar>> {
            override fun onResponse(call: Call<List<RetrofitCar>>, response: Response<List<RetrofitCar>>) {
                if (response.isSuccessful) {
                    val retrofitCars = response.body() ?: emptyList()
                    val cars = retrofitCars.map { rc ->
                        Car(
                            id = rc.id,
                            brand = rc.brand,
                            model = rc.model,
                            licensePlate = rc.licensePlate,
                            color = rc.color,
                            productionYear = rc.productionYear,
                            userId = rc.userId
                        )
                    }
                    vehiclesAdapter.submitList(cars)
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки автомобилей", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<RetrofitCar>>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    // Диалог "Управление автомобилями"
    private fun openManageVehiclesDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manage_vehicles, null)
        val vehiclesRecyclerInDialog = dialogView.findViewById<RecyclerView>(R.id.vehiclesRecyclerView)
        val addVehicleButton = dialogView.findViewById<Button>(R.id.addVehicleButton)

        vehiclesRecyclerInDialog.layoutManager = LinearLayoutManager(requireContext())
        vehiclesRecyclerInDialog.adapter = vehiclesAdapter

        reloadVehicles() // обновляем данные

        addVehicleButton.setOnClickListener {
            showAddVehicleDialog {
                reloadVehicles()
            }
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Управление автомобилями")
            .setView(dialogView)
            .setNegativeButton("Закрыть", null)
            .create()

        alertDialog.setOnShowListener {
            // Настраиваем кнопку "Close"
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.aquamarine))
        }

        alertDialog.show()


    }

    // Диалог добавления автомобиля
    private fun showAddVehicleDialog(onVehicleAdded: () -> Unit) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_vehicle, null)
        val brandSpinner = dialogView.findViewById<Spinner>(R.id.brandSpinner)
        val modelSpinner = dialogView.findViewById<Spinner>(R.id.modelSpinner)
        val licensePlateEditText = dialogView.findViewById<EditText>(R.id.licensePlateEditText)
        val colorEditText = dialogView.findViewById<EditText>(R.id.colorEditText)
        val productionYearEditText = dialogView.findViewById<EditText>(R.id.productionYearEditText)

        // Если есть бренды/модели на сервере, грузим их:
        // (иначе можно оставить заглушку)
        ApiClient.apiService.getBrands().enqueue(object : Callback<List<Brand>> {
            override fun onResponse(call: Call<List<Brand>>, response: Response<List<Brand>>) {
                if (response.isSuccessful) {
                    val brands = response.body() ?: emptyList()
                    val brandNames = brands.map { it.name }
                    val brandAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        brandNames
                    )
                    brandSpinner.adapter = brandAdapter

                    brandSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val selectedBrandId = brands[position].id
                                ApiClient.apiService.getModelsByBrand(selectedBrandId)
                                    .enqueue(object : Callback<List<Model>> {
                                        override fun onResponse(
                                            call: Call<List<Model>>,
                                            response: Response<List<Model>>
                                        ) {
                                            if (response.isSuccessful) {
                                                val models = response.body() ?: emptyList()
                                                val modelNames = models.map { it.name }
                                                val modelAdapter = ArrayAdapter(
                                                    requireContext(),
                                                    android.R.layout.simple_spinner_dropdown_item,
                                                    modelNames
                                                )
                                                modelSpinner.adapter = modelAdapter
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<List<Model>>,
                                            t: Throwable
                                        ) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Ошибка загрузки моделей: ${t.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                }
            }

            override fun onFailure(call: Call<List<Brand>>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки брендов: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Добавление автомобиль")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val brand = brandSpinner.selectedItem?.toString() ?: ""
                val model = modelSpinner.selectedItem?.toString() ?: ""
                val plate = licensePlateEditText.text.toString().trim()
                val color = colorEditText.text.toString().trim()
                val year = productionYearEditText.text.toString().toIntOrNull()
                val userId = currentUser?.id ?: 0

                if (brand.isEmpty() || model.isEmpty() || plate.isEmpty()
                    || color.isEmpty() || year == null || userId <= 0
                ) {
                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }

                val newCar = RetrofitCar(
                    id = 0,
                    brand = brand,
                    model = model,
                    licensePlate = plate,
                    color = color,
                    productionYear = year,
                    userId = userId
                )

                ApiClient.apiService.addCar(newCar).enqueue(object : Callback<RetrofitCar> {
                    override fun onResponse(
                        call: Call<RetrofitCar>,
                        response: Response<RetrofitCar>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Автомобиль добавлен",
                                Toast.LENGTH_SHORT
                            ).show()
                            onVehicleAdded()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Ошибка добавления: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<RetrofitCar>, t: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка сети: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            .setNegativeButton("Закрыть", null)
            .create()

        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.aquamarine
                )
            )
            negativeButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.aquamarine
                )
            )
        }

        alertDialog.show()
    }


    // Редактирование авто
    private fun showEditVehicleDialog(vehicle: Car, onVehicleEdited: () -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_vehicle, null)
        val licensePlateEditText = dialogView.findViewById<EditText>(R.id.licensePlateEditText)
        val colorEditText = dialogView.findViewById<EditText>(R.id.colorEditText)
        val productionYearEditText = dialogView.findViewById<EditText>(R.id.productionYearEditText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        licensePlateEditText.setText(vehicle.licensePlate)
        colorEditText.setText(vehicle.color)
        productionYearEditText.setText(vehicle.productionYear.toString())

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Изменение автомобиля")
            .setView(dialogView)
            .setNegativeButton("Закрыть", null)
            .setNeutralButton("Удалить") { _, _ ->
                deleteVehicle(vehicle, onVehicleEdited)
            }
            .create()

        alertDialog.setOnShowListener {
            // Получаем кнопку "Отмена" и устанавливаем ей цвет текста
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.aquamarine))

            val neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.aquamarine))
        }

        saveButton.setOnClickListener {
            val newPlate = licensePlateEditText.text.toString().trim()
            val newColor = colorEditText.text.toString().trim()
            val newYear = productionYearEditText.text.toString().toIntOrNull()

            if (newPlate.isEmpty() || newColor.isEmpty() || newYear == null) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedVehicle = vehicle.copy(
                licensePlate = newPlate,
                color = newColor,
                productionYear = newYear
            )

            updateVehicle(updatedVehicle) {
                alertDialog.dismiss()
                onVehicleEdited()
            }
        }

        alertDialog.show()
    }

    private fun updateVehicle(vehicle: Car, onUpdated: () -> Unit) {
        val rc = RetrofitCar(
            id = vehicle.id,
            brand = vehicle.brand,
            model = vehicle.model,
            licensePlate = vehicle.licensePlate,
            color = vehicle.color,
            productionYear = vehicle.productionYear,
            userId = vehicle.userId
        )
        ApiClient.apiService.updateCar(vehicle.id, rc).enqueue(object : Callback<RetrofitCar> {
            override fun onResponse(call: Call<RetrofitCar>, response: Response<RetrofitCar>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Автомобиль обновлён", Toast.LENGTH_SHORT).show()
                    onUpdated()
                } else {
                    Toast.makeText(requireContext(), "Ошибка обновления: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RetrofitCar>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteVehicle(vehicle: Car, onDeleted: () -> Unit) {
        ApiClient.apiService.deleteCar(vehicle.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Автомобиль удалён", Toast.LENGTH_SHORT).show()
                    onDeleted()
                } else {
                    Toast.makeText(requireContext(), "Ошибка удаления: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Редактирование профиля
    private fun openEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val surnameEditText = dialogView.findViewById<EditText>(R.id.surnameEditText)
        val dobEditText = dialogView.findViewById<EditText>(R.id.dobEditText)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.phoneEditText)
        val saveProfileButton = dialogView.findViewById<Button>(R.id.saveProfileButton)

        currentUser?.let { u ->
            nameEditText.setText(u.name)
            surnameEditText.setText(u.surname)
            dobEditText.setText(u.dob)
            phoneEditText.setText(u.phone)
        }

        dobEditText.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                dobEditText.setText("$day.${month+1}.$year")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Редактировать профиль")
            .setView(dialogView)
            .setNegativeButton("Закрыть", null)
            .create()

        alertDialog.setOnShowListener {
            // Получаем кнопку "Отмена" и устанавливаем ей цвет текста
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.aquamarine))
        }

        saveProfileButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            val newSurname = surnameEditText.text.toString().trim()
            val newDob = dobEditText.text.toString().trim()
            val newPhone = phoneEditText.text.toString().trim()

            if (newName.isEmpty() || newSurname.isEmpty() || newDob.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedUser = currentUser?.copy(
                name = newName,
                surname = newSurname,
                dob = newDob,
                phone = newPhone,
                password = currentUser?.password ?: ""
            )

            updatedUser?.let { u ->
                ApiClient.apiService.updateUser(u.id, u).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            currentUser = response.body()
                            loadProfile()
                            Toast.makeText(requireContext(), "Данные обновлены", Toast.LENGTH_SHORT).show()
                            alertDialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        alertDialog.show()
    }
}
