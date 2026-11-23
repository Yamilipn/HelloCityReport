package com.example.hellocityreport.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

/**
 * Punto único de acceso a los SDK de Firebase.
 *
 * Las APIs Kotlin (antes en módulos -ktx) viven ahora en los módulos principales
 * desde BoM v32.5.0+, por eso los imports son com.google.firebase.* y no .ktx.
 */
object FirebaseModule {

    val auth: FirebaseAuth by lazy { Firebase.auth }

    val firestore: FirebaseFirestore by lazy { Firebase.firestore }

    val storage: FirebaseStorage by lazy { Firebase.storage }

    /** UID del usuario autenticado, o null si no hay sesión. */
    fun currentUserId(): String? = auth.currentUser?.uid
}
