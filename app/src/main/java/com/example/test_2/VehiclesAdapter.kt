import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_2.R
import com.example.test_2.data.Car

class VehiclesAdapter(
    private var vehicles: List<Car>,
    private val onEditClick: (Car) -> Unit
) : RecyclerView.Adapter<VehiclesAdapter.VehicleViewHolder>() {

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val brandModelTextView: TextView = itemView.findViewById(R.id.brandModelTextView)
        private val licensePlateTextView: TextView = itemView.findViewById(R.id.licensePlateTextView)
        private val colorTextView: TextView = itemView.findViewById(R.id.colorTextView)
        private val productionYearTextView: TextView = itemView.findViewById(R.id.productionYearTextView)

        fun bind(vehicle: Car) {
            // Логирование данных
            println("Привязываем автомобиль: $vehicle")

            brandModelTextView.text = "${vehicle.brand} ${vehicle.model}"
            licensePlateTextView.text = "Госномер: ${vehicle.licensePlate ?: "Не указан"}"
            colorTextView.text = "Цвет: ${vehicle.color ?: "Не указан"}"
            productionYearTextView.text = "Год выпуска: ${vehicle.productionYear.takeIf { it > 0 } ?: "Не указан"}"

            itemView.setOnClickListener {
                onEditClick(vehicle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int = vehicles.size

    fun submitList(newVehicles: List<Car>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}
