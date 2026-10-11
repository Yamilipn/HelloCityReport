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
import com.example.hellocityreport.data.IncidenciaRepositoryImpl
import com.example.hellocityreport.mvp.nueva.NuevaContract
import com.example.hellocityreport.mvp.nueva.NuevaPresenter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Vista de crear incidencia en MVP: widgets + recursos, lógica en [NuevaPresenter]. */
class NuevaIncidenciaFragment : Fragment(), NuevaContract.View {

    private var presenter: NuevaContract.Presenter? = null

    private var inputTitulo: TextInputEditText? = null
    private var inputDescripcion: TextInputEditText? = null
    private var inputUbicacion: TextInputEditText? = null
    private var inputFecha: TextInputEditText? = null
    private var inputEstado: AutoCompleteTextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nueva_incidencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val presenter = NuevaPresenter(IncidenciaRepositoryImpl())
        this.presenter = presenter
        presenter.attach(this)

        inputTitulo = view.findViewById(R.id.inputTitulo)
        inputDescripcion = view.findViewById(R.id.inputDescripcion)
        inputUbicacion = view.findViewById(R.id.inputUbicacion)
        inputFecha = view.findViewById(R.id.inputFecha)
        inputEstado = view.findViewById(R.id.inputEstado)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardar)

        val estados = resources.getStringArray(R.array.estados_incidencia).toList()
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        presenter.onInit(
            fechaActual = hoy,
            estadoDefault = estados.first(),
            estados = estados
        )

        btnGuardar.setOnClickListener {
            presenter.onGuardar(
                titulo = inputTitulo?.text?.toString().orEmpty(),
                descripcion = inputDescripcion?.text?.toString().orEmpty(),
                ubicacion = inputUbicacion?.text?.toString().orEmpty(),
                fecha = inputFecha?.text?.toString().orEmpty(),
                estado = inputEstado?.text?.toString().orEmpty(),
                tituloErrorMsg = getString(R.string.error_titulo_requerido),
                ubicacionErrorMsg = getString(R.string.error_ubicacion_requerida),
                descripcionFallback = getString(R.string.sin_descripcion),
                fechaFallback = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                estadoFallback = estados.first()
            )
        }
    }

    override fun onDestroyView() {
        presenter?.detach()
        presenter = null
        inputTitulo = null
        inputDescripcion = null
        inputUbicacion = null
        inputFecha = null
        inputEstado = null
        super.onDestroyView()
    }

    override fun showFechaInicial(fecha: String) {
        if (inputFecha?.text.isNullOrBlank()) {
            inputFecha?.setText(fecha)
        }
    }

    override fun showEstados(estados: List<String>, seleccionado: String) {
        inputEstado?.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estados)
        )
        if (inputEstado?.text.isNullOrBlank()) {
            inputEstado?.setText(seleccionado, false)
        }
    }

    override fun showTituloError(message: String) {
        inputTitulo?.error = message
        inputTitulo?.requestFocus()
    }

    override fun showUbicacionError(message: String) {
        inputUbicacion?.error = message
        inputUbicacion?.requestFocus()
    }

    override fun showCreada() {
        Toast.makeText(requireContext(), getString(R.string.incidencia_creada), Toast.LENGTH_SHORT)
            .show()
    }

    override fun close() {
        findNavController().popBackStack()
    }
}
