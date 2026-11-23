package com.example.hellocityreport.firebase

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Modelo de una incidencia tal como se persiste en Firestore.
 * Los valores por defecto son necesarios para que Firestore pueda instanciar la clase.
 */
data class IncidenciaFirestore(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotoUrl: String? = null,
    val creadoPor: String? = null,
)

/**
 * Ejemplo de capa de datos remota para CityReport usando Firestore + Storage.
 * Se usa `await()` (kotlinx-coroutines-play-services) en lugar de listeners anidados.
 */
class IncidenciaRepository(
    private val db: FirebaseFirestore = FirebaseModule.firestore,
    private val storage: FirebaseStorage = FirebaseModule.storage,
) {

    private val incidencias = db.collection("incidencias")

    /** Crea una incidencia con ID automático y marca autor/fecha en el servidor. */
    suspend fun crear(incidencia: IncidenciaFirestore): String {
        val ref = incidencias.document()
        val datos = hashMapOf(
            "titulo" to incidencia.titulo,
            "descripcion" to incidencia.descripcion,
            "categoria" to incidencia.categoria,
            "latitud" to incidencia.latitud,
            "longitud" to incidencia.longitud,
            "fotoUrl" to incidencia.fotoUrl,
            "creadoPor" to FirebaseModule.currentUserId(),
            "creadoEn" to FieldValue.serverTimestamp(),
        )
        ref.set(datos).await()
        return ref.id
    }

    /** Lista las incidencias de una categoría, ordenadas por fecha. */
    suspend fun listar(categoria: String? = null, limite: Int = 20): List<IncidenciaFirestore> {
        var query = incidencias.orderBy("creadoEn")
        if (categoria != null) query = query.whereEqualTo("categoria", categoria)
        return query.limit(limite).get().await().toObjects(IncidenciaFirestore::class.java)
    }

    /** Obtiene una incidencia por ID; null si no existe. */
    suspend fun obtener(id: String): IncidenciaFirestore? =
        incidencias.document(id).get().await().toObject(IncidenciaFirestore::class.java)

    /** Sube la foto a Storage y devuelve su URL de descarga. */
    suspend fun subirFoto(incidenciaId: String, uri: Uri): String {
        val ref = storage.reference.child("incidencias/$incidenciaId/${System.currentTimeMillis()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    /** Actualiza la URL de la foto en el documento de Firestore. */
    suspend fun guardarFotoUrl(incidenciaId: String, url: String) {
        incidencias.document(incidenciaId).update("fotoUrl", url).await()
    }
}
