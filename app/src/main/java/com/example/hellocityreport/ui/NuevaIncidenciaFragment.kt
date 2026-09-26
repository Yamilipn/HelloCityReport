package com.example.hellocityreport.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hellocityreport.R
import com.example.hellocityreport.model.IncidenciasDummy
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pantalla para crear una nueva incidencia y volver a la lista. */
class NuevaIncidenciaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nueva_incidencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputTitulo = view.findViewById<TextInputEditText>(R.id.inputTitulo)
        val inputDescripcion = view.findViewById<TextInputEditText>(R.id.inputDescripcion)
        val inputUbicacion = view.findViewById<TextInputEditText>(R.id.inputUbicacion)
        val inputFecha = view.findViewById<TextInputEditText>(R.id.inputFecha)
        val inputEstado = view.findViewById<AutoCompleteTextView>(R.id.inputEstado)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardar)

        if (inputFecha.text.isNullOrBlank()) {
            val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            inputFecha.setText(hoy)
        }

        val estados = resources.getStringArray(R.array.estados_incidencia).toList()
        inputEstado.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estados)
        )
        if (inputEstado.text.isNullOrBlank()) inputEstado.setText(estados.first(), false)

        btnGuardar.setOnClickListener {
            val titulo = inputTitulo.text?.toString()?.trim().orEmpty()
            val descripcion = inputDescripcion.text?.toString()?.trim().orEmpty()
            val ubicacion = inputUbicacion.text?.toString()?.trim().orEmpty()
            val fecha = inputFecha.text?.toString()?.trim().orEmpty()
            val estado = inputEstado.text?.toString()?.trim().orEmpty().ifBlank { estados.first() }

            if (titulo.isBlank()) {
                inputTitulo.error = getString(R.string.error_titulo_requerido)
                inputTitulo.requestFocus()
                return@setOnClickListener
            }
            if (ubicacion.isBlank()) {
                inputUbicacion.error = getString(R.string.error_ubicacion_requerida)
                inputUbicacion.requestFocus()
                return@setOnClickListener
            }

            val nueva = IncidenciasDummy.agregar(
                titulo = titulo,
                descripcion = descripcion.ifBlank { getString(R.string.sin_descripcion) },
                ubicacion = ubicacion,
                fecha = fecha.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) },
                estado = estado
            )

            Toast.makeText(requireContext(), getString(R.string.incidencia_creada), Toast.LENGTH_SHORT).show()
            // Volver a Home; onResume de la lista recarga el RecyclerView.
            findNavController().popBackStack()
        }
    }
}
