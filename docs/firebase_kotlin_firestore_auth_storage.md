# Firebase para Kotlin en Android: Firestore, Authentication y Storage

**Proyecto:** HelloCityReport — app Android (Kotlin) para reportar incidencias urbanas.
**Autor:** Ortega Cruz Yamil Gael (Equipo 1).
**Rama de prueba:** `test/firebase-or`.
**Fecha:** 22 de noviembre de 2026.
**Fuentes:** documentación oficial de Firebase (firebase.google.com/docs), consultada el 22/11/2026.

---

## 0. Cambio importante: los módulos KTX ya no se usan

Firebase movió **todas las APIs KTX a los módulos principales**. Desde julio de 2025 no se publican nuevas versiones de los módulos `-ktx` y desde el **BoM v34.0.0** fueron eliminados del BoM.

| Antes (obsoleto) | Ahora (correcto) |
|---|---|
| `com.google.firebase:firebase-auth-ktx` | `com.google.firebase:firebase-auth` |
| `com.google.firebase:firebase-firestore-ktx` | `com.google.firebase:firebase-firestore` |
| `com.google.firebase:firebase-storage-ktx` | `com.google.firebase:firebase-storage` |
| `import com.google.firebase.ktx.Firebase` | `import com.google.firebase.Firebase` |
| `import com.google.firebase.auth.ktx.auth` | `import com.google.firebase.auth.auth` |
| `import com.google.firebase.firestore.ktx.firestore` | `import com.google.firebase.firestore.firestore` |

> En este proyecto usamos el módulo principal con **BoM 35.2.0**, por lo que las extensiones Kotlin (`Firebase.auth`, `Firebase.firestore`, `toObject`, etc.) están disponibles sin los artefactos `-ktx`.

Versiones vigentes según la documentación consultada:

| Componente | Versión |
|---|---|
| Firebase Android BoM | `35.2.0` |
| Google services Gradle plugin | `4.6.0` |
| `firebase-auth` | `25.0.0` (la controla el BoM) |
| `firebase-firestore` | `27.2.0` (la controla el BoM) |
| `firebase-storage` | `23.0.0` (la controla el BoM) |
| `androidx.credentials` / `credentials-play-services-auth` | `1.3.0` |
| `googleid` | `1.1.1` |
| `kotlinx-coroutines-play-services` | `1.10.2` |

---

## 1. Configuración base (pasos obligatorios)

1. **Crear proyecto en Firebase Console** → *Add project* (nombre, ID único, opcional Analytics).
2. **Registrar la app Android**: paquete `com.example.hellocityreport` (debe coincidir exactamente con `applicationId`; es sensible a mayúsculas y no se puede cambiar después).
3. **Añadir SHA-1** del certificado de depuración y del de release en *Project settings → General → SHA certificate fingerprints*.
4. **Descargar `google-services.json`** y colocarlo en `app/` (raíz del módulo). Contiene identificadores **no secretos** del proyecto; aun así se versiona solo la plantilla `.example` y el archivo real se ignora en Git.
5. **Plugin Gradle**:
   - Root `build.gradle.kts`: `alias(libs.plugins.google.services) apply false`
   - `app/build.gradle.kts`: `alias(libs.plugins.google.services)`
6. **BoM y dependencias** en `app/build.gradle.kts`:
   ```kotlin
   implementation(platform(libs.firebase.bom))
   implementation(libs.firebase.auth)
   implementation(libs.firebase.firestore)
   implementation(libs.firebase.storage)
   implementation(libs.kotlinx.coroutines.play.services)
   ```
7. **Sincronizar Gradle**. El plugin `google-services` genera recursos como `R.string.default_web_client_id` a partir del `oauth_client` con `client_type: 3`.

Requisitos del proyecto: minSdk 26 (cumple el mínimo 24/API 21 de los SDK), compileSdk 37, Jetpack AndroidX y dispositivo/emulador con Google Play Services.

---

## 2. Cloud Firestore

### 2.1 Qué es y cómo se activa

Base de datos NoSQL documental, escalable y con sincronización en tiempo real. Los datos se guardan en **documentos**, agrupados en **colecciones**, y pueden tener **subcolecciones**. Se activa en *Databases & Storage → Firestore → Create database*, eligiendo ubicación y modo inicial de reglas:

- **Test mode**: apto para empezar con SDKs móviles; permite a cualquiera leer/escribir. Solo temporal.
- **Production mode**: niega todo acceso desde clientes móviles; requiere reglas y Auth.

### 2.2 API Kotlin esencial

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

val db = Firebase.firestore              // instancia por defecto
val incidencias = db.collection("incidencias")
```

**Escribir** (el `await()` convierte `Task<T>` en `suspend`):

```kotlin
data class Incidencia(
    val titulo: String = "",
    val categoria: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val creadoPor: String? = null,
)

suspend fun crear(inc: Incidencia): String {
    val ref = db.collection("incidencias").document()
    ref.set(inc).await()               // o .add(map).await() con ID automático
    return ref.id
}
```

**Leer y consultar**:

```kotlin
val lista = db.collection("incidencias")
    .whereEqualTo("categoria", "bache")
    .orderBy("creadoEn")
    .limit(20)
    .get().await()
    .toObjects(Incidencia::class.java)

