# Autenticación con Google Sign-In en Firebase (Kotlin / Android)

**Proyecto:** HelloCityReport · **Rama:** `test/firebase-or` · **Fecha:** 22/11/2026
**Implementación de referencia:** `app/src/main/java/com/example/hellocityreport/firebase/GoogleSignInManager.kt`

---

## 1. Idea general

El usuario pulsa "Continuar con Google". Android obtiene un **Google ID token** mediante **Credential Manager**; la app envía ese token a **Firebase Authentication**, que lo valida y devuelve un `FirebaseUser`. A partir de ahí, Firestore y Storage pueden identificar al usuario mediante `request.auth.uid` en sus reglas de seguridad.

Firebase **no** guarda la contraseña del usuario de Google: solo recibe el token firmado por Google, lo verifica y crea/vincula una cuenta en el proyecto.

---

## 2. Elementos que intervienen

| Elemento | Dónde está | Función |
|---|---|---|
| Proyecto Firebase y app Android registrada | Firebase Console | Define `project_id`, `applicationId`, `mobilesdk_app_id` |
| `google-services.json` | `app/` | Configuración del SDK; su `oauth_client` con `client_type: 3` genera `R.string.default_web_client_id` |
| Huella **SHA-1** | Firebase Console ↔ keystore local | Vincula la app instalada con el cliente OAuth autorizado |
| OAuth 2.0 Client ID **de tipo Web** | Google Cloud Console → Credentials | Es el `serverClientId`; **no** es el client ID de Android |
| Proveedor Google habilitado | Firebase Console → Authentication → Sign-in method | Permite que Firebase acepte credenciales de Google |
| `CredentialManager` | `androidx.credentials` | Diálogo del sistema para elegir cuenta Google |
| `GetGoogleIdOption` / `GetCredentialRequest` | `googleid` + `credentials` | Describe qué credencial se solicita |
| `GetCredentialResponse` → `CustomCredential` | `androidx.credentials` | Respuesta del diálogo; contiene datos del Google ID token |
| `GoogleIdTokenCredential` | `googleid` | Parsea `credential.data` y expone `idToken` |
| `GoogleAuthProvider.getCredential` | `firebase-auth` | Crea un `AuthCredential` de Firebase con el ID token |
| `FirebaseAuth.signInWithCredential` | `firebase-auth` | Valida el token y entrega `AuthResult.user` |
| `FirebaseUser` | `firebase-auth` | Perfil (`uid`, `displayName`, `email`, `photoUrl`) y sesión |
| `ClearCredentialStateRequest` | `androidx.credentials` | Limpia la cuenta recordada al cerrar sesión |
| Reglas de Firestore/Storage | Firebase Console | Usan `request.auth.uid` para autorizar datos |

> Nota: la documentación antigua usaba `GoogleSignInClient` de `play-services-auth`. La guía actual de Firebase usa **Credential Manager**, que es el reemplazo moderno y funciona con el diálogo del sistema.

---

## 3. Flujo completo

```mermaid
sequenceDiagram
    participant U as Usuario
    participant A as App (Compose)
    participant CM as Credential Manager
    participant G as Google Identity
    participant FA as Firebase Auth
    participant FS as Firestore / Storage

    U->>A: Pulsa "Continuar con Google"
    A->>CM: getCredential(GetCredentialRequest + GetGoogleIdOption)
    CM->>G: Solicita cuenta e ID token (serverClientId)
    G-->>CM: CustomCredential (google.idtoken)
    CM-->>A: GetCredentialResponse
    A->>A: GoogleIdTokenCredential.createFrom(data).idToken
    A->>FA: signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
    FA->>FA: Verifica firma/audience del token; crea o vincula cuenta
    FA-->>A: AuthResult.user (FirebaseUser)
    A->>FS: Operaciones con request.auth.uid
    A->>CM: signOut → ClearCredentialStateRequest
```

---

## 4. Configuración previa (checklist)

1. ✅ Firebase agregado al proyecto (ver `firebase_kotlin_firestore_auth_storage.md`).
2. ✅ Dependencias en `app/build.gradle.kts`:
   ```kotlin
   implementation(platform(libs.firebase.bom))          // 35.2.0
   implementation(libs.firebase.auth)                    // 25.0.0 vía BoM
   implementation(libs.androidx.credentials)             // 1.3.0
   implementation(libs.androidx.credentials.play.services.auth) // 1.3.0
   implementation(libs.google.id)                        // 1.1.1
   implementation(libs.kotlinx.coroutines.play.services)
   ```
