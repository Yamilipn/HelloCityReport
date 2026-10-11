package com.example.hellocityreport.data

import com.example.hellocityreport.model.Incidencia
import com.example.hellocityreport.model.IncidenciasDummy

/**
 * Modelo (M en MVP). Abstrae el origen de datos para que los
 * Presenters no dependan del singleton en memoria directamente.
 */
interface IncidenciaRepository {
    fun getAll(): List<Incidencia>
    fun getById(id: Int): Incidencia?
    fun add(
        titulo: String,
        descripcion: String,
        ubicacion: String,
        fecha: String,
        estado: String
    ): Incidencia
}

/** Implementación por defecto sobre [IncidenciasDummy] (Fase 1, sin backend). */
class IncidenciaRepositoryImpl : IncidenciaRepository {
    override fun getAll(): List<Incidencia> = IncidenciasDummy.lista

    override fun getById(id: Int): Incidencia? = IncidenciasDummy.porId(id)

    override fun add(
        titulo: String,
        descripcion: String,
        ubicacion: String,
        fecha: String,
        estado: String
    ): Incidencia = IncidenciasDummy.agregar(
        titulo = titulo,
        descripcion = descripcion,
        ubicacion = ubicacion,
        fecha = fecha,
        estado = estado
    )
}
