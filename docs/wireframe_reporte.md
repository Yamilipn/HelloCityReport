# Wireframe – Pantalla de reporte (HelloCityReport)

## 1. Contexto y análisis del repositorio
- App Android en Kotlin con arquitectura **MVP + Material 3**.
- Flujos existentes: Lista de incidencias, Detalle, Nueva incidencia.
- Modelo `Incidencia`: id, titulo, descripcion, ubicacion, fecha, estado.
- Repositorio en memoria (`IncidenciasDummy`) sin backend.
- Pantalla actual de creación: `NuevaIncidenciaFragment` con campos título, descripción, ubicación, fecha, estado y botón Guardar.

## 2. Revisión de documentación oficial
### Fused Location Provider
- API principal: `FusedLocationProviderClient`.
- Última ubicación: `getLastLocation()`.
- Actualizaciones periódicas: `LocationRequest` + `requestLocationUpdates()` + `LocationCallback.onLocationResult()`.
- Detener actualizaciones: `removeLocationUpdates()`.
- Permisos: `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` (y background si aplica).
- Ajuste de settings con `SettingsClient` / `LocationSettingsRequest`.
- Buenas prácticas: iniciar en `onResume`, detener en `onPause`, guardar estado en `Bundle`.

### CameraX
- Librería Jetpack para cámara, compatible con API 21+.
- Use cases: Preview, ImageCapture, ImageAnalysis, VideoCapture.
- `ProcessCameraProvider`, `bindToLifecycle()`, `CameraSelector` (cámara trasera).
- `PreviewView` para vista previa.
- Captura con `ImageCapture.takePicture()` y callback.
- Manejo de rotación y ciclo de vida.

## 3. Objetivo del wireframe
Diseñar una pantalla de reporte que combine **mapa (ubicación)** y **foto (evidencia)** de forma clara y usable, manteniendo los campos existentes del formulario.

## 4. Wireframe visual
Archivo: `wireframe_reporte.svg` (incluido en este mismo directorio).

## 5. Estructura de la pantalla
```
+----------------------------------+
| <- Reportar incidencia            |
+----------------------------------+
| [ Tarjeta 1: Ubicación (Mapa) ]  |
| [ Tarjeta 2: Evidencia (Foto) ]  |
| [ Tarjeta 3: Datos del reporte ] |
|   Título / Descripción           |
|   Ubicación (auto) / Fecha       |
|   Estado                         |
|   [ Guardar reporte ]            |
+----------------------------------+
|  Inicio              Reportar    |
+----------------------------------+
```

## 6. Componentes y comportamiento
- **Mapa**: muestra ubicación actual y permite ajustar marcador; botón "Mi ubicación".
- **Foto**: PreviewView + botón capturar; muestra miniatura y opción de retomar.
- **Formulario**: validación de título y ubicación; ubicación autocompletada desde el mapa.
- **Botón Guardar**: deshabilitado hasta contar con ubicación y foto (según reglas).

## 7. Estados
- Sin permiso de ubicación: tarjeta con botón "Conceder permiso".
- Obteniendo ubicación: spinner en el mapa.
- Sin foto: placeholder con ícono de cámara.
- Listo: botón habilitado.

## 8. Permisos requeridos
- AndroidManifest: `ACCESS_FINE_LOCATION` (y COARSE), `CAMERA`.
- Runtime: solicitar ubicación y cámara antes de usar.

## 9. Integración con arquitectura actual
- Actualizar `NuevaIncidenciaFragment` (o crear `ReporteIncidenciaFragment`).
- Ampliar `NuevaPresenter` con ubicación y foto.
- Repository: guardar URI de foto y lat/lng si se agrega al modelo (futuro).
- Navigation: acción desde lista (FAB) a reporte.

## 10. Notas de implementación (próxima fase)
- Dependencias: play-services-location, camerax-core/camera2/lifecycle/view.
- Google Maps SDK o alternativa según alcance.
- Revisar keep rules si aplica.
