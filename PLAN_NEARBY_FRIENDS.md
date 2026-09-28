# Plan: pantalla "Nearby Friends" (Flutter / Dart)

Pantalla que muestra **los amigos que están en el mismo edificio del campus que yo**, y que se actualiza cuando me muevo a otro edificio.

> Este documento es solo planeación. No se ha modificado código del backend ni del front.

---

## 1. Dónde está el backend de esta funcionalidad

Las rutas **no están en la rama `googleImports`** (esa solo tiene auth y Google). Están en las ramas remotas:

- `origin/CLASS-BLOCKS` (la más reciente, usada como referencia en este plan)
- `origin/PATRON-OBSERVER`
- `origin/todos-los-controllers`

Archivos clave (en `origin/CLASS-BLOCKS`):

| Archivo | Qué hace |
|---|---|
| `controllers/NearbyFriendsController.java` | Rutas `/nearby-friends/...` |
| `controllers/UserContextController.java` | Las **mismas** operaciones, con rutas `/users/{userId}/...` (duplicado) |
| `controllers/BuildingController.java` | `/buildings` y `/buildings/locate` |
| `services/NearbyFriendsService.java` | Lógica: actualizar edificio, guardar log, filtrar amigos |
| `repositories/BuildingRepository.java` | Query PostGIS `ST_DWithin` → edificio más cercano cuyo radio te contiene |

**Conclusión:** el backend ya decide en qué edificio estás a partir de las coordenadas GPS. El front **no** tiene que calcular distancias a edificios; solo manda latitud/longitud.

---

## 2. Rutas que va a usar el front

Usar las de `/nearby-friends` (las de `/users/...` hacen exactamente lo mismo; elegir una sola y no mezclar).

### 2.1 Mandar mi ubicación GPS y recibir amigos cercanos ⭐ (la principal)

```
PUT /nearby-friends/user/{userId}/location?latitude=4.6015&longitude=-74.0661
```
- Parámetros van en el **query string**, no en el body.
- El backend busca el edificio que contiene el punto.
  - **Si estoy en un edificio:** guarda `currentBuilding`, `locationUpdatedAt`, crea un `UserLocationLog` y devuelve los amigos que están ahí.
  - **Si no estoy en ninguno:** pone `currentBuilding = null` y devuelve `[]`.
- Respuesta `200`: `List<UserBasicDTO>`

```json
[
  {
    "id": 7,
    "name": "Laura Gómez",
    "phoneNumber": "3001234567",
    "career": "Ingeniería de Sistemas",
    "locationUpdatedAt": "2026-09-28T10:42:00"
  }
]
```

### 2.2 Consultar amigos cercanos sin mandar ubicación (para refrescar)

```
GET /nearby-friends/user/{userId}
```
- Usa el `currentBuilding` que ya tiene guardado el usuario.
- Si no tengo edificio → `[]`.
- Misma respuesta que 2.1. **No** crea log → es la que se usa para el refresco periódico.

### 2.3 Saber en qué edificio estoy (para mostrar el nombre)

```
GET /buildings/locate?latitude=4.6015&longitude=-74.0661
```
- `200` → `BuildingBasicDTO` (`id`, `name`, `location`, `radiusMeters`)
- `404` → texto plano `"No hay ningún edificio en las coordenadas indicadas"` → **estoy fuera de todo edificio**.

### 2.4 Para pruebas sin GPS

```
PUT /nearby-friends/user/{userId}/building/{buildingId}
```
Pone al usuario directamente en un edificio. Útil para probar la pantalla desde el emulador o para un modo "debug".

### Reglas que ya aplica el backend (no repetirlas en el front)

- Solo amistades `ACCEPTED`, sin importar quién envió la solicitud.
- Solo amigos en el **mismo** `currentBuilding`.
- Solo amigos con `locationUpdatedAt` de los **últimos 30 minutos** (`RECENT_MINUTES = 30`).
- Edificio = el más cercano cuyo `radiusMeters` contiene el punto.

### Errores

`GlobalExceptionHandler` responde con **texto plano** (no JSON):

