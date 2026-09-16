package com.example.hellocityreport

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

/**
 * Activity contenedora única.
 * Aloja el NavHostFragment definido en activity_main.xml y delega
 * la navegación a los Fragments mediante el NavGraph.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: return super.onSupportNavigateUp()
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
