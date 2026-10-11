package com.example.hellocityreport.mvp.lista

import com.example.hellocityreport.model.Incidencia

/**
 * Contrato MVP de la lista (Home).
 * La Vista es pasiva: solo renderiza y delega eventos al Presenter.
 */
interface ListaContract {

    interface View {
        fun showIncidencias(items: List<Incidencia>)
        fun navigateToDetalle(id: Int)
        fun navigateToNueva()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onViewReady()
        fun onResume()
        fun onIncidenciaClicked(id: Int)
        fun onNuevaClicked()
    }
}
