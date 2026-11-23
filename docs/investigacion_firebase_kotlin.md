# Reporte de indagación: integración de Firebase en HelloCityReport (Kotlin)

**Integrante:** Ortega Cruz Yamil Gael (Equipo 1) · **Repo:** https://github.com/Yamilipn/HelloCityReport
**Rama de prueba:** `test/firebase-or` (creada desde `master`) · **Fecha:** 22/11/2026
**Consigna:** revisar la documentación de Firebase para Kotlin (Firestore, Auth, Storage), configurar Firebase en una rama de prueba, documentar Google Sign-In y reportar aprendizajes, dificultades y soluciones.

---

## 1. Metodología

Consulté la documentación oficial de Firebase en `firebase.google.com/docs` (versión vigente en la fecha), con foco en las guías de configuración de Android, migración KTX → módulos principales, Auth + Google Sign-In, Firestore y Cloud Storage. Contrasté esas indicaciones con el estado real del repositorio (`app/build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`, paquete `com.example.hellocityreport`) para proponer una integración compatible con el proyecto (AGP 9.3.2, Kotlin 2.2.10, compileSdk 37, minSdk 26, Compose y MVP).

---

## 2. Lo que aprendí

### 2.1 Configuración
- Integrar Firebase son **tres piezas**: un proyecto en Console, el archivo `google-services.json` en `app/` y el plugin Gradle `com.google.gms.google-services` junto con el BoM. El plugin es el que "inyecta" los valores del JSON en el build (incluido `R.string.default_web_client_id`).
- El **BoM** fija versiones compatibles: con `35.2.0`, Auth queda en 25.0.0, Firestore en 27.2.0 y Storage en 23.0.0 sin escribir versiones en cada dependencia.
- La app ya tiene **minSdk 26 y Java 11**, así que no hic falta activar desugaring adicional.

### 2.2 Firestore
- Modelo documental (colecciones → documentos → subcolecciones), con consultas, tiempo real y reglas de seguridad.
- `Firebase.firestore` + `Task.await()` permite código `suspend` limpio; `toObjects<T>()` mapea documentos a `data class`.
- Las reglas con `request.auth.uid` son la forma natural de proteger las incidencias por usuario; hay que pasar de *test mode* a reglas reales antes de producir.

### 2.3 Auth
- `Firebase.auth` expone `currentUser`, `addAuthStateListener` y los métodos de los proveedores habilitados en Console.
- Google Sign-In ya no se implementa con el viejo `GoogleSignInClient`: Firebase recomienda **Credential Manager**. El flujo es: ID token de Google → `GoogleAuthProvider.getCredential` → `signInWithCredential` → `FirebaseUser`.

### 2.4 Storage
- Requiere plan Blaze y un bucket (`PROJECT_ID.firebasestorage.app`).
- `Firebase.storage.reference.child(...)` + `putFile(uri).await()` + `downloadUrl.await()` resuelve el almacenamiento de fotos de incidencias; conviene guardar la URL resultante en Firestore y limitar tamaño/tipo en las reglas.

### 2.5 Kotlin moderno
- **Descubrimiento clave:** los módulos `-ktx` dejaron de publicarse en julio de 2025 y se eliminaron del BoM 34.0.0. Las APIs Kotlin están ahora en los módulos principales con otros imports (`com.google.firebase.Firebase`, `com.google.firebase.auth.auth`, etc.). Ahorra dependencias y evita usar librerías en fin de mantenimiento.

---

## 3. Dificultades encontradas y cómo las resolví

