# Guía de commits por funcionalidad

La rama `base-crud` tiene solo entidades, enums, DTOs y CRUD básico. Esta guía explica cómo agregar encima, commit por commit, cada funcionalidad de la versión final (`main`) hasta que el proyecto quede idéntico a `main`.

## Reglas generales

- Sigan los pasos **en orden**. Cada paso depende de los anteriores y el proyecto compila después de cada uno.
- Cada integrante hace el commit de su funcionalidad con **su propia cuenta de git**.
- Todo se trae desde `main` (la versión final). Si su `main` local no está actualizado, primero corran `git fetch` y usen `origin/main` en lugar de `main` en los comandos.
- Hay dos formas de traer cambios:
  - **Archivo completo**: `git checkout main -- <ruta>` copia el archivo tal como está en `main`. Se usa cuando todo lo que falta del archivo pertenece a ese paso.
  - **A mano**: cuando el archivo mezcla varias funcionalidades, se copian solo los métodos indicados. Para ver el archivo final sin cambiar de rama: `git show main:<ruta>`.
- Después de cada paso, antes del commit, verifiquen que compila:
  ```bash
  ./mvnw -q -DskipTests clean package
  ```
- Para abreviar, en los comandos `J` es la carpeta del código:
  ```bash
  J=src/main/java/com/backend/gapfinder
  ```

## Orden de las funcionalidades

| # | Funcionalidad | Punto del enunciado | Depende de |
|---|---|---|---|
| 1 | Notificaciones (patrón Observer) | Lógica de negocio | — |
| 2 | Usuarios: búsqueda e intereses | Lógica de negocio | — |
| 3 | Amistades: aceptar, rechazar y lista de amigos | Lógica de negocio | 1 |
| 4 | Matches: solicitud, respuesta y cierre automático | Lógica de negocio | 1 |
| 5 | Mesas abiertas: unirse, salir y cierre automático | Lógica de negocio | 1 |
| 6 | Gaps semanales | Lógica de negocio | 4 |
| 7 | Sensor GPS | a) | — |
| 8 | Context aware (amigos cercanos) | c) | 3, 7 |
| 9 | Smart features (patrón Strategy) | d) | 4, 5, 6 |
| 10 | Business questions y analítica | b) | 5, 6, 9 |
| 11 | Autenticación (JWT) | e) | 2 (comparten `UserRepository`) |
| 12 | Servicios externos (Google Calendar) | f) | 6, 11 |

Los patrones de diseño entran con su funcionalidad: **Observer** en el paso 1 (eventos y listener) y en los pasos 3, 4 y 5 (los `publishEvent`); **Strategy** en el paso 9.

---

## Paso 1. Notificaciones (patrón Observer)

**Punto del enunciado:** lógica de negocio.

**Archivos nuevos:**
- `events/FriendshipEvent.java`
- `events/MatchEvent.java`
- `events/OpenTableEvent.java`
- `listeners/NotificationListener.java`
- `services/NotificationService.java`
- `controllers/NotificationController.java`

**Archivos modificados:**
- `repositories/NotificationRepository.java`

**Comandos:**
```bash
git checkout main -- $J/events $J/listeners \
  $J/services/NotificationService.java \
  $J/controllers/NotificationController.java \
  $J/repositories/NotificationRepository.java
```

**Commit sugerido:** `Agregar notificaciones con patrón Observer (eventos y listener)`

---

## Paso 2. Usuarios: búsqueda por nombre e intereses

**Punto del enunciado:** lógica de negocio.

**Archivos nuevos:** ninguno.

**Archivos modificados:**
- `services/UserService.java` (métodos `addInterest` y `searchByName`)
- `controllers/UserController.java` (`POST /users/{userId}/interests/{interestId}` y `GET /users/search`)
- `repositories/UserRepository.java`

**Comandos:**
```bash
git checkout main -- $J/services/UserService.java $J/controllers/UserController.java
```

**A mano en `repositories/UserRepository.java`:** no traigan el archivo completo, porque también tiene métodos de autenticación (paso 11). Agreguen:

