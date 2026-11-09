# Investigación: Almacenamiento local en apps Android

**Proyecto base:** HelloCityReport (https://github.com/Yamilipn/HelloCityReport)  
**Equipo:** AGUILAR LÓPEZ GAEL ANTONIO, GONZÁLEZ MUÑOZ ALEXA SHERLYN, MANRIQUE OLVERA JOSUÉ URIEL, ORTEGA CRUZ YAMIL GAEL, VALLADARES CAZARES ÁNGEL URIEL  
**Contexto:** CityReport maneja incidencias reportadas por usuarios y clasificadas por categoría. La información es estructurada y relacional, por lo que la elección del mecanismo de persistencia impacta en mantenibilidad, seguridad y rendimiento.

---

## 1. SharedPreferences vs DataStore

### 1.1 SharedPreferences

**Cómo funciona**

- API clásica de Android para guardar datos **clave-valor** en un archivo XML privado de la app.
- Se obtiene con `getSharedPreferences(name, MODE_PRIVATE)` o `PreferenceManager`.
- Operaciones:
  - `edit()` → `putString`, `putInt`, `putBoolean`, etc.
  - `apply()` guarda de forma asíncrona (recomendado).
  - `commit()` guarda de forma síncrona y devuelve `boolean`.
- Lectura con `getString`, `getInt`, etc., con valor por defecto.

**Ventajas**

- Muy simple y rápido de usar.
- Ideal para datos pequeños: flags, tokens simples, preferencias de UI, estado de onboarding.
- No requiere dependencias adicionales.
- Ampliamente documentado y compatible con versiones antiguas de Android.

**Desventajas**

- No es **type-safe**: las claves son `String` y los valores se recuperan manualmente.
- No es transaccional: escrituras concurrentes pueden generar inconsistencias.
- `apply()`/`commit()` pueden bloquear o generar ANR en casos de I/O pesado.
- No soporta datos estructurados ni consultas.
- Difícil de migrar y versionar; el XML no ofrece validación de esquema.
- Google recomienda migrar a DataStore.

### 1.2 Jetpack DataStore

**Cómo funciona**

- Solución moderna de Jetpack para persistir datos con **Kotlin Coroutines y Flow**.
- Dos variantes:
  1. **Preferences DataStore**: clave-valor, sin esquema fijo, API similar a SharedPreferences.
  2. **Proto DataStore**: objetos tipados definidos con Protocol Buffers (o JSON con serializer propio), con esquema y type safety.
- Lectura mediante `Flow<T>` (reactiva, asíncrona).
- Escritura mediante `updateData { ... }`, que ejecuta una transacción atómica read-modify-write.
- Manejo de corrupción con `ReplaceFileCorruptionHandler`.
- Soporte multi-proceso con `MultiProcessDataStoreFactory`.

**Ventajas**

- Asíncrono por diseño (coroutines), evita bloqueos del hilo principal.
- Transaccional y consistente (read-after-write).
- Type-safe en la variante Proto.
- Inmutabilidad y validación de esquema.
- Migración asistida desde SharedPreferences.
- Integración natural con Compose (`collectAsStateWithLifecycle`) y ViewModel.

**Desventajas**

- Mayor boilerplate (keys, serializers, Flow).
- Requiere Kotlin Coroutines y conocimiento de Flow.
- No está pensado para datasets grandes ni consultas parciales.
- No ofrece integridad referencial ni relaciones entre entidades.
- Google indica que para datos grandes/complejos se debe usar Room.

### 1.3 Comparativa rápida

| Criterio | SharedPreferences | DataStore |
|---|---|---|
| Tipo de datos | Clave-valor simples | Clave-valor o objetos tipados |
| Asincronía | `apply()` (limitada) | Coroutines/Flow (nativa) |
| Type safety | No | Sí (Proto) / parcial (Preferences) |
| Transacciones | No | Sí (atómica) |
| Corrupción | Manejo manual | Handler integrado |
| Multi-proceso | Limitado | Soportado |
| Datasets grandes | No | No |
| Recomendación actual | Legado | Preferido para preferencias |

**Conclusión para CityReport:** SharedPreferences/DataStore son adecuados para **preferencias de la app** (tema, filtros, sesión ligera), pero no para el modelo de incidencias, que es relacional y requiere consultas.

---

## 2. SQLite directo vs Room

### 2.1 SQLite directo (SQLiteDatabase / SQLiteOpenHelper)

**Cómo funciona**

- Uso directo de la base de datos SQLite embebida en Android.
- Se crea una clase que extiende `SQLiteOpenHelper` (o se usa `SQLiteDatabase` directamente).
- Se escriben sentencias SQL manualmente (`CREATE TABLE`, `INSERT`, `SELECT`, `UPDATE`, `DELETE`).
- Los resultados se leen con `Cursor`, que debe cerrarse manualmente.
- Las migraciones se implementan sobrescribiendo `onUpgrade` y gestionando versiones manualmente.

**Ventajas**

- Control total sobre el SQL y el esquema.
- Sin dependencias adicionales ni generación de código.
- Puede ser más flexible para consultas SQL muy específicas o dinámicas.
- Menor overhead de abstracción.

**Desventajas**

- Mucho boilerplate y código repetitivo.
- Sin verificación en tiempo de compilación: errores SQL aparecen en runtime.
- `Cursor` propenso a fugas de memoria si no se cierra.
- Migraciones manuales y propensas a errores.
- No integra de forma nativa con LiveData/Flow/Coroutines; requiere envoltorios propios.
- Mayor riesgo de inyección SQL si no se usan parámetros.

### 2.2 Room (Jetpack)

**Cómo funciona**

- Capa de abstracción sobre SQLite que genera código en tiempo de compilación (KSP).
- Componentes principales:
  1. **Entity**: clase que representa una tabla (`@Entity`).
  2. **DAO**: interfaz con métodos anotados (`@Query`, `@Insert`, `@Update`, `@Delete`).
  3. **Database**: clase abstracta que extiende `RoomDatabase` y declara entidades y DAOs.
- Las consultas se verifican en tiempo de compilación.
- Soporta `Flow`, `LiveData`, `suspend` functions y RxJava.
- Migraciones declarativas con `Migration` y validación de esquema.

**Ventajas**

- Menos boilerplate y código más legible.
- Verificación de SQL en tiempo de compilación.
- Type safety: resultados mapeados a data classes/Kotlin.
- Integración con coroutines, Flow y Compose.
- Migraciones más seguras y automatizables.
- Recomendado oficialmente por Google sobre SQLite directo.

**Desventajas**

- Curva de aprendizaje inicial.
- Requiere KSP y generación de código (tiempos de build).
- Menos flexible para SQL dinámico complejo.
- Overhead de abstracción (mínimo en la mayoría de casos).
- Versiones recientes (Room 3.x) cambian paquetes (`androidx.room3`), lo que puede afectar migraciones de proyectos.

### 2.3 Comparativa rápida

| Criterio | SQLite directo | Room |
|---|---|---|
| Boilerplate | Alto | Bajo/Medio |
| Verificación SQL | Runtime | Compile-time |
| Type safety | Manual | Automática |
| Migraciones | Manuales | Declarativas |
| Integración Flow/Coroutines | Manual | Nativa |
| Flexibilidad SQL | Total | Alta (con límites) |
| Recomendación Google | No (legado) | Sí |

**Conclusión para CityReport:** Las incidencias, usuarios y categorías forman un modelo relacional. Se recomienda **Room sobre SQLite** para la persistencia local, y DataStore (o SharedPreferences legado) solo para preferencias simples.

---

## 3. Recomendación arquitectónica para CityReport

- **Room (SQLite)** para entidades `Usuario`, `Categoria` e `Incidencia` y sus relaciones.
- **DataStore (Preferences)** para preferencias de UI (tema, filtros, último usuario recordado).
- Evitar SharedPreferences en código nuevo; migrar gradualmente si existiera.
- Evitar SQLite directo salvo consultas muy específicas no cubiertas por Room.

---

## 4. Fuentes

- Android Developers: DataStore (https://developer.android.com/topic/libraries/architecture/datastore)
- Android Developers: Room (https://developer.android.com/training/data-storage/room)
- Android Developers: SharedPreferences (https://developer.android.com/reference/kotlin/android/content/SharedPreferences)
- Android Developers: SQLite (https://developer.android.com/training/data-storage/sqlite)
