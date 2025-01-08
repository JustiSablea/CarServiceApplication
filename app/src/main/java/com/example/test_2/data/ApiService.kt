import com.example.test_2.data.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // --- USERS --- //
    @GET("users/{email}")
    fun getUserByEmail(@Path("email") email: String): Call<User>

    @GET("users/{id}")
    fun getUserById(@Path("id") userId: Int): Call<User>

    @POST("users/")
    fun createUser(@Body user: User): Call<User>

    @PUT("users/{id}")
    fun updateUser(@Path("id") userId: Int, @Body user: User): Call<User>

    // --- CARS --- //
    @POST("cars/")
    fun addCar(@Body car: RetrofitCar): Call<RetrofitCar>

    @GET("cars/user/{userId}")
    fun getCarsByUser(@Path("userId") userId: Int): Call<List<RetrofitCar>>

    @PUT("cars/{id}")
    fun updateCar(@Path("id") carId: Int, @Body car: RetrofitCar): Call<RetrofitCar>

    @DELETE("cars/{id}")
    fun deleteCar(@Path("id") carId: Int): Call<Void>

    // --- APPOINTMENTS --- //
    @GET("appointments/user/{userId}")
    fun getAppointmentsByUser(@Path("userId") userId: Int): Call<List<Appointment>>

    @POST("appointments/")
    fun createAppointment(@Body appointment: Appointment): Call<Appointment>

    @DELETE("appointments/{id}")
    fun deleteAppointment(@Path("id") appointmentId: Int): Call<Void>

    // --- SERVICES --- //
    @GET("services/")
    fun getServices(): Call<List<ServiceEntity>>

    @GET("services/{id}")
    fun getServiceById(@Path("id") serviceId: Int): Call<ServiceEntity>

    @GET("brands/")
    fun getBrands(): Call<List<Brand>>

    @GET("models/brand/{brandId}")
    fun getModelsByBrand(@Path("brandId") brandId: Int): Call<List<Model>>
}
