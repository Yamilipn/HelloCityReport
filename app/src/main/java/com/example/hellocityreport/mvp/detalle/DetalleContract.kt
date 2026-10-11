package com.example.hellocityreport.mvp.detalle

import com.example.hellocityreport.model.Incidencia

/** Contrato MVP del detalle. */
interface DetalleContract {

    interface View {
        fun showIncidencia(incidencia: Incidencia)
        fun showNotFound()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun loadDetalle(id: Int)
    }
}