| Código | Cuándo |
|---|---|
| `404` | Usuario o edificio no existe / `locate` fuera de edificios |
| `400` | Datos inválidos |
| `500` | Error inesperado (`"Error interno del servidor: ..."`) |

En Dart leer `response.body` como `String` en los errores, no hacer `jsonDecode`.

---

## 3. Cosas a tener en cuenta (limitaciones del backend actual)

1. **La lista no dice en qué edificio estás.** Las rutas 2.1 y 2.2 devuelven solo amigos. Si quieres mostrar "Estás en Biblioteca", hay que llamar además `GET /buildings/locate` con las mismas coordenadas.
2. **`[]` es ambiguo.** Puede significar "no estás en ningún edificio" o "estás en uno pero no hay amigos". Se distingue con `/buildings/locate` (404 = fuera).
3. **`BuildingBasicDTO.location` es un `Point` de JTS** y el `pom.xml` no incluye un módulo de Jackson para JTS. Es probable que `/buildings/locate` y `/buildings` fallen al convertirse a JSON (error 500) o devuelvan un objeto enorme. **Probarlo en Postman primero.** Si falla, pedir al backend que el DTO exponga `latitude`/`longitude` como `double` (o que ignore `location`). Mientras tanto, el front puede funcionar sin el nombre del edificio (ver plan B en la sección 5).
4. **El `userId` va en la URL.** En `googleImports` el login (`AuthResponse`) ya devuelve `id`; guardarlo junto con los tokens.
5. **Autenticación:** en `googleImports` todas las rutas (menos `/auth/**`) exigen `Authorization: Bearer <accessToken>`. Cuando se unan las ramas, estas rutas también lo van a pedir. Mandar el header desde ya.
6. **Cada `PUT .../location` crea un registro en `UserLocationLog`.** No llamarlo en cada lectura del GPS; solo cuando la posición cambie de verdad (ver sección 5).
7. **`locationUpdatedAt` viene sin zona horaria** (`LocalDateTime`, ej. `"2026-09-28T10:42:00"`). `DateTime.parse` lo toma como hora local, lo que sirve si el servidor está en la misma zona (Colombia). Tenerlo presente si el "hace X min" sale mal.
8. **Hay dos juegos de rutas iguales** (`/nearby-friends/...` y `/users/{id}/...`). Usar solo `/nearby-friends` y comentarlo con el equipo.

---

## 4. Flujo de la pantalla

```
Abrir pantalla
   │
   ├─ ¿Permiso de ubicación? ── no ──► Estado "Sin permiso"
   ├─ ¿GPS encendido?        ── no ──► Estado "Ubicación desactivada"
   │
   ├─ Obtener posición (lat, lng)
   ├─ En paralelo:
   │     PUT /nearby-friends/user/{id}/location?latitude&longitude   → amigos
   │     GET /buildings/locate?latitude&longitude                    → edificio o 404
   │
   ├─ locate = 404        ──► Estado "No estás en ningún edificio"
   ├─ amigos vacío        ──► Estado "Ningún amigo en <Edificio>"
   └─ amigos con datos    ──► Lista

Mientras la pantalla está abierta:
   - Stream de posición (distanceFilter ~20 m). Al moverse:
       PUT .../location + GET /buildings/locate
       (si el edificio no cambió, el PUT igual sirve para mantener fresco locationUpdatedAt)
   - Timer cada 60 s: GET /nearby-friends/user/{id}   (los amigos también se mueven; no crea log)
   - Timer cada ~10 min: PUT .../location aunque no me haya movido,
     para no desaparecer de la lista de mis amigos (el backend descarta ubicaciones de más de 30 min)
   - Pull-to-refresh: PUT .../location con la posición actual
```

---

## 5. Estructura sugerida en Dart

Adáptalo al gestor de estado que ya uses (Provider, Riverpod, Bloc…).

```
lib/
  features/nearby_friends/
    data/
      models/
        nearby_friend.dart           // id, name, phoneNumber?, career, locationUpdatedAt?
        building.dart                // id, name, radiusMeters (ignorar "location")
      nearby_friends_api.dart        // las llamadas de la sección 2
      nearby_friends_repository.dart
    presentation/
      nearby_friends_controller.dart
      nearby_friends_screen.dart
      widgets/
        friend_tile.dart
        building_header.dart
        empty_states.dart
  core/
    location/location_service.dart   // permisos + posición + stream (geolocator)
    network/api_client.dart          // baseUrl, header Bearer, refresh en 401
    session/session_store.dart       // userId + tokens (del AuthResponse)
```