1. Debajo de `import org.springframework.stereotype.Repository;`, una línea en blanco y:
   ```java
   import java.util.List;
   ```
2. Dentro de la interfaz:
   ```java
       // Finds users whose name contains the text, ignoring case
       List<UserModel> findByNameContainingIgnoreCase(String name);
   ```

**Commit sugerido:** `Agregar búsqueda de usuarios por nombre y asignación de intereses`

---

## Paso 3. Amistades: aceptar, rechazar y lista de amigos

**Punto del enunciado:** lógica de negocio. Publica eventos (Observer), por eso va después del paso 1.

**Archivos nuevos:** ninguno.

**Archivos modificados:**
- `services/FriendshipService.java` (evento en `create`, `acceptFriendship`, `rejectFriendship`, `getFriendsByUser` y validaciones)
- `controllers/FriendshipController.java` (`GET /friendships/user/{userId}/friends`, `PATCH /friendships/{id}/accept` y `PATCH /friendships/{id}/reject`)
- `repositories/FriendshipRepository.java` (`findByUserAndStatus`)

**Comandos:**
```bash
git checkout main -- $J/services/FriendshipService.java \
  $J/controllers/FriendshipController.java \
  $J/repositories/FriendshipRepository.java
```

**Commit sugerido:** `Agregar flujo de amistades: aceptar, rechazar y lista de amigos`

---

## Paso 4. Matches: solicitud, respuesta y cierre automático

**Punto del enunciado:** lógica de negocio. Publica eventos (Observer), por eso va después del paso 1.

**Archivos nuevos:** ninguno.

**Archivos modificados (todo a mano):**
- `services/MatchService.java`
- `controllers/MatchController.java`
- `repositories/MatchRepository.java`

No traigan `MatchService` ni `MatchController` completos, porque en `main` también tienen la búsqueda de candidatos, que es del paso 9.

**A mano en `services/MatchService.java`:**

1. Imports. Agreguen:
   ```java
   import com.backend.gapfinder.enums.MatchStatusEnum;
   import com.backend.gapfinder.enums.NotificationTypeEnum;
   import com.backend.gapfinder.events.MatchEvent;
   import org.springframework.context.ApplicationEventPublisher;
   import org.springframework.scheduling.annotation.Scheduled;
   import java.time.LocalDateTime;
   ```
2. Campo y constructor. Agreguen el `ApplicationEventPublisher`:
   ```java
       private final MatchRepository matchRepository;
       private final GapService gapService;
       private final ApplicationEventPublisher eventPublisher;

       public MatchService(MatchRepository matchRepository, GapService gapService,
                            ApplicationEventPublisher eventPublisher) {
           this.matchRepository = matchRepository;
           this.gapService = gapService;
           this.eventPublisher = eventPublisher;
       }
   ```
3. Métodos. Copien desde `main`, al final de la clase y con sus comentarios, todos los métodos que en `main` están **después de `findCandidates`**:
   - `sendMatchRequest`
   - `acceptMatch`
   - `rejectMatch`
   - `validateAcceptor`
   - `completeMatch`
   - `validatePending`
   - `completeExpiredMatches` (el `@Scheduled`)
   - `getPendingReceived`

   **No copien `findCandidates`.**

**A mano en `controllers/MatchController.java`:**

1. Copien desde `main` estos endpoints y péguenlos entre `getAll` y `createMatch`, en este orden:
   - `sendMatchRequest` (`POST /matches/request`)
   - `acceptMatch` (`PATCH /matches/{id}/accept`)
   - `rejectMatch` (`PATCH /matches/{id}/reject`)
   - `completeMatch` (`PATCH /matches/{id}/complete`)
2. Copien `getPendingReceived` (`GET /matches/user/{userId}/pending`) al final de la clase.
3. **No copien `findCandidates`** ni el import de `MatchCandidateResponseDTO`.

**A mano en `repositories/MatchRepository.java`:** debe quedar así:
```java
package com.backend.gapfinder.repositories;

import com.backend.gapfinder.enums.MatchStatusEnum;
import com.backend.gapfinder.models.MatchModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchModel, Long> {

    // Get all matches with a given status (used to auto-complete expired ones)
    List<MatchModel> findByStatus(MatchStatusEnum status);

    // Get the matches with a given status whose acceptor gap belongs to the given user
    List<MatchModel> findByAcceptorGapUserIdAndStatus(Long userId, MatchStatusEnum status);
}
```

