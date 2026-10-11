package com.example.hellocityreport.mvp.detalle

import com.example.hellocityreport.data.IncidenciaRepository

/** Presenter del detalle: resuelve el id contra el repositorio. */
class DetallePresenter(
    private val repository: IncidenciaRepository
) : DetalleContract.Presenter {

    private var view: DetalleContract.View? = null

    override fun attach(view: DetalleContract.View) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    override fun loadDetalle(id: Int) {
        val incidencia = repository.getById(id)
        if (incidencia != null) {
            view?.showIncidencia(incidencia)
        } else {
            view?.showNotFound()
        }
    }
}
