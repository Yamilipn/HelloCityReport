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
import com.example.hellocityreport.data.IncidenciaRepositoryImpl
import com.example.hellocityreport.model.Incidencia
import com.example.hellocityreport.mvp.lista.ListaContract
import com.example.hellocityreport.mvp.lista.ListaPresenter

/** Vista de la lista (Home) en MVP: solo renderiza y delega al [ListaPresenter]. */
class ListaIncidenciasFragment : Fragment(), ListaContract.View {

    private var adapter: IncidenciaAdapter? = null
    private var presenter: ListaContract.Presenter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_incidencias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val presenter = ListaPresenter(IncidenciaRepositoryImpl())
        this.presenter = presenter
        presenter.attach(this)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerIncidencias)
        val adapter = IncidenciaAdapter { incidencia ->
            presenter.onIncidenciaClicked(incidencia.id)
        }.also { this.adapter = it }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<View>(R.id.fabNueva).setOnClickListener {
            presenter.onNuevaClicked()
        }

        presenter.onViewReady()
    }

    override fun onResume() {
        super.onResume()
        presenter?.onResume()
    }

    override fun onDestroyView() {
        presenter?.detach()
        presenter = null
        adapter = null
        super.onDestroyView()
    }

    override fun showIncidencias(items: List<Incidencia>) {
        adapter?.submitList(items)
    }

    override fun navigateToDetalle(id: Int) {
        findNavController().navigate(
            R.id.action_lista_to_detalle,
            bundleOf("incidenciaId" to id)
        )
    }

    override fun navigateToNueva() {
        findNavController().navigate(R.id.action_lista_to_nueva)
    }
}
