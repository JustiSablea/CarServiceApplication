package com.example.test_2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.test_2.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Date
import javax.sql.DataSource

class ServiceDetailFragment : Fragment() {

//    private lateinit var mapView: MapView
    private var currentUserId: Int = 1
    private lateinit var mapImageView: ImageView

    companion object {
        private const val ARG_SERVICE_NAME = "service_name"
        private const val ARG_SERVICE_LOCATION = "service_location"
        private const val ARG_SERVICE_HOURS = "service_hours"
        private const val ARG_SERVICE_DESCRIPTION = "service_description"
        private const val ARG_SERVICE_COORDINATES_LAT = "service_coordinates_lat"
        private const val ARG_SERVICE_COORDINATES_LNG = "service_coordinates_lng"
        private const val ARG_SERVICE_MAP_IMAGE_URL = "map_image_url"

        fun newInstance(service: ServiceEntity): ServiceDetailFragment {
            val fragment = ServiceDetailFragment()
            val args = Bundle().apply {
                putString(ARG_SERVICE_NAME, service.name)
                putString(ARG_SERVICE_LOCATION, service.location)
                putString(ARG_SERVICE_HOURS, service.hours)
                putString(ARG_SERVICE_DESCRIPTION, service.description)
                service.latitude?.let { putDouble(ARG_SERVICE_COORDINATES_LAT, it) }
                service.longitude?.let { putDouble(ARG_SERVICE_COORDINATES_LNG, it) }
                putString(ARG_SERVICE_MAP_IMAGE_URL, service.mapImageUrl)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        MapKitFactory.initialize(context)
        val view = inflater.inflate(R.layout.fragment_service_detail, container, false)

        val nameTextView: TextView = view.findViewById(R.id.serviceName)
        val locationTextView: TextView = view.findViewById(R.id.serviceLocation)
        val hoursTextView: TextView = view.findViewById(R.id.serviceHours)
        val descriptionTextView: TextView = view.findViewById(R.id.serviceDescription)
        val appointmentButton: Button = view.findViewById(R.id.appointment_button)
        mapImageView = view.findViewById(R.id.mapImageView)

        arguments?.getString(ARG_SERVICE_NAME)?.let { serviceName ->
            loadServiceDetails(serviceName)
        }

        arguments?.let {
            nameTextView.text = it.getString(ARG_SERVICE_NAME, "Название отсутствует")
            locationTextView.text = it.getString(ARG_SERVICE_LOCATION, "Расположение отсутствует")
            hoursTextView.text = it.getString(ARG_SERVICE_HOURS, "Часы работы отсутствуют")
            descriptionTextView.text = it.getString(ARG_SERVICE_DESCRIPTION, "Описание отсутствует")
            val mapImageUrl = it.getString(ARG_SERVICE_MAP_IMAGE_URL)

            if (!mapImageUrl.isNullOrEmpty()) {
                loadMapImage(mapImageUrl)
            } else {
                Log.e("ServiceDetailFragment", "URL изображения карты отсутствует")
            }
        }

//        mapView = view.findViewById(R.id.mapView)
//        initializeMapView()

        appointmentButton.setOnClickListener {
            fetchVehiclesFromServer()
        }

        return view
    }

    private fun loadMapImage(mapImageUrl: String) {
        Glide.with(this)
            .load(mapImageUrl)
            .placeholder(R.drawable.placeholder_image) // Изображение-заполнитель
            .error(R.drawable.error_image) // Изображение для ошибки
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("ServiceDetailFragment", "Ошибка загрузки изображения: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("ServiceDetailFragment", "Изображение карты успешно загружено")
                    return false
                }
            })
            .into(mapImageView)
    }


