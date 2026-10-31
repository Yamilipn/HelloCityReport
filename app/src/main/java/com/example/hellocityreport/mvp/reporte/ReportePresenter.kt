package com.example.hellocityreport.mvp.reporte

import com.example.hellocityreport.data.IncidenciaRepository

/**
 * Presenter de la pantalla de reporte. Centraliza validación, estado de
 * ubicación/foto y alta de la incidencia.
 */
class ReportePresenter(
    private val repository: IncidenciaRepository
) : ReporteContract.Presenter {

    private var view: ReporteContract.View? = null

    private var latitud: Double? = null
    private var longitud: Double? = null
    private var fotoUri: String? = null

    override fun attach(view: ReporteContract.View) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    override fun onInit(fechaActual: String, estadoDefault: String, estados: List<String>) {
        view?.showFechaInicial(fechaActual)
        view?.showEstados(estados, estadoDefault)
        actualizarBotonGuardar()
    }

    override fun onMiUbicacionClicked() {
        view?.requestLocationPermission()
    }

    override fun onUbicacionObtenida(lat: Double, lng: Double) {
        latitud = lat
        longitud = lng
        view?.showUbicacion(lat, lng)
        view?.showUbicacionTexto("$lat, $lng")
        actualizarBotonGuardar()
    }

    override fun onPermisoUbicacionDenegado() {
        // Sin permiso no se puede obtener ubicación; el botón sigue deshabilitado.
        actualizarBotonGuardar()
    }

    override fun onCapturarFotoClicked() {
        view?.requestCameraPermission()
    }

    override fun onFotoCapturada(uri: String) {
        fotoUri = uri
        view?.showFoto(uri)
        actualizarBotonGuardar()
    }

    override fun onPermisoCamaraDenegado() {
        actualizarBotonGuardar()
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
        estadoFallback: String,
        sinUbicacionMsg: String,
        sinFotoMsg: String
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
        if (latitud == null || longitud == null) {
            view?.showUbicacionError(sinUbicacionMsg)
            return
        }
        if (fotoUri == null) {
            // No bloquea guardado, pero informa; según reglas del wireframe se requiere foto.
            view?.showUbicacionError(sinFotoMsg)
            return
        }

        repository.add(
            titulo = tituloLimpio,
            descripcion = descripcion.trim().ifBlank { descripcionFallback },
            ubicacion = ubicacionLimpia,
            fecha = fecha.trim().ifBlank { fechaFallback },
            estado = estado.trim().ifBlank { estadoFallback },
            latitud = latitud,
            longitud = longitud,
            fotoUri = fotoUri
        )
        view?.showCreada()
        view?.close()
    }

    private fun actualizarBotonGuardar() {
        val enabled = latitud != null && longitud != null && fotoUri != null
        view?.enableGuardar(enabled)
    }
}
