package com.example.hellocityreport.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hellocityreport.R
import com.example.hellocityreport.data.IncidenciaRepositoryImpl
import com.example.hellocityreport.model.Incidencia
import com.example.hellocityreport.mvp.detalle.DetalleContract
import com.example.hellocityreport.mvp.detalle.DetallePresenter

/** Vista del detalle en MVP: muestra lo que el Presenter resuelve. */
class DetalleIncidenciaFragment : Fragment(), DetalleContract.View {

    private var presenter: DetalleContract.Presenter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_incidencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val presenter = DetallePresenter(IncidenciaRepositoryImpl())
        this.presenter = presenter
        presenter.attach(this)

        val id = arguments?.getInt("incidenciaId", -1) ?: -1
        presenter.loadDetalle(id)
    }

    override fun onDestroyView() {
        presenter?.detach()
        presenter = null
        super.onDestroyView()
    }

    override fun showIncidencia(incidencia: Incidencia) {
        view?.findViewById<TextView>(R.id.textDetalleTitulo)?.text = incidencia.titulo
        view?.findViewById<TextView>(R.id.textDetalleEstado)?.text = incidencia.estado
        view?.findViewById<TextView>(R.id.textDetalleUbicacion)?.text = incidencia.ubicacion
        view?.findViewById<TextView>(R.id.textDetalleFecha)?.text = incidencia.fecha
        view?.findViewById<TextView>(R.id.textDetalleDescripcion)?.text = incidencia.descripcion
    }

    override fun showNotFound() {
        view?.findViewById<TextView>(R.id.textDetalleTitulo)?.text =
            getString(R.string.incidencia_no_encontrada)
        view?.findViewById<TextView>(R.id.textDetalleEstado)?.text = "-"
        view?.findViewById<TextView>(R.id.textDetalleUbicacion)?.text = "-"
        view?.findViewById<TextView>(R.id.textDetalleFecha)?.text = "-"
        view?.findViewById<TextView>(R.id.textDetalleDescripcion)?.text =
            getString(R.string.incidencia_reintentar)
    }
}
