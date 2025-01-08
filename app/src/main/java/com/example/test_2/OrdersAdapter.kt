package com.example.test_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_2.data.Appointment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersAdapter(
    private val orders: List<Appointment>,
    private val onItemClick: (Appointment) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val brandModelView: TextView = itemView.findViewById(R.id.vehicleInfoTextView)
        private val dateView: TextView = itemView.findViewById(R.id.orderDateTextView)
        private val problemView: TextView = itemView.findViewById(R.id.orderProblemTextView)
        private val serviceNameView: TextView = itemView.findViewById(R.id.serviceNameTextView)

        fun bind(appointment: Appointment) {
            brandModelView.text = "${appointment.brand} ${appointment.model} (${appointment.licensePlate})"
            val dateString = formatDate(appointment.date)
            dateView.text = "Дата: $dateString"
            problemView.text = "Проблема: ${appointment.problemDescription}"
            serviceNameView.text = "Сервис: ${appointment.serviceName}"


            itemView.setOnClickListener {
                onItemClick(appointment)
            }

        }
        private fun formatDate(epochMillis: Long): String {
            val date = Date(epochMillis)
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return sdf.format(date)
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount() = orders.size
}