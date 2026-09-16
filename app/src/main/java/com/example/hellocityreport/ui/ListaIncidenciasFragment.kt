package com.example.hellocityreport.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hellocityreport.R
import com.example.hellocityreport.model.IncidenciasDummy

/** Pantalla principal: lista dinámica de incidencias con RecyclerView. */
class ListaIncidenciasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_incidencias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerIncidencias)
        val adapter = IncidenciaAdapter { incidencia ->
            findNavController().navigate(
                R.id.action_lista_to_detalle,
                bundleOf("incidenciaId" to incidencia.id)
            )
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        adapter.submitList(IncidenciasDummy.lista)
    }
}