**Commit sugerido:** `Agregar flujo de matches: solicitud, aceptar, rechazar, completar y pendientes`

---

## Paso 5. Mesas abiertas: unirse, salir y cierre automático

**Punto del enunciado:** lógica de negocio. Publica eventos (Observer), por eso va después del paso 1.

**Archivos nuevos:** ninguno.

**Archivos modificados:**
- `services/OpenTableParticipantService.java` (evento en `create`, `getByOpenTable`, `join` con cambio a `FULL`, y `leave`)
- `controllers/OpenTableParticipantController.java` (`GET /table/{tableId}`, `POST /table/{tableId}/join` y `DELETE /table/{tableId}/leave`)
- `repositories/OpenTableParticipantRepository.java` (a mano)
- `services/OpenTableService.java` (a mano: `closeExpiredTables`)
- `repositories/OpenTableRepository.java` (a mano: `findByStatusInAndEndTimeBefore`)

**Comandos:**
```bash
git checkout main -- $J/services/OpenTableParticipantService.java \
  $J/controllers/OpenTableParticipantController.java
```

**A mano en `repositories/OpenTableParticipantRepository.java`:** no traigan el archivo completo, porque `countByJoinedAtGreaterThanEqual` es de analítica (paso 10).

1. Imports, antes de `import org.springframework.data.jpa.repository.JpaRepository;`:
   ```java
   import java.util.List;
   import java.util.Optional;
   ```
2. Dentro de la interfaz, debajo de `existsByOpenTableIdAndUserId`:
   ```java
       // Counts the total number of participants for a given open table ID
       long countByOpenTableId(Long openTableId);

       // Retrieves all participant records associated with a specific open table ID
       List<OpenTableParticipantModel> findByOpenTableId(Long openTableId);

       // Finds a specific participant record by open table ID and user ID, wrapped in an Optional
       Optional<OpenTableParticipantModel> findByOpenTableIdAndUserId(Long openTableId, Long userId);
   ```

**A mano en `services/OpenTableService.java`:** no traigan el archivo completo, porque `countCreatedSince` es de analítica (paso 10).

1. Imports:
   ```java
   import com.backend.gapfinder.enums.OpenTableStatusEnum;
   import com.backend.gapfinder.repositories.OpenTableParticipantRepository;
   import org.springframework.scheduling.annotation.Scheduled;
   ```
2. Agreguen el campo `openTableParticipantRepository` como segundo campo y como segundo parámetro del constructor:
   ```java
       private final OpenTableRepository openTableRepository;
       private final OpenTableParticipantRepository openTableParticipantRepository;
       ...
       public OpenTableService(OpenTableRepository openTableRepository,
                               OpenTableParticipantRepository openTableParticipantRepository,
                               UserService userService,
                               ...
           this.openTableRepository = openTableRepository;
           this.openTableParticipantRepository = openTableParticipantRepository;
   ```
3. Copien desde `main` el método `closeExpiredTables` (con `@Scheduled(fixedRate = 60000)`) entre `delete` y `validateOpenTableData`.

**A mano en `repositories/OpenTableRepository.java`:**

1. Imports:
   ```java
   import com.backend.gapfinder.enums.OpenTableStatusEnum;

   import java.time.LocalDateTime;
   import java.util.List;
   ```
2. Dentro de la interfaz:
   ```java
       // Find the open tables with any of the given statuses whose end time is before the given time
       List<OpenTableModel> findByStatusInAndEndTimeBefore(List<OpenTableStatusEnum> statuses, LocalDateTime time);
   ```

**Commit sugerido:** `Agregar unirse y salir de mesas abiertas, estado FULL y cierre automático de mesas vencidas`

---

## Paso 6. Gaps semanales

**Punto del enunciado:** lógica de negocio. Usa `MatchRepository.existsByProposerGapIdOrAcceptorGapId`, por eso va después del paso 4.