### API en Dart (esqueleto)

```dart
class NearbyFriendsApi {
  NearbyFriendsApi(this._client);
  final ApiClient _client;

  Future<List<NearbyFriend>> updateLocation(int userId, double lat, double lng) =>
      _client.putList('/nearby-friends/user/$userId/location',
          query: {'latitude': '$lat', 'longitude': '$lng'},
          fromJson: NearbyFriend.fromJson);

  Future<List<NearbyFriend>> getNearbyFriends(int userId) =>
      _client.getList('/nearby-friends/user/$userId', fromJson: NearbyFriend.fromJson);

  /// null = fuera de todo edificio (404)
  Future<Building?> locateBuilding(double lat, double lng) async { ... }
}
```

### Modelo

```dart
class NearbyFriend {
  final int id;
  final String name;
  final String? phoneNumber;
  final String career;
  final DateTime? locationUpdatedAt;

  factory NearbyFriend.fromJson(Map<String, dynamic> j) => NearbyFriend(
        id: j['id'],
        name: j['name'],
        phoneNumber: j['phoneNumber'],
        career: j['career'],
        locationUpdatedAt: j['locationUpdatedAt'] == null
            ? null
            : DateTime.parse(j['locationUpdatedAt']),
      );
}
```

### Plan B si `/buildings/locate` falla por el `Point`

- Mostrar la lista sin el nombre del edificio ("Amigos cerca de ti").
- Estado vacío genérico: "No hay amigos cerca o no estás en un edificio del campus".
- Cuando backend arregle el DTO, activar el header con el nombre.

### Paquetes

- `geolocator`: permisos, posición actual, stream de posición.
- `dio` (recomendado, por el interceptor para renovar el token) o `http`.
- `flutter_secure_storage`: `accessToken`, `refreshToken`, `userId` (si no lo tienes ya del login).

### Configuración de plataforma

- **Android** (`AndroidManifest.xml`): `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`.
- **iOS** (`Info.plist`): `NSLocationWhenInUseUsageDescription` ("Usamos tu ubicación para mostrarte qué amigos están en tu mismo edificio").
- Solo ubicación **mientras se usa la app** en esta versión.
- Emulador Android: el backend local es `http://10.0.2.2:8080`.

---

## 6. Estado de la pantalla

```dart
sealed class NearbyFriendsState {}
class NearbyLoading       extends NearbyFriendsState {}
class NearbyNoPermission  extends NearbyFriendsState { final bool permanentlyDenied; }
class NearbyLocationOff   extends NearbyFriendsState {}
class NearbyOutsideCampus extends NearbyFriendsState {}               // locate → 404
class NearbyLoaded        extends NearbyFriendsState {
  final Building? building;          // null si se usa el plan B
  final List<NearbyFriend> friends;  // puede estar vacío
  final DateTime lastUpdated;
}
class NearbyError         extends NearbyFriendsState { final String message; }
```

Responsabilidades del controller:
- `init()`: permiso → posición → `PUT location` + `locate` → estado.
- Stream de `geolocator` con `distanceFilter: 20`; ignorar lecturas con `accuracy > 50 m`.
- Debounce (~2 s) y no lanzar una petición si ya hay otra en curso.
- Timers: 60 s → `GET` amigos; 10 min → `PUT location`.
- Pausar stream y timers en `AppLifecycleState.paused`, reanudar en `resumed`, cancelar en `dispose()`.

---

## 7. UI

**Header:** "Estás en **Biblioteca**" + "Actualizado hace 1 min".

**Lista** (`ListView` dentro de `RefreshIndicator`). Cada `FriendTile`:
- Avatar con iniciales.
- Nombre y carrera.
- "Aquí hace 5 min" (a partir de `locationUpdatedAt`).
- (Opcional) Botón de WhatsApp o llamada si `phoneNumber` no es null.

