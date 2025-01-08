package com.example.test_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_2.data.ServiceEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ServiceListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Передаём обработчик клика
        serviceAdapter = ServiceAdapter(emptyList()) { service ->
            openServiceDetailFragment(service)
        }
        recyclerView.adapter = serviceAdapter

        loadServices()

        return view
    }

    private fun loadServices() {
        ApiClient.apiService.getServices().enqueue(object : Callback<List<ServiceEntity>> {
            override fun onResponse(call: Call<List<ServiceEntity>>, response: Response<List<ServiceEntity>>) {
                if (response.isSuccessful) {
                    val services = response.body() ?: emptyList()
                    serviceAdapter.updateData(services)
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки данных: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ServiceEntity>>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun openServiceDetailFragment(service: ServiceEntity) {
        val fragment = ServiceDetailFragment.newInstance(service)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}