**Archivos nuevos:**
- `services/WeeklyGapService.java`
- `services/WeeklyGapJob.java`
- `src/test/java/com/backend/gapfinder/services/WeeklyGapServiceTest.java`

**Archivos modificados:**
- `controllers/GapController.java` (`POST /gaps/user/{userId}/generate-week` y `GET /gaps/user/{userId}`)
- `repositories/ClassBlockRepository.java` (`findDistinctUserIds`)
- `repositories/MatchRepository.java` (`existsByProposerGapIdOrAcceptorGapId`)
- `repositories/GapRepository.java` (a mano: `findByUserAndStartBetween`)
- `src/main/resources/application.properties` (a mano: propiedades `gaps.*`)

**Comandos:**
```bash
git checkout main -- $J/services/WeeklyGapService.java $J/services/WeeklyGapJob.java \
  $J/controllers/GapController.java \
  $J/repositories/ClassBlockRepository.java $J/repositories/MatchRepository.java \
  src/test/java/com/backend/gapfinder/services/WeeklyGapServiceTest.java
```

**A mano en `repositories/GapRepository.java`:** no traigan el archivo completo, porque tiene queries de los pasos 9 y 10.

1. Imports:
   ```java
   import org.springframework.data.jpa.repository.Query;
   import org.springframework.data.repository.query.Param;

   import java.time.LocalDateTime;
   import java.util.List;
   ```
2. Copien desde `main` el método `findByUserAndStartBetween`, con su `@Query` y su comentario.

**A mano en `src/main/resources/application.properties`:** agreguen al final:
```properties

# Weekly gaps calculated from the class schedule
gaps.day-start=06:30
gaps.day-end=21:30
gaps.min-minutes=30
gaps.weekly-cron=0 0 20 * * SUN
```

**Commit sugerido:** `Agregar cálculo semanal de gaps desde el horario de clases`

---

## Paso 7. Sensor GPS

**Punto del enunciado:** a).

**Archivos nuevos:**
- `config/GeometryConfig.java`

**Archivos modificados:**
- `services/BuildingService.java` (`GeometryFactory` y `findBuildingContainingUser`)
- `repositories/BuildingRepository.java` (`findBuildingAtUserLocation`)
- `controllers/BuildingController.java` (`GET /buildings/locate`)

**Comandos:**
```bash
git checkout main -- $J/config/GeometryConfig.java $J/services/BuildingService.java \
  $J/repositories/BuildingRepository.java $J/controllers/BuildingController.java
```

**Commit sugerido:** `Agregar sensor GPS: ubicar el edificio a partir de coordenadas`

---

## Paso 8. Context aware (amigos cercanos)

**Punto del enunciado:** c). Usa el sensor GPS (paso 7) y la lista de amigos (paso 3).

**Archivos nuevos:**
- `services/NearbyFriendsService.java`
- `controllers/NearbyFriendsController.java`
- `controllers/UserContextController.java`

**Archivos modificados:** ninguno.

**Comandos:**
```bash
git checkout main -- $J/services/NearbyFriendsService.java \
  $J/controllers/NearbyFriendsController.java $J/controllers/UserContextController.java
```

**Commit sugerido:** `Agregar context aware: ubicación actual del usuario y amigos cercanos`

---

## Paso 9. Smart features (patrón Strategy)

**Punto del enunciado:** d).

**Archivos nuevos:**
- `strategies/MatchingStrategy.java`
- `strategies/OverlapStrategy.java`
- `strategies/SameCareerStrategy.java`
- `strategies/SharedInterestStrategy.java`
- `strategies/EffortStrategy.java`
- `services/RecommendationService.java`
- `controllers/RecommendationController.java`
- `repositories/projections/FavoriteBuildingProjection.java`
- `dto/MatchSearchCriteriaDTO.java`
- `dto/responses/MatchCandidateResponseDTO.java`
- `dto/responses/OpenTableRecommendationResponseDTO.java`