**Estados vacíos / error:**

| Estado | Texto | Acción |
|---|---|---|
| Sin permiso | "Activa la ubicación para ver amigos cerca" | Dar permiso / Abrir ajustes |
| GPS apagado | "Tu ubicación está desactivada" | Abrir ajustes de ubicación |
| Fuera de edificio | "No estás en ningún edificio del campus" | Reintentar |
| Sin amigos | "Ninguno de tus amigos está en Biblioteca ahora" | Ir a agregar amigos |
| Error | Mensaje del backend o "No pudimos cargar tus amigos" | Reintentar |

**Loading:** skeleton en la primera carga; en los refrescos, mantener la lista visible (sin spinner a pantalla completa).

---

## 8. Autenticación en las llamadas

- Header `Authorization: Bearer <accessToken>` en todas las peticiones.
- En `401`: `POST /auth/refresh` con el `refreshToken` → guardar el nuevo `accessToken` → reintentar **una** vez → si falla, ir al login.
- Hacerlo en `api_client.dart` (interceptor de `dio`), no en cada pantalla.
- `userId` = `id` del `AuthResponse` guardado al iniciar sesión.

---

## 9. Privacidad

- Explicar por qué se pide la ubicación **antes** de mostrar el diálogo del sistema.
- A los amigos solo se les muestra el **edificio**, nunca las coordenadas.
- Solo lo ven amigos `ACCEPTED` (ya lo filtra el backend).
- A futuro: modo invisible (necesita un campo nuevo en el backend).

---

## 10. Pruebas

- **Con Postman primero:** `PUT /nearby-friends/user/{id}/building/{buildingId}` con dos usuarios amigos en el mismo edificio → `GET /nearby-friends/user/{id}` debe devolver al otro. Probar también `/buildings/locate` (punto 3.3).
- **Unit:** `NearbyFriend.fromJson`, manejo de 404 en `locateBuilding`, lógica del controller con API y ubicación simuladas.
- **Widget:** cada estado de la sección 6 se ve como se espera.
- **Manual:** simular ubicación en el emulador (Android: Extended controls → Location; iOS Simulator: Features → Location) moviéndose entre dos edificios.

---

## 11. Checklist por fases

### Fase 0: Backend
- [ ] Traer a tu rama el código de `origin/CLASS-BLOCKS` (o esperar el merge a `main`) y levantar el backend.
- [ ] Probar en Postman las rutas de la sección 2.
- [ ] Verificar si `/buildings/locate` serializa bien el `Point`. Si no, pedir el cambio del DTO o usar el plan B.
- [ ] Acordar con el equipo usar solo `/nearby-friends/...`.

### Fase 1: UI con datos simulados
- [ ] Modelos `NearbyFriend` y `Building` con `fromJson`.
- [ ] `NearbyFriendsScreen` con todos los estados usando un repositorio falso.
- [ ] `FriendTile`, header y estados vacíos.
- [ ] Navegación hacia la pantalla.

### Fase 2: Ubicación
- [ ] `geolocator` + permisos Android/iOS.
- [ ] `LocationService`: permiso, posición actual, stream con `distanceFilter`.
- [ ] Manejar permiso denegado, denegado para siempre y GPS apagado.

### Fase 3: Conectar al backend
- [ ] `ApiClient` con Bearer + refresh en 401 + errores en texto plano.
- [ ] `NearbyFriendsApi` con las rutas reales.
- [ ] `PUT location` al moverse y cada 10 min; `GET` amigos cada 60 s; pull-to-refresh.
- [ ] Pausar/reanudar según el ciclo de vida de la app.

### Fase 4: Pulido
- [ ] "Aquí hace X min".
- [ ] Skeleton loading.
- [ ] Pruebas unitarias y de widget.
- [ ] Prueba manual moviéndose entre edificios.

### Futuro
- Notificación "Laura acaba de llegar a la Biblioteca" (la rama `PATRON-OBSERVER` ya tiene `NotificationController`; revisar si sirve).
- Ubicación en segundo plano.
- Modo invisible.
- Invitar a un amigo cercano a una actividad o mesa abierta.
