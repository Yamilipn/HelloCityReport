package com.example.hellocityreport.mvp.lista

import com.example.hellocityreport.data.IncidenciaRepository

/**
 * Presenter de la lista. Contiene toda la lógica de presentación;
 * el Fragment no accede al repositorio directamente.
 */
class ListaPresenter(
    private val repository: IncidenciaRepository
) : ListaContract.Presenter {

    private var view: ListaContract.View? = null

    override fun attach(view: ListaContract.View) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    override fun onViewReady() {
        view?.showIncidencias(repository.getAll())
    }

    override fun onResume() {
        // Recarga por si NuevaIncidencia agregó un item.
        view?.showIncidencias(repository.getAll())
    }

    override fun onIncidenciaClicked(id: Int) {
        view?.navigateToDetalle(id)
    }

    override fun onNuevaClicked() {
        view?.navigateToNueva()
    }
}