**Archivos modificados:**
- `services/MatchService.java` (estrategias y `findCandidates`; queda igual a `main`)
- `controllers/MatchController.java` (`GET /matches/gap/{gapId}/candidates`; queda igual a `main`)
- `repositories/UserLocationLogRepository.java` (a mano: `findFavoriteBuilding`)
- `repositories/OpenTableRepository.java` (a mano: `findMatchingOpenTables`)
- `repositories/GapRepository.java` (a mano: `findOverlappingGapsExcludingUser`)

**Comandos:**
```bash
git checkout main -- $J/strategies \
  $J/services/RecommendationService.java $J/controllers/RecommendationController.java \
  $J/repositories/projections/FavoriteBuildingProjection.java \
  $J/dto/MatchSearchCriteriaDTO.java \
  $J/dto/responses/MatchCandidateResponseDTO.java \
  $J/dto/responses/OpenTableRecommendationResponseDTO.java \
  $J/services/MatchService.java $J/controllers/MatchController.java
```

**A mano en `repositories/UserLocationLogRepository.java`:** no traigan el archivo completo, porque `findGapPresenceByBuilding` es de analítica (paso 10).

1. Imports:
   ```java
   import com.backend.gapfinder.repositories.projections.FavoriteBuildingProjection;

   import java.util.Optional;

   import org.springframework.data.jpa.repository.Query;
   import org.springframework.data.repository.query.Param;
   ```
2. Copien desde `main` el método `findFavoriteBuilding`, con su `@Query` nativa y sus comentarios (`// SMART-FEATURE`).

**A mano en `repositories/OpenTableRepository.java`:**

1. Imports:
   ```java
   import org.springframework.data.jpa.repository.Query;
   import org.springframework.data.repository.query.Param;
   ```
2. Copien desde `main` el método `findMatchingOpenTables` como **primer** método de la interfaz, con su `@Query` y sus comentarios.

**A mano en `repositories/GapRepository.java`:** copien desde `main` el método `findOverlappingGapsExcludingUser` como **primer** método de la interfaz, con su `@Query` y su comentario.

**Commit sugerido:** `Agregar smart features: búsqueda de candidatos con patrón Strategy y recomendación de mesas`

---

## Paso 10. Business questions y analítica

**Punto del enunciado:** b). Incluye BQ 3 tipo 2 grupal (abandonos), BQ 5 (cobertura de gaps) y BQ 11 (presencia por edificio).

**Archivos nuevos:**
- `services/AnalyticsService.java`
- `controllers/AnalyticsController.java`
- `dto/OpenTableAbandonmentStatsBasicDTO.java`
- `dto/responses/GapCoverageResponseDTO.java`
- `dto/responses/BuildingGapPresenceResponseDTO.java`
- `repositories/projections/BuildingGapPresenceProjection.java`

**Archivos modificados** (en este paso todos quedan iguales a `main`, así que se traen completos):
- `services/OpenTableAbandonmentService.java` (`countByStep`, `calculateAbandonmentRateByStep` y `getMostAbandonedStep`)
- `controllers/OpenTableAbandonmentController.java` (`/count-by-step`, `/stats` y `/stats/most-abandoned`)
- `repositories/OpenTableAbandonmentRepository.java` (`countGroupedByStep`)
- `services/OpenTableService.java` (`countCreatedSince`)
- `controllers/OpenTableController.java` (`GET /open-tables/count`)
- `repositories/OpenTableRepository.java` (`countByCreatedAtGreaterThanEqual`)
- `repositories/OpenTableParticipantRepository.java` (`countByJoinedAtGreaterThanEqual`)
- `repositories/GapRepository.java` (`findCoverageByDurationBucket`)
- `repositories/UserLocationLogRepository.java` (`findGapPresenceByBuilding`)

**Comandos:**
```bash
git checkout main -- $J/services/AnalyticsService.java $J/controllers/AnalyticsController.java \
  $J/dto/OpenTableAbandonmentStatsBasicDTO.java \
  $J/dto/responses/GapCoverageResponseDTO.java $J/dto/responses/BuildingGapPresenceResponseDTO.java \
  $J/repositories/projections/BuildingGapPresenceProjection.java \
  $J/services/OpenTableAbandonmentService.java $J/controllers/OpenTableAbandonmentController.java \
  $J/repositories/OpenTableAbandonmentRepository.java \
  $J/services/OpenTableService.java $J/controllers/OpenTableController.java \
  $J/repositories/OpenTableRepository.java $J/repositories/OpenTableParticipantRepository.java \
  $J/repositories/GapRepository.java $J/repositories/UserLocationLogRepository.java
```

