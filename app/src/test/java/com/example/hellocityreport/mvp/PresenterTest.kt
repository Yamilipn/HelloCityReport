package com.example.hellocityreport.mvp

import com.example.hellocityreport.data.IncidenciaRepository
import com.example.hellocityreport.model.Incidencia
import com.example.hellocityreport.mvp.detalle.DetalleContract
import com.example.hellocityreport.mvp.detalle.DetallePresenter
import com.example.hellocityreport.mvp.lista.ListaContract
import com.example.hellocityreport.mvp.lista.ListaPresenter
import com.example.hellocityreport.mvp.nueva.NuevaContract
import com.example.hellocityreport.mvp.nueva.NuevaPresenter
import com.example.hellocityreport.mvp.reporte.ReporteContract
import com.example.hellocityreport.mvp.reporte.ReportePresenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeRepository : IncidenciaRepository {
    private val items = mutableListOf(
        Incidencia(1, "Bache", "Desc", "Av. 1", "2026-09-10", "Pendiente"),
        Incidencia(2, "Fuga", "Desc 2", "Calle 2", "2026-09-11", "En proceso")
    )

    override fun getAll(): List<Incidencia> = items.toList()

    override fun getById(id: Int): Incidencia? = items.find { it.id == id }

    override fun add(
        titulo: String,
        descripcion: String,
        ubicacion: String,
        fecha: String,
        estado: String,
        latitud: Double?,
        longitud: Double?,
        fotoUri: String?
    ): Incidencia {
        val nueva = Incidencia(3, titulo, descripcion, ubicacion, fecha, estado, latitud, longitud, fotoUri)
        items.add(0, nueva)
        return nueva
    }
}

class ListaPresenterTest {

    @Test
    fun onViewReady_muestraLista() {
        val repo = FakeRepository()
        val presenter = ListaPresenter(repo)
        var mostrados: List<Incidencia>? = null
        val view = object : ListaContract.View {
            override fun showIncidencias(items: List<Incidencia>) {
                mostrados = items
            }

            override fun navigateToDetalle(id: Int) = Unit
            override fun navigateToNueva() = Unit
            override fun navigateToReporte() = Unit
        }
        presenter.attach(view)
        presenter.onViewReady()
        assertEquals(2, mostrados?.size)
    }

    @Test
    fun onIncidenciaClicked_navegaADetalle() {
        val presenter = ListaPresenter(FakeRepository())
        var destino = -1
        val view = object : ListaContract.View {
            override fun showIncidencias(items: List<Incidencia>) = Unit
            override fun navigateToDetalle(id: Int) {
                destino = id
            }

            override fun navigateToNueva() = Unit
            override fun navigateToReporte() = Unit
        }
        presenter.attach(view)
        presenter.onIncidenciaClicked(2)
        assertEquals(2, destino)
    }
}

class DetallePresenterTest {

    @Test
    fun loadDetalle_existente_muestraIncidencia() {
        val presenter = DetallePresenter(FakeRepository())
        var titulo: String? = null
        val view = object : DetalleContract.View {
            override fun showIncidencia(incidencia: Incidencia) {
                titulo = incidencia.titulo
            }

            override fun showNotFound() = Unit
        }
        presenter.attach(view)
        presenter.loadDetalle(1)
        assertEquals("Bache", titulo)
    }

    @Test
    fun loadDetalle_inexistente_muestraNotFound() {
        val presenter = DetallePresenter(FakeRepository())
        var notFound = false
        val view = object : DetalleContract.View {
            override fun showIncidencia(incidencia: Incidencia) = Unit
            override fun showNotFound() {
                notFound = true
            }
        }
        presenter.attach(view)
        presenter.loadDetalle(999)
        assertTrue(notFound)
    }
}

class NuevaPresenterTest {

    private class ViewRecorder : NuevaContract.View {
        var tituloError: String? = null
        var ubicacionError: String? = null
        var creada = false
        var cerrada = false

        override fun showFechaInicial(fecha: String) = Unit
        override fun showEstados(estados: List<String>, seleccionado: String) = Unit
        override fun showTituloError(message: String) {
            tituloError = message
        }

        override fun showUbicacionError(message: String) {
            ubicacionError = message
        }

        override fun showCreada() {
            creada = true
        }

