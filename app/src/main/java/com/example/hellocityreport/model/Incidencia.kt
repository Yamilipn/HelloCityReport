package com.example.hellocityreport.model

/**
 * Representa una incidencia dentro de la aplicación.
 *
 * @param id Identificador único de la incidencia.
 * @param titulo Título corto de la incidencia.
 * @param descripcion Descripción detallada de lo reportado.
 * @param ubicacion Lugar donde ocurrió la incidencia.
 * @param fecha Fecha del reporte en formato texto (ej. "2026-09-16").
 * @param estado Estado actual: Pendiente, En proceso, Resuelta.
 */
data class Incidencia(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val ubicacion: String,
    val fecha: String,
    val estado: String
)

/** Datos de ejemplo para mostrar la lista dinámica mientras no hay backend. */
object IncidenciasDummy {
    val lista: List<Incidencia> = listOf(
        Incidencia(1, "Bache en Av. Principal", "Bache profundo que dificulta el paso vehicular.", "Av. Principal #123", "2026-09-10", "Pendiente"),
        Incidencia(2, "Alumbrado apagado", "Luminaria fundida en el parque central.", "Parque Central", "2026-09-11", "En proceso"),
        Incidencia(3, "Fuga de agua", "Fuga constante en la banqueta frente al mercado.", "Calle Hidalgo #45", "2026-09-12", "Pendiente"),
        Incidencia(4, "Basura acumulada", "Contenedor desbordado desde hace 3 días.", "Col. Centro", "2026-09-12", "Pendiente"),
        Incidencia(5, "Semáforo descompuesto", "Semáforo intermitente en cruce peligroso.", "Cruce Juárez y Morelos", "2026-09-13", "En proceso"),
        Incidencia(6, "Banqueta rota", "Tramo de banqueta levantada, riesgo para peatones.", "Calle 16 de Septiembre", "2026-09-14", "Resuelta"),
        Incidencia(7, "Grafiti en escuela", "Pinta en la fachada de la primaria.", "Primaria Benito Juárez", "2026-09-15", "Pendiente"),
        Incidencia(8, "Poste inclinado", "Poste de luz inclinado tras la tormenta.", "Av. Universidad #200", "2026-09-16", "En proceso")
    )

    fun porId(id: Int): Incidencia? = lista.find { it.id == id }
}