| # | Dificultad | Cómo la resolví |
|---|---|---|
| 1 | **Información contradictoria**: muchos tutoriales seguían usando `firebase-auth-ktx` y `GoogleSignInClient` | Contrasté con la guía oficial de migración y con la página actual de Google Sign-In; adopté módulos principales y Credential Manager |
| 2 | **Elegir versión del BoM** compatible con AGP 9.3.2 / compileSdk 37 | Usé el BoM 35.2.0 documentado y dejé que él controlara las versiones de cada SDK |
| 3 | **`google-services.json` real no disponible** (requiere proyecto Firebase y permisos de Console) | Creé `app/google-services.json.example` con la estructura correcta (incluido `client_type: 3`) y añadí `/google-services.json` a `.gitignore`; el equipo solo debe descargar su archivo y copiarlo |
| 4 | **No subir secretos al repo** | El JSON real se ignora; la plantilla lleva valores de ejemplo (`REEMPLAZA_CON...`). El token MCP tampoco permite secret scanning sobre el archivo real porque no existe localmente |
| 5 | **SHA-1 del certificado** | Documenté `keytool` y *signingReport* de Android Studio; recordé registrar tanto debug como release |
| 6 | **`R.string.default_web_client_id`** no existe si el JSON no tiene OAuth web | Documenté que hay que volver a descargar el JSON **después** de habilitar el proveedor Google; es el error de compilación más frecuente |
| 7 | **Distinguir client ID Web vs Android** | Aclaré en la guía que `setServerClientId` usa el de tipo *Web application* (`client_type: 3`), no el de Android (`client_type: 1`) |
| 8 | **Manejo de coroutines sobre `Task`** | Añadí `kotlinx-coroutines-play-services` y usé `await()` + `runCatching` en lugar de listeners anidados |
| 9 | **Acceso de escritura a GitHub via MCP** | El token configurado devolvió `403 Resource not accessible by personal access token` al crear rama/archivos (permiso `Contents: write` ausente). Preparé todos los entregables localmente y solicité habilitar ese permiso para subirlos a `test/firebase-or` |

---

## 4. Estado de la integración en la rama

**Archivos preparados para la rama `test/firebase-or`:**

- `gradle/libs.versions.toml`: BoM, plugin `google-services`, Firestore/Auth/Storage, Credentials, Google ID y coroutines.
- `build.gradle.kts`: plugin `google-services` (`apply false`).
- `app/build.gradle.kts`: plugin aplicado + dependencias Firebase y Google Sign-In.
- `app/.gitignore`: ignora `google-services.json`.
- `app/google-services.json.example`: plantilla segura del archivo de configuración.
- `app/src/main/java/com/example/hellocityreport/firebase/FirebaseModule.kt`: instancias `auth`, `firestore`, `storage`.
- `app/src/main/java/com/example/hellocityreport/firebase/IncidenciaRepository.kt`: CRUD y subida de fotos con coroutines.
- `app/src/main/java/com/example/hellocityreport/firebase/GoogleSignInManager.kt`: flujo completo de Credential Manager + Firebase.
- `docs/firebase_kotlin_firestore_auth_storage.md`, `docs/autenticacion_google_signin.md`, este reporte y el README actualizado.

**Pendiente del equipo (requiere credenciales reales):**
1. Crear el proyecto Firebase y registrar `com.example.hellocityreport`.
2. Añadir SHA-1 de debug/release, habilitar Google, descargar `google-services.json` y copiarlo en `app/`.
3. Crear la base Firestore y el bucket Storage; reemplazar las reglas de prueba por las definitivas.
4. Ejecutar `./gradlew assembleDebug` y probar el flujo en emulador con Google Play Services.

---

## 5. Conclusiones y recomendaciones

- La integración moderna de Firebase en Kotlin es **más simple que la antigua**: un solo módulo por producto, BoM para versiones y `await()` para coroutines.
- El punto más delicado no es el código sino la **configuración en Console**: SHA-1, proveedor habilitado y un `google-services.json` actualizado. La mayoría de errores de compilación y de login vienen de ahí.
- Para CityReport conviene Firestore como fuente remota, Storage para fotos y Auth (Google) como llave de las reglas; Room/DataStore sigue siendo útil como caché local.
- Recomendación final: usar el Emulator Suite mientras se ajustan reglas y subir a producción solo con reglas estrictas y App Check.

---

## 6. Fuentes

- https://firebase.google.com/docs/android/setup
- https://firebase.google.com/docs/android/kotlin-migration
- https://firebase.google.com/docs/auth/android/start
- https://firebase.google.com/docs/auth/android/google-signin
- https://firebase.google.com/docs/firestore/quickstart
- https://firebase.google.com/docs/firestore/security/get-started
- https://firebase.google.com/docs/storage/android/start
- https://developer.android.com/identity/sign-in/credential-manager-siwg