        override fun close() {
            cerrada = true
        }
    }

    @Test
    fun onGuardar_tituloVacio_muestraError() {
        val presenter = NuevaPresenter(FakeRepository())
        val view = ViewRecorder()
        presenter.attach(view)
        presenter.onGuardar(
            titulo = "  ",
            descripcion = "d",
            ubicacion = "u",
            fecha = "2026-09-16",
            estado = "Pendiente",
            tituloErrorMsg = "req",
            ubicacionErrorMsg = "req-ubi",
            descripcionFallback = "Sin descripción",
            fechaFallback = "2026-09-16",
            estadoFallback = "Pendiente"
        )
        assertEquals("req", view.tituloError)
    }

    @Test
    fun onGuardar_valido_guardaYCierra() {
        val repo = FakeRepository()
        val presenter = NuevaPresenter(repo)
        val view = ViewRecorder()
        presenter.attach(view)
        presenter.onGuardar(
            titulo = "Poste",
            descripcion = "",
            ubicacion = "Av. 3",
            fecha = "",
            estado = "",
            tituloErrorMsg = "req",
            ubicacionErrorMsg = "req-ubi",
            descripcionFallback = "Sin descripción",
            fechaFallback = "2026-09-16",
            estadoFallback = "Pendiente"
        )
        assertTrue(view.creada)
        assertTrue(view.cerrada)
        assertEquals(3, repo.getAll().size)
    }
}

class ReportePresenterTest {

    private class ViewRecorder : ReporteContract.View {
        var guardado = false
        var cerrado = false
        var guardadoHabilitado = false
        override fun showFechaInicial(fecha: String) = Unit
        override fun showEstados(estados: List<String>, seleccionado: String) = Unit
        override fun showUbicacion(lat: Double, lng: Double) = Unit
        override fun showUbicacionTexto(texto: String) = Unit
        override fun showFoto(uri: String) = Unit
        override fun showTituloError(message: String) = Unit
        override fun showUbicacionError(message: String) = Unit
        override fun showCreada() { guardado = true }
        override fun close() { cerrado = true }
        override fun enableGuardar(enabled: Boolean) { guardadoHabilitado = enabled }
        override fun requestLocationPermission() = Unit
        override fun requestCameraPermission() = Unit
        override fun startLocationUpdates() = Unit
        override fun startCameraCapture() = Unit
    }

    @Test
    fun onInit_deshabilitaGuardar() {
        val presenter = ReportePresenter(FakeRepository())
        val view = ViewRecorder()
        presenter.attach(view)
        presenter.onInit("2026-09-16", "Pendiente", listOf("Pendiente"))
        org.junit.Assert.assertFalse(view.guardadoHabilitado)
    }

    @Test
    fun ubicacionYFoto_habilitanGuardar() {
        val presenter = ReportePresenter(FakeRepository())
        val view = ViewRecorder()
        presenter.attach(view)
        presenter.onInit("2026-09-16", "Pendiente", listOf("Pendiente"))
        presenter.onUbicacionObtenida(19.43, -99.13)
        presenter.onFotoCapturada("file:///tmp/foto.jpg")
        org.junit.Assert.assertTrue(view.guardadoHabilitado)
    }

    @Test
    fun onGuardar_valido_guardaYCierra() {
        val repo = FakeRepository()
        val presenter = ReportePresenter(repo)
        val view = ViewRecorder()
        presenter.attach(view)
        presenter.onInit("2026-09-16", "Pendiente", listOf("Pendiente"))
        presenter.onUbicacionObtenida(19.43, -99.13)
        presenter.onFotoCapturada("file:///tmp/foto.jpg")
        presenter.onGuardar(
            titulo = "Bache",
            descripcion = "",
            ubicacion = "Av. X",
            fecha = "",
            estado = "",
            tituloErrorMsg = "req",
            ubicacionErrorMsg = "req-ubi",
            descripcionFallback = "Sin descripción",
            fechaFallback = "2026-09-16",
            estadoFallback = "Pendiente",
            sinUbicacionMsg = "sin-ubi",
            sinFotoMsg = "sin-foto"
        )
        org.junit.Assert.assertTrue(view.guardado)
        org.junit.Assert.assertTrue(view.cerrado)
        org.junit.Assert.assertEquals(3, repo.getAll().size)
    }
}
