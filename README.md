# HelloCityReport

App Android (Kotlin) para reportar incidencias urbanas (baches, alumbrado, fugas, etc.).
Arquitectura **MVP + Material 3**, con colores dinámicos (Material You).

## Requisitos
- Android Studio (recomendado) con SDK Android (minSdk 26, compileSdk 37).
- JDK 11+ (Android Studio incluye uno en `jbr/`).
- Emulador/dispositivo con **Google Play Services** (para Maps y ubicación).
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

   > Sin key, la app compila y corre, pero el mapa se verá vacío (placeholder `YOUR_GOOGLE_MAPS_API_KEY`).

3. Obtén una API key en Google Cloud Console:
   - Habilita **Maps SDK for Android**.
   - Crea una credencial (API key) y restríngela por paquete `com.example.hellocityreport` + huella SHA-1 de tu certificado (recomendado).

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