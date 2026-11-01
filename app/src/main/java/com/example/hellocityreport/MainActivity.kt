package com.example.hellocityreport

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors

/**
 * Activity contenedora única (Fase 2: MVP + Material 3).
 * - Colores dinámicos Material You en Android 12+ con fallback M3.
 * - TopAppBar + BottomNavigationView conectados al NavController.
 * - Sin lógica de negocio: solo navegación.
 */
class MainActivity : AppCompatActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* noop */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Activa colores dinámicos si el dispositivo los soporta (S+).
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Solicita permiso de notificaciones en Android 13+ (si no está concedido).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Destinos top-level: no muestran Up y están en el BottomNav.
        val topLevel = setOf(
            R.id.listaIncidenciasFragment,
            R.id.reporteIncidenciaFragment
        )
        val appBarConfig = AppBarConfiguration(topLevel)

        topAppBar.setupWithNavController(navController, appBarConfig)
        bottomNav.setupWithNavController(navController)

        // Oculta el BottomNav en detalle para dar foco al contenido.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility =
                if (destination.id == R.id.detalleIncidenciaFragment) View.GONE else View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: return super.onSupportNavigateUp()
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