val una = db.collection("incidencias").document(id).get().await()
if (una.exists()) {
    val inc = una.toObject(Incidencia::class.java)
}
```

**Tiempo real**: `addSnapshotListener` emite un `QuerySnapshot` cada vez que cambian los datos; en Kotlin se suele envolver con `callbackFlow` para exponerlo como `Flow`.

### 2.3 Seguridad

Las reglas se editan en la pestaña *Rules*. Ejemplo mínimo con Auth:

```
service cloud.firestore {
  match /databases/{database}/documents {
    match /incidencias/{id} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
                    && request.resource.data.creadoPor == request.auth.uid;
      allow update, delete: if request.auth != null
                            && resource.data.creadoPor == request.auth.uid;
    }
  }
}
```

Buenas prácticas: no confiar en datos del cliente, validar `request.resource.data` en reglas, crear índices compuestos cuando `whereEqualTo` + `orderBy` los exige, y activar App Check en producción.

### 2.4 Aplicación en CityReport

Colección `incidencias` con campos `titulo`, `descripcion`, `categoria`, `latitud`, `longitud`, `fotoUrl`, `creadoPor` (UID) y `creadoEn` (`FieldValue.serverTimestamp()`). Esto cubre el modelo relacional actual sin abandonar Room: Firestore sería la fuente de datos remota y Room/DataStore la caché local.

---

## 3. Firebase Authentication

### 3.1 API Kotlin esencial

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

val auth = Firebase.auth
val user = auth.currentUser              // null si no hay sesión
val uid = user?.uid
val nombre = user?.displayName
val email = user?.email
val foto = user?.photoUrl
```

- Los proveedores se **habilitan en Console** (*Authentication → Sign-in method*): Email/Password, Google, anónimo, etc.
- `auth.addAuthStateListener { ... }` reacciona a cambios de sesión.
- El estado se persiste entre reinicios; `auth.signOut()` cierra la sesión.
- `user?.getIdToken(false)?.await()` entrega el ID token para un backend propio (**no** usar `uid` como credencial de servidor).

### 3.2 Email/Password (referencia)

```kotlin
auth.createUserWithEmailAndPassword(email, pass).await()
auth.signInWithEmailAndPassword(email, pass).await()
```

Errores típicos: `FirebaseAuthWeakPasswordException`, `FirebaseAuthUserCollisionException`, `FirebaseAuthInvalidCredentialsException`.

### 3.3 Google Sign-In (resumen)

Flujo moderno recomendado por Firebase: **Credential Manager** obtiene un **Google ID token** y Firebase lo canjea por una credencial:

```kotlin
val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
val result = auth.signInWithCredential(credential).await()
val user = result.user
```

El detalle completo (elementos, configuración, código y problemas) está en `docs/autenticacion_google_signin.md`.

### 3.4 Relación con las reglas

En Firestore y Storage, `request.auth` contiene el UID del usuario autenticado; por eso Auth es la pieza que habilita reglas por usuario.

---

## 4. Cloud Storage for Firebase

### 4.1 Activación

Requiere proyecto en plan **Blaze (pay-as-you-go)**. En *Databases & Storage → Storage → Get started* se elige la ubicación del bucket y las reglas. El bucket por defecto se llama `PROJECT_ID.firebasestorage.app` (los antiguos, `PROJECT_ID.appspot.com`).

### 4.2 API Kotlin esencial

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

val storage = Firebase.storage
// val storage = Firebase.storage("gs://mi-bucket")  // bucket alternativo
val ref = storage.reference.child("incidencias/$id/foto.jpg")

ref.putFile(uri).await()                       // UploadTask → Task<UploadTask.TaskSnapshot>
val url = ref.downloadUrl.await()              // Uri
val meta = ref.metadata.await()
```

También existe `putBytes(byteArray)` y se puede usar `getBytes(limit)` para descargar. Las operaciones pueden reportar progreso con `addOnProgressListener`.

### 4.3 Reglas y uso en CityReport

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /incidencias/{id}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
                   && request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
  }
}
```

Las fotos de incidencias se guardan en `incidencias/{id}/...` y su `downloadUrl` se persiste en el documento de Firestore. En producción conviene combinar reglas con App Check y limitar el tamaño/tipo de archivo.

---

## 5. Coroutines y `await()`

`kotlinx-coroutines-play-services` añade `Task<T>.await()`, disponible para Auth, Firestore y Storage. Evita encadenar `addOnSuccessListener`/`addOnFailureListener` y permite escribir código secuencial con `suspend` + `viewModelScope.launch`. Las excepciones se capturan con `try/catch` (o `runCatching`).

---

## 6. Emuladores (opcional pero recomendado)

El *Local Emulator Suite* permite probar Firestore, Auth y Storage sin tocar datos reales. Se conecta desde el código de depuración (por ejemplo `Firebase.firestore.useEmulator("10.0.2.2", 8080)`) y se levanta con `firebase emulators:start`. Muy útil para validar reglas antes de publicar.

---

## 7. Fuentes oficiales

- Configuración Android: https://firebase.google.com/docs/android/setup
- Migración KTX → módulos principales: https://firebase.google.com/docs/android/kotlin-migration
- Auth (inicio): https://firebase.google.com/docs/auth/android/start
- Google Sign-In: https://firebase.google.com/docs/auth/android/google-signin
- Firestore quickstart: https://firebase.google.com/docs/firestore/quickstart
- Firestore reglas: https://firebase.google.com/docs/firestore/security/get-started
- Storage (inicio): https://firebase.google.com/docs/storage/android/start
- Referencia Kotlin: https://firebase.google.com/docs/reference/kotlin/packages
