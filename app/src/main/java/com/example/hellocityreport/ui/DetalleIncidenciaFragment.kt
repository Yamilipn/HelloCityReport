package com.example.hellocityreport.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hellocityreport.R
import com.example.hellocityreport.model.IncidenciasDummy

/** Pantalla de detalle: muestra la información de una incidencia seleccionada. */
class DetalleIncidenciaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_incidencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getInt("incidenciaId", -1) ?: -1
        val incidencia = IncidenciasDummy.porId(id)

        view.findViewById<TextView>(R.id.textDetalleTitulo).text =
            incidencia?.titulo ?: "Incidencia no encontrada"
        view.findViewById<TextView>(R.id.textDetalleEstado).text =
            incidencia?.estado ?: "-"
        view.findViewById<TextView>(R.id.textDetalleUbicacion).text =
            incidencia?.ubicacion ?: "-"
        view.findViewById<TextView>(R.id.textDetalleFecha).text =
            incidencia?.fecha ?: "-"
        view.findViewById<TextView>(R.id.textDetalleDescripcion).text =
            incidencia?.descripcion ?: "Revisa la lista e intenta de nuevo."
    }
}
