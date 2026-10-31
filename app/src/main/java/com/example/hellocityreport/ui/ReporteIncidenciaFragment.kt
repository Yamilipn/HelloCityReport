package com.example.hellocityreport.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hellocityreport.R
import com.example.hellocityreport.data.IncidenciaRepositoryImpl
import com.example.hellocityreport.mvp.reporte.ReporteContract
import com.example.hellocityreport.mvp.reporte.ReportePresenter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ReporteIncidenciaFragment : Fragment(), ReporteContract.View, OnMapReadyCallback {

    private var presenter: ReporteContract.Presenter? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var googleMap: GoogleMap? = null
    private var locationMarker: Marker? = null
    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService? = null
    private var mapFragment: SupportMapFragment? = null

    private var inputTitulo: TextInputEditText? = null
    private var inputDescripcion: TextInputEditText? = null
    private var inputUbicacion: TextInputEditText? = null
    private var inputFecha: TextInputEditText? = null
    private var inputEstado: AutoCompleteTextView? = null
    private var textCoords: android.widget.TextView? = null
    private var previewView: PreviewView? = null
    private var imageFoto: ImageView? = null

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                presenter?.let { /* noop */ }
                obtenerUbicacion()
            } else {
                presenter?.onPermisoUbicacionDenegado()
                Toast.makeText(requireContext(), getString(R.string.permiso_ubicacion_denegado), Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                iniciarCamara()
                Handler(Looper.getMainLooper()).postDelayed({ tomarFoto() }, 1000)
            } else {
                presenter?.onPermisoCamaraDenegado()
                Toast.makeText(requireContext(), getString(R.string.permiso_camara_denegado), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reporte_incidencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val presenter = ReportePresenter(IncidenciaRepositoryImpl())
        this.presenter = presenter
        presenter.attach(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        cameraExecutor = Executors.newSingleThreadExecutor()

        inputTitulo = view.findViewById(R.id.inputTitulo)
        inputDescripcion = view.findViewById(R.id.inputDescripcion)
        inputUbicacion = view.findViewById(R.id.inputUbicacion)
        inputFecha = view.findViewById(R.id.inputFecha)
        inputEstado = view.findViewById(R.id.inputEstado)
        textCoords = view.findViewById(R.id.textUbicacionCoords)
        previewView = view.findViewById(R.id.previewView)
        imageFoto = view.findViewById(R.id.imageFoto)

        val btnMiUbicacion = view.findViewById<MaterialButton>(R.id.btnMiUbicacion)
        val btnCapturar = view.findViewById<MaterialButton>(R.id.btnCapturar)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardar)

        // Mapa
        mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, mapFragment!!)
                .commit()
        }
        mapFragment?.getMapAsync(this)

        val estados = resources.getStringArray(R.array.estados_incidencia).toList()
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        presenter.onInit(
            fechaActual = hoy,
            estadoDefault = estados.first(),
            estados = estados
        )

        btnMiUbicacion.setOnClickListener { presenter.onMiUbicacionClicked() }
        btnCapturar.setOnClickListener { presenter.onCapturarFotoClicked() }
        btnGuardar.setOnClickListener {
            presenter.onGuardar(
                titulo = inputTitulo?.text?.toString().orEmpty(),
                descripcion = inputDescripcion?.text?.toString().orEmpty(),
                ubicacion = inputUbicacion?.text?.toString().orEmpty(),
                fecha = inputFecha?.text?.toString().orEmpty(),
                estado = inputEstado?.text?.toString().orEmpty(),
                tituloErrorMsg = getString(R.string.error_titulo_requerido),
                ubicacionErrorMsg = getString(R.string.error_ubicacion_requerida),
                descripcionFallback = getString(R.string.sin_descripcion),
                fechaFallback = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                estadoFallback = estados.first(),
                sinUbicacionMsg = getString(R.string.error_ubicacion_requerida),
                sinFotoMsg = getString(R.string.error_foto_requerida)
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        if (hasLocationPermission()) {
            try {
                map.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                // sin permiso, se ignora
            }
        }
    }

    override fun onDestroyView() {
        presenter?.detach()
        presenter = null
        cameraExecutor?.shutdown()
        cameraExecutor = null
        fusedLocationClient = null
        googleMap = null
        super.onDestroyView()
    }

    // --- View contract ---

    override fun showFechaInicial(fecha: String) {
        if (inputFecha?.text.isNullOrBlank()) {
            inputFecha?.setText(fecha)
        }
    }

    override fun showEstados(estados: List<String>, seleccionado: String) {
        inputEstado?.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estados)
        )
        if (inputEstado?.text.isNullOrBlank()) {
            inputEstado?.setText(seleccionado, false)
        }
    }

    override fun showUbicacion(lat: Double, lng: Double) {
        val pos = LatLng(lat, lng)
        val map = googleMap
        if (map != null) {
            try {
                if (hasLocationPermission()) map.isMyLocationEnabled = true
            } catch (e: SecurityException) { }
            if (locationMarker == null) {
                locationMarker = map.addMarker(MarkerOptions().position(pos).title(getString(R.string.mi_ubicacion)))
            } else {
                locationMarker?.position = pos
            }
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))
        }
    }

    override fun showUbicacionTexto(texto: String) {
        textCoords?.text = texto
    }

    override fun showFoto(uri: String) {
        imageFoto?.visibility = View.VISIBLE
        imageFoto?.setImageURI(Uri.parse(uri))
    }

    override fun showTituloError(message: String) {
        inputTitulo?.error = message
        inputTitulo?.requestFocus()
    }

    override fun showUbicacionError(message: String) {
        inputUbicacion?.error = message
        inputUbicacion?.requestFocus()
    }

    override fun showCreada() {
        Toast.makeText(requireContext(), getString(R.string.incidencia_creada), Toast.LENGTH_SHORT).show()
    }

    override fun close() {
        try { findNavController().popBackStack() } catch (e: Exception) { }
    }

    override fun enableGuardar(enabled: Boolean) {
        requireView().findViewById<MaterialButton>(R.id.btnGuardar).isEnabled = enabled
    }

    override fun requestLocationPermission() {
        if (hasLocationPermission()) {
            obtenerUbicacion()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    override fun requestCameraPermission() {
        if (hasCameraPermission()) {
            iniciarCamara()
            Handler(Looper.getMainLooper()).postDelayed({ tomarFoto() }, 1000)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun startLocationUpdates() {
        obtenerUbicacion()
    }

    override fun startCameraCapture() {
        tomarFoto()
    }

    // --- helpers ---

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun obtenerUbicacion() {
        if (!hasLocationPermission()) return
        try {
            fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                ?.addOnSuccessListener { loc ->
                    if (loc != null) {
                        presenter?.onUbicacionObtenida(loc.latitude, loc.longitude)
                    } else {
                        // Fallback a última ubicación conocida
                        fusedLocationClient?.lastLocation?.addOnSuccessListener { last ->
                            if (last != null) {
                                presenter?.onUbicacionObtenida(last.latitude, last.longitude)
                            }
                        }
                    }
                }
        } catch (e: SecurityException) {
            presenter?.onPermisoUbicacionDenegado()
        }
    }

    private fun iniciarCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView?.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, selector, preview, imageCapture)
                // Capturar automáticamente una foto al iniciar? No: esperar al botón.
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al iniciar cámara: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun tomarFoto() {
        val capture = imageCapture ?: return
        val photoFile = File(requireContext().cacheDir, "reporte_${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        capture.takePicture(
            output,
            cameraExecutor!!,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(photoFile).toString()
                    presenter?.onFotoCapturada(uri)
                }

                override fun onError(exc: ImageCaptureException) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Error al capturar: ${exc.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun findNavControllerSafe(): androidx.navigation.NavController? =
        try { findNavController() } catch (e: Exception) { null }
}
