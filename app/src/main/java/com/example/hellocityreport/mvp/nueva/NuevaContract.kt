package com.example.hellocityreport.mvp.nueva

/** Contrato MVP de crear incidencia. */
interface NuevaContract {

    interface View {
        fun showFechaInicial(fecha: String)
        fun showEstados(estados: List<String>, seleccionado: String)
        fun showTituloError(message: String)
        fun showUbicacionError(message: String)
        fun showCreada()
        fun close()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onInit(fechaActual: String, estadoDefault: String, estados: List<String>)
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
            estadoFallback: String
        )
    }
}
