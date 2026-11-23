# HelloCityReport

App Android (Kotlin) para reportar incidencias urbanas (baches, alumbrado, fugas, etc.).
Arquitectura **MVP + Material 3**, con colores dinámicos (Material You).

## Requisitos
- Android Studio (recomendado) con SDK Android (minSdk 26, compileSdk 37).
- JDK 11+ (Android Studio incluye uno en `jbr/`).
- Emulador/dispositivo con **Google Play Services** (para Maps, ubicación y Google Sign-In).
- Conexión a internet en la primera compilación (descarga dependencias).

## Configuración inicial
1. Clona el repo y ábrelo en Android Studio. Android Studio generará `local.properties` con `sdk.dir`.
2. Configura la API key de Google Maps (elige una opción):

   **Opción A — `local.properties` (recomendada para desarrollo):**
   Copia `local.properties.example` a `local.properties` y completa:
   ```
   MAPS_API_KEY=TU_API_KEY
   sdk.dir=C:/Users/TU_USUARIO/AppData/Local/Android/Sdk
   ```

   **Opción B — variable de entorno:**
   - Windows (PowerShell): `[System.Environment]::SetEnvironmentVariable("MAPS_API_KEY", "TU_API_KEY", "User")`
   - Luego reinicia Android Studio / terminal.

   **Opción C — línea de comandos:**
   `./gradlew assembleDebug -PMAPS_API_KEY=TU_API_KEY`

   > La API key ya está incluida por defecto en el proyecto, por lo que el mapa funciona sin configuración adicional.
   > Si quieres usar tu propia key, sobrescríbela con las opciones A/B/C.

3. Obtén una API key en Google Cloud Console:
   - Habilita **Maps SDK for Android**.
   - Crea una credencial (API key) y restríngela por paquete `com.example.hellocityreport` + huella SHA-1 de tu certificado (recomendado).

## Integración con Firebase (rama `test/firebase-or`)
1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/) y registra el paquete `com.example.hellocityreport`.
2. Registra el **SHA-1** de debug y release en *Project settings → General*.
3. Activa los productos: **Authentication** (proveedor Google), **Cloud Firestore** y **Cloud Storage** (plan Blaze).
4. Copia `app/google-services.json.example` como `app/google-services.json` y sustituye los valores por los de tu proyecto. El archivo real está en `.gitignore`; **nunca lo subas**.
   - Es importante volver a descargar el JSON **después** de activar Google: así incluye el `oauth_client` tipo 3 que genera `R.string.default_web_client_id`.
5. Sincroniza Gradle y ejecuta la app en un dispositivo con Google Play Services.

Documentación detallada:
- `docs/firebase_kotlin_firestore_auth_storage.md` — Firestore, Auth y Storage en Kotlin.
- `docs/autenticacion_google_signin.md` — flujo y elementos de Google Sign-In.
- `docs/investigacion_firebase_kotlin.md` — reporte de indagación.

## Compilar y ejecutar
```
./gradlew assembleDebug
```
O desde Android Studio: Run ▶ sobre un emulador/dispositivo con Google Play Services.

## Tests
```
./gradlew testDebugUnitTest
```

## Equipo
Equipo 1:
- AGUILAR LOPEZ GAEL ANTONIO
- GONZALEZ MUÑOZ ALEXA SHERLYN
- MANRIQUE OLVERA JOSUE URIEL
- ORTEGA CRUZ YAMIL GAEL
- VALLADARES CAZARES ANGEL URIEL

## Documentación
- Wireframe de reporte: `docs/wireframe_reporte.html` / `docs/wireframe_reporte.md` / `docs/wireframe_reporte.pdf`
- Almacenamiento local: `docs/investigacion_almacenamiento_android.md`
- Firebase y Google Sign-In: ver la sección "Integración con Firebase".