3. **SHA-1** registrado. Obtención local:
   ```powershell
   keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
   También está el *Signing Report* en Android Studio (▶ Gradle → `app:tasks → signingReport`). El SHA-1 de debug y el de release deben registrarse.
4. **Habilitar Google** en *Firebase Console → Authentication → Sign-in method → Google → Enable → Save*. Elegir el email de soporte del proyecto.
5. **Descargar de nuevo `google-services.json`** y reemplazar el anterior: ahora contendrá el `oauth_client` de tipo web. Sin ese bloque, la compilación falla porque no existe `R.string.default_web_client_id`.
6. Verificar en *Google Cloud Console → APIs & Services → Credentials* que existe un client ID de tipo **Web application**; su valor es el `serverClientId`.

---

## 5. Implementación Kotlin

Archivo: `app/src/main/java/com/example/hellocityreport/firebase/GoogleSignInManager.kt` (incluido en esta rama).

Puntos clave:

```kotlin
// 1. Petición: usar el client ID WEB (generado por google-services)
private fun buildRequest(filterByAuthorized: Boolean, autoSelect: Boolean) =
    GetCredentialRequest.Builder()
        .addCredentialOption(
            GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(filterByAuthorized)
                .setAutoSelectEnabled(autoSelect)
                .build()
        )
        .build()

// 2. Obtener credencial; si no hay cuentas autorizadas, mostrar todas
val response = try {
    credentialManager.getCredential(context, buildRequest(true, true))
} catch (e: NoCredentialException) {
    credentialManager.getCredential(context, buildRequest(false, false))
}

// 3. Extraer el ID token y validarlo en Firebase
val googleCredential = when (val c = response.credential) {
    is CustomCredential if c.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ->
        GoogleIdTokenCredential.createFrom(c.data).idToken
    else -> throw IllegalArgumentException("Credencial no es de tipo Google ID")
}
val user = auth.signInWithCredential(
    GoogleAuthProvider.getCredential(googleCredential, null)
).await().user

// 4. Cerrar sesión en ambos sistemas
auth.signOut()
credentialManager.clearCredentialState(ClearCredentialStateRequest())
```

**Por qué dos intentos:** `setFilterByAuthorizedAccounts(true)` solo muestra cuentas que ya usaron la app; si no hay ninguna, Credential Manager lanza `NoCredentialException` y se reintenta con `false` para mostrar todas las cuentas disponibles. `setAutoSelectEnabled(true)` permite el ingreso silencioso cuando solo hay una cuenta autorizada.

**Comprobación de sesión:** en `onStart()` (o al iniciar el ViewModel), `auth.currentUser` debe actualizar la UI. `addAuthStateListener` mantiene la UI sincronizada si la sesión expira.

---

## 6. Errores frecuentes y solución

| Síntoma | Causa probable | Solución |
|---|---|---|
| `unresolved reference: default_web_client_id` | Falta `oauth_client` tipo 3 en `google-services.json` o no se aplicó el plugin | Volver a descargar el JSON tras habilitar Google y sincronizar Gradle |
| `GetCredentialException` / `NoCredentialException` | Sin cuentas, Play Services desactualizado o filtro demasiado estricto | Reintentar con `filterByAuthorizedAccounts(false)`; verificar Google Play Services |
| `FirebaseAuthException: The given sign-in provider is disabled` | Proveedor Google no habilitado | Activar Google en Console → Authentication → Sign-in method |
| Error 401 / `audience mismatch` | Se usó el client ID de Android en vez del Web | Usar `R.string.default_web_client_id` (client_type 3) |
| Error de certificado / `SHA-1 mismatch` | Falta la huella del keystore usado | Registrar SHA-1 de debug y de release en Console |
| `google-services.json` no encontrado | Archivo no colocado en `app/` o con sufijo `(2)` | Copiar el archivo exactamente en `app/` y sin caracteres extra |
| La sesión se pierde al reiniciar | Se limpió Credential Manager pero no se llamó `signOut()` o viceversa | Ejecutar ambos pasos del apartado 5 |

---

## 7. Seguridad y buenas prácticas

- El `uid` identifica al usuario **dentro de Firebase**; no debe usarse como token para un servidor propio. Para eso, `user.getIdToken()`.
- Reglas de ejemplo: `allow write: if request.auth != null && request.auth.uid == resource.data.creadoPor;`
- Al cerrar sesión hay que limpiar **ambos** estados: Firebase y Credential Manager.
- Si un usuario entra primero con Google y después con email, se pueden vincular cuentas con `linkWithCredential`.
- En producción, activar App Check y revisar las reglas antes de publicar.

---

## 8. Fuentes

- Google Sign-In en Firebase (Android): https://firebase.google.com/docs/auth/android/google-signin
- Auth en Android: https://firebase.google.com/docs/auth/android/start
- Credential Manager — Sign in with Google: https://developer.android.com/identity/sign-in/credential-manager-siwg
- Configuración Android y plugin google-services: https://firebase.google.com/docs/android/setup
