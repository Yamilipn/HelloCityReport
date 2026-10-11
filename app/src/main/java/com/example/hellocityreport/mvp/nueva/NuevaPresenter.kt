package com.example.hellocityreport.mvp.nueva

import com.example.hellocityreport.data.IncidenciaRepository

/**
 * Presenter de crear incidencia. Centraliza validación y alta,
 * para que el Fragment solo maneje widgets y recursos.
 */
class NuevaPresenter(
    private val repository: IncidenciaRepository
) : NuevaContract.Presenter {

    private var view: NuevaContract.View? = null

    override fun attach(view: NuevaContract.View) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    override fun onInit(fechaActual: String, estadoDefault: String, estados: List<String>) {
        view?.showFechaInicial(fechaActual)
        view?.showEstados(estados, estadoDefault)
    }

    override fun onGuardar(
        titulo: String,
        descripcion: String,
        ubicacion: String,
        fecha: String,
        estado: String,
        tituloErrorMsg: String,
        ubicacionErrorMsg: String,
        descripcionFallback: String,
        fechaFallback: String,
        estadoFallback: String
    ) {
        val tituloLimpio = titulo.trim()
        val ubicacionLimpia = ubicacion.trim()

        if (tituloLimpio.isBlank()) {
            view?.showTituloError(tituloErrorMsg)
            return
        }
        if (ubicacionLimpia.isBlank()) {
            view?.showUbicacionError(ubicacionErrorMsg)
            return
        }

        repository.add(
            titulo = tituloLimpio,
            descripcion = descripcion.trim().ifBlank { descripcionFallback },
            ubicacion = ubicacionLimpia,
            fecha = fecha.trim().ifBlank { fechaFallback },
            estado = estado.trim().ifBlank { estadoFallback }
        )
        view?.showCreada()
        view?.close()
    }
}