**Commit sugerido:** `Agregar business questions: abandono de mesas, cobertura de gaps y presencia por edificio`

> Si cada BQ debe quedar en el commit de su autor, este paso se puede dividir en tres commits. `AnalyticsService` y `AnalyticsController` mezclan las tres BQ, así que cada commit copia a mano solo su sección `// ==== BQ n ====` y ajusta el constructor. El último commit trae los archivos completos.

---

## Paso 11. Autenticación (JWT)

**Punto del enunciado:** e).

**Archivos nuevos:**
- `controllers/AuthController.java`
- `services/AuthService.java`
- `services/JwtService.java`
- `services/RefreshTokenService.java`
- `security/JwtAuthFilter.java`
- `security/SecurityConfig.java`
- `exceptions/AuthExceptionHandler.java`
- `dto/request/LoginRequest.java`
- `dto/request/RefreshRequest.java`
- `dto/request/RegisterRequest.java`
- `dto/responses/AuthResponse.java`

**Archivos modificados:**
- `repositories/UserRepository.java` (`findByEmail` y `existsByEmail`; queda igual a `main`)
- `repositories/RefreshTokenRepository.java` (`findByToken` y `deleteByUserId`)
- `src/main/resources/application.properties` (propiedades `jwt.*`; queda igual a `main`)
- `pom.xml` (a mano: Spring Security y JWT)

**Comandos:**
```bash
git checkout main -- $J/controllers/AuthController.java \
  $J/services/AuthService.java $J/services/JwtService.java $J/services/RefreshTokenService.java \
  $J/security $J/exceptions/AuthExceptionHandler.java \
  $J/dto/request $J/dto/responses/AuthResponse.java \
  $J/repositories/UserRepository.java $J/repositories/RefreshTokenRepository.java \
  src/main/resources/application.properties
```

**A mano en `pom.xml`:** no traigan el archivo completo, porque también tiene las dependencias de Google (paso 12). Debajo de la dependencia `spring-boot-starter-web`, agreguen:
```xml

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>


        <!-- ========================= -->
        <!-- JWT -->
        <!-- ========================= -->

        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>

        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
```

> A partir de este paso todos los endpoints, excepto `/auth/**` y `/google/callback`, piden token JWT.

**Commit sugerido:** `Agregar autenticación con JWT y refresh tokens`

---

## Paso 12. Servicios externos (Google Calendar)

**Punto del enunciado:** f). Usa el usuario autenticado (paso 11) y el cálculo de gaps semanales (paso 6).

**Archivos nuevos:**
- `controllers/GoogleAuthController.java`
- `controllers/GoogleImportController.java`
- `config/GoogleOAuthConfig.java`
- `services/GoogleCalendarService.java`
- `services/GoogleScheduleImportService.java`
- `mapper/GoogleEventMapper.java`
- `dto/responses/GoogleImportResult.java`
- `src/main/resources/application.yml`

**Archivos modificados:**
- `repositories/GoogleCredentialRepository.java` (`findByUserId`)
- `pom.xml` (dependencias de Google; queda igual a `main`)

**Comandos:**
```bash
git checkout main -- $J/controllers/GoogleAuthController.java $J/controllers/GoogleImportController.java \
  $J/config/GoogleOAuthConfig.java \
  $J/services/GoogleCalendarService.java $J/services/GoogleScheduleImportService.java \
  $J/mapper $J/dto/responses/GoogleImportResult.java \
  $J/repositories/GoogleCredentialRepository.java \
  pom.xml src/main/resources/application.yml
```

**Commit sugerido:** `Agregar importación del horario desde Google Calendar`

---

## Verificación final

Después del paso 12 el código debe ser idéntico a `main`. Este comando no debe mostrar ningún archivo, salvo esta guía:
```bash
git diff main --stat
```
