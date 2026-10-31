package com.example.hellocityreport.mvp.reporte

import com.example.hellocityreport.model.Incidencia

/** Contrato MVP de la pantalla de reporte (ubicación + foto + datos). */
interface ReporteContract {

    interface View {
        fun showFechaInicial(fecha: String)
        fun showEstados(estados: List<String>, seleccionado: String)
        fun showUbicacion(lat: Double, lng: Double)
        fun showUbicacionTexto(texto: String)
        fun showFoto(uri: String)
        fun showTituloError(message: String)
        fun showUbicacionError(message: String)
        fun showCreada()
        fun close()
        fun enableGuardar(enabled: Boolean)
        fun requestLocationPermission()
        fun requestCameraPermission()
        fun startLocationUpdates()
        fun startCameraCapture()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onInit(fechaActual: String, estadoDefault: String, estados: List<String>)
        fun onMiUbicacionClicked()
        fun onUbicacionObtenida(lat: Double, lng: Double)
        fun onPermisoUbicacionDenegado()
        fun onCapturarFotoClicked()
        fun onFotoCapturada(uri: String)
        fun onPermisoCamaraDenegado()
        fun onGuardar(
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
        )
    }
}
