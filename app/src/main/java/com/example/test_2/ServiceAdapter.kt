package com.example.test_2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_2.data.ServiceEntity

class ServiceAdapter(
    private var services: List<ServiceEntity>,
    private val onServiceClick: (ServiceEntity) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.serviceName)
        val detailsTextView: TextView = itemView.findViewById(R.id.serviceDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]

        // Логируем описание
        println("Сервис: ${service.name}, Описание: ${service.description}")

        holder.nameTextView.text = service.name
        holder.detailsTextView.text = service.description ?: "Описание отсутствует"

        holder.itemView.setOnClickListener {
            onServiceClick(service)
        }
    }


    override fun getItemCount(): Int = services.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newServices: List<ServiceEntity>) {
        services = newServices
        notifyDataSetChanged()
    }
}