    private fun fetchVehiclesFromServer() {
        ApiClient.apiService.getCarsByUser(currentUserId).enqueue(object : Callback<List<RetrofitCar>> {
            override fun onResponse(call: Call<List<RetrofitCar>>, response: Response<List<RetrofitCar>>) {
                if (response.isSuccessful) {
                    val vehicles = response.body() ?: emptyList()
                    if (vehicles.isEmpty()) {
                        Toast.makeText(requireContext(), "Вы пока не добавили свой автомобиль", Toast.LENGTH_SHORT).show()
                    } else {
                        showVehicleSelectionDialog(vehicles)
                    }
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки автомобилей: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RetrofitCar>>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showVehicleSelectionDialog(vehicles: List<RetrofitCar>) {
        val vehicleNames = vehicles.map { "${it.brand} ${it.model} (${it.licensePlate})" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите автомобиль")
            .setItems(vehicleNames) { _, which ->
                val selectedVehicle = vehicles[which]
                showDatePickerDialog(selectedVehicle)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDatePickerDialog(vehicle: RetrofitCar) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                showTimePickerDialog(vehicle, calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog(vehicle: RetrofitCar, calendar: Calendar) {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                showProblemTypeDialog(vehicle, calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showProblemTypeDialog(vehicle: RetrofitCar, date: Date) {
        val problemTypes = arrayOf("Масляный сервис", "Ремонт подвески", "Замена шин", "Другое")
        val problemInput = EditText(requireContext())

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите проблему")
            .setItems(problemTypes) { _, which ->
                val selectedProblem = problemTypes[which]
                if (selectedProblem == "Другое") {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Опишите вашу проблему")
                        .setView(problemInput)
                        .setPositiveButton("ОК") { _, _ ->
                            val customProblem = problemInput.text.toString()
                            if (customProblem.isNotEmpty()) {
                                saveAppointment(vehicle, date, customProblem)
                            } else {
                                Toast.makeText(requireContext(), "Описание проблемы не может быть пустым", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                } else {
                    saveAppointment(vehicle, date, selectedProblem)
                }
            }
            .show()
    }

    private fun saveAppointment(vehicle: RetrofitCar, date: Date, problemDescription: String) {
        val serviceName = arguments?.getString(ARG_SERVICE_NAME) ?: "Неизвестный автосервис"

        val appointment = Appointment(
            id = 0,
            brand = vehicle.brand,
            model = vehicle.model,
            licensePlate = vehicle.licensePlate,
            date = date.time,
            problemDescription = problemDescription,
            serviceName = serviceName,
            userId = currentUserId // Добавляем userId
        )

        ApiClient.apiService.createAppointment(appointment).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Запись успешно создана!", Toast.LENGTH_SHORT).show()
                } else {
                    println("Ошибка создания записи: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                println("Ошибка сети: ${t.message}")
                Toast.makeText(context, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


//    private fun initializeMapView() {
//        val latitude = arguments?.getDouble(ARG_SERVICE_COORDINATES_LAT, 0.0) ?: 0.0
//        val longitude = arguments?.getDouble(ARG_SERVICE_COORDINATES_LNG, 0.0) ?: 0.0
//
//        if (latitude == 0.0 || longitude == 0.0) {
//            Toast.makeText(requireContext(), "Координаты недоступны", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val point = Point(latitude, longitude)
//        mapView.map.move(
//            CameraPosition(point, 15f, 0f, 0f)
//        )
//        mapView.map.mapObjects.addPlacemark(point).apply {
//            setIcon(ImageProvider.fromResource(requireContext(), R.drawable.baseline_place_24))
//        }
//    }


    private fun updateUI(service: ServiceEntity) {
        view?.findViewById<TextView>(R.id.serviceName)?.text = service.name
        view?.findViewById<TextView>(R.id.serviceLocation)?.text = service.location
        view?.findViewById<TextView>(R.id.serviceHours)?.text = service.hours
        view?.findViewById<TextView>(R.id.serviceDescription)?.text = service.description

        val mapImageUrl = service.mapImageUrl
        Log.d("ServiceDetailFragment", "URL для карты: $mapImageUrl")

        if (!mapImageUrl.isNullOrEmpty()) {
            loadMapImage(mapImageUrl)
        } else {
            Log.e("ServiceDetailFragment", "URL изображения карты отсутствует")
            mapImageView.setImageResource(R.drawable.error_image)
        }
    }


    private fun loadServiceDetails(serviceName: String) {
        ApiClient.apiService.getServices().enqueue(object : Callback<List<ServiceEntity>> {
            override fun onResponse(call: Call<List<ServiceEntity>>, response: Response<List<ServiceEntity>>) {
                if (response.isSuccessful) {
                    val services = response.body() ?: emptyList()
                    val service = services.find { it.name == serviceName }
                    if (service != null) {
                        Log.d("ServiceDetailFragment", "Загруженный сервис: ${service.name}, URL карты: ${service.mapImageUrl}")
                        updateUI(service)
                    } else {
                        Log.e("ServiceDetailFragment", "Сервис не найден для имени: $serviceName")
                        Toast.makeText(requireContext(), "Сервис не найден", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ServiceDetailFragment", "Ошибка загрузки сервиса: ${response.code()}")
                    Toast.makeText(requireContext(), "Ошибка загрузки сервиса: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ServiceEntity>>, t: Throwable) {
                Log.e("ServiceDetailFragment", "Ошибка сети: ${t.message}")
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
//        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
//        mapView.onStop()
    }
}
