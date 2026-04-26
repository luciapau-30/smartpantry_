# Merge Change Log

A record of every merge done on `dev` — what was brought in, which files had conflicts, and exactly what was changed. For teammates reviewing the branch.

---

## Merge 1 — `archit-chenyang/integration` → `dev`
**Date:** 2026-04-25
**Commit:** `8ae8afc`
**Who:** Lucia (merge integrator)

### What came in
The entire Maven backend project — everything under `backend/`. Nothing on `dev` at the time existed inside that folder, so there were **zero conflicts**.

| Area | Files added |
|------|------------|
| Maven build | `backend/pom.xml` (Java 17, Jakarta Servlet 6, Gson, BCrypt) |
| User model | `model/User.java`, `model/Guest.java`, `model/Member.java`, `model/UserPublicView.java` |
| Content models | `model/content/Post.java`, `model/content/Recipe.java`, `model/content/Comment.java` |
| Auth API DTOs | `api/auth/LoginRequest.java`, `api/auth/RegisterRequest.java` |
| Member API DTOs | `api/member/AddToPantryRequest.java`, `api/member/PostCommentRequest.java`, `api/member/UploadRecipeRequest.java` |
| Auth servlets | `AuthLoginServlet`, `AuthLogoutServlet`, `AuthRegisterServlet`, `AuthMeServlet` |
| Member servlets | `AddToPantryServlet`, `PostCommentServlet`, `UploadRecipeServlet` |
| Guest servlet | `GuestRecipeServlet` (`GET /api/recipes`) |
| In-memory stores | `InMemoryUserStore`, `InMemoryRecipeRepository` (temporary stubs, to be replaced) |
| Interfaces | `UserStore`, `RecipeRepository` |
| Security | `PasswordHasher` (BCrypt), `RequestUsers`, `SessionAttributes` |
| JSON helpers | `GsonProvider`, `JsonApiResponse`, `AbstractJsonServlet` |
| Web wiring | `ContextKeys`, `CorsFilter`, `SmartPantryBootstrapListener` |
| DB connection | `JdbcConnectionFactory` (reads `PANTRY_DB_URL` env var or JNDI) |

### Conflicts resolved
| File | Resolution |
|------|------------|
| `.gitignore` | Auto-merged cleanly — no manual action needed |

---

## Merge 2 — `lucia/recommendation-engine` → `dev`
**Date:** 2026-04-25
**Who:** Lucia (merge integrator)

### What came in
The full recommendation engine pipeline plus a new `/api/member/recipes/recommend` endpoint. All new files landed with no conflict. Five existing files had conflicts — all were purely additive (Lucia added things, nobody deleted anything).

#### New files added (no conflicts)
| File | What it does |
|------|-------------|
| `recommendation/IngredientSynonymResolver.java` | Loads `synonyms.json`, strips adjective modifiers (fresh/dried/chopped/etc.), maps ingredient variants to canonical names |
| `recommendation/UnitNormalizer.java` | Converts quantities to common base units: volume → ml, weight → g, count → each |
| `recommendation/PantryMatcher.java` | Checks if a recipe ingredient is covered by the user's pantry; returns `CoverageResult(ingredientFound, quantityRatio)` |
| `recommendation/RecipeScorer.java` | Computes three sub-scores (pantry coverage, user preferences, popularity) each normalised 0–1 |
| `recommendation/RecipeRecommender.java` | Orchestrates the full pipeline: fetch all public recipes → score each → sort by total score descending |
| `recommendation/ScoreWeights.java` | Tunable weights w1/w2/w3 (defaults: 0.5 pantry, 0.3 preference, 0.2 popularity) |
| `recommendation/ScoredRecipe.java` | DTO carrying a recipe plus all its scores and missing-ingredient count |
| `recommendation/PantryItem.java` | Lightweight pantry item model used by the recommendation pipeline |
| `servlet/member/RecommendRecipesServlet.java` | `GET /api/member/recipes/recommend` — fetches member's pantry from DB, runs recommender, returns ranked list |
| `resources/synonyms.json` | Ingredient synonym mappings loaded by `IngredientSynonymResolver` |

#### Conflicted files — what changed
| File | Conflict type | What was kept |
|------|--------------|---------------|
| `.gitignore` | Both branches added different ignore rules | Kept both: `backend/target/`, `target/`, `.DS_Store` |
| `web/ContextKeys.java` | Lucia added `RECOMMENDER` constant | Kept Lucia's version — one new line added |
| `recipe/RecipeRepository.java` | Lucia added `listAllPublishedFull()` method + `Recipe` import | Kept Lucia's version — one new method added to interface |
| `recipe/InMemoryRecipeRepository.java` | Lucia added `publishedFull` list + `listAllPublishedFull()` implementation | Kept both — Archit's `published` list untouched, Lucia's `publishedFull` added alongside it |
| `web/SmartPantryBootstrapListener.java` | Lucia added recommender imports and wiring block | Kept Lucia's version — recommender pipeline is now built and stored at app startup |
| `jdbc/JdbcConnectionFactory.java` | Only the Javadoc comment differed; code was identical | Kept Archit's comment (more specific) |

---

## Step 3 — Port `zeqiang/database` DAOs into Maven project
**Date:** 2026-04-25
**Commit:** `715ccb2`
**Who:** Lucia (merge integrator)

### What was done
Zeqiang's standalone Eclipse DAO project was manually ported into the Maven backend. All files were placed under `backend/src/main/java/...` with correct package declarations. `Database.getConnection()` was replaced with `JdbcConnectionFactory.openConnection()` already present in the project.

#### Naming collisions resolved
Zeqiang's POJOs conflicted with Archit's domain models that share the same simple names. The DAO-layer row objects were renamed with a `Row` suffix:

| Zeqiang's original name | Renamed to | Why |
|------------------------|-----------|-----|
| `dao.Recipe` | `dao.RecipeRow` | Archit's `model.content.Recipe` already exists |
| `dao.PantryItem` | `dao.PantryItemRow` | Archit's `recommendation.PantryItem` already exists |
| `dao.Comment` | `dao.CommentRow` | Archit's `model.content.Comment` already exists |
| `dao.RecipeIngredient` | `dao.RecipeIngredientRow` | Archit's `model.content.Recipe.RecipeIngredient` already exists |
| `dao.User` | eliminated | Replaced by package-private `dao.UserRow` bridge DTO |

#### New files added
| File | What it does |
|------|-------------|
| `dao/UserRow.java` | Package-private DTO bridging `UserDao` → `JdbcUserStore`; not visible outside `dao` package |
| `dao/RecipeRow.java` | DB row POJO for RECIPES table |
| `dao/PantryItemRow.java` | DB row POJO for PANTRY_ITEMS table |
| `dao/CommentRow.java` | DB row POJO for COMMENTS table |
| `dao/RecipeIngredientRow.java` | DB row POJO for RECIPE_INGREDIENTS table |
| `dao/RecipeStep.java` | DB row POJO for RECIPE_STEPS table (no collision) |
| `dao/Ingredient.java` | DB row POJO for INGREDIENTS table (no collision) |
| `dao/UserAllergy.java` | DB row POJO for USER_ALLERGIES table (no collision) |
| `dao/UserDao.java` | CRUD for USERS table; returns `UserRow` |
| `dao/PantryItemDao.java` | CRUD for PANTRY_ITEMS; includes `getExpiringSoon()` for background thread |
| `dao/IngredientDao.java` | CRUD for INGREDIENTS table |
| `dao/RecipeIngredientDao.java` | CRUD for RECIPE_INGREDIENTS table |
| `dao/RecipeStepDao.java` | CRUD for RECIPE_STEPS table |
| `dao/RecipeLikeDao.java` | Upsert like/dislike for RECIPE_LIKES table |
| `dao/RecipeSaveDao.java` | Save/unsave recipes for RECIPE_SAVES table |
| `dao/CommentDao.java` | Threaded comments with soft-delete for COMMENTS table |
| `dao/UserAllergyDao.java` | CRUD for USER_ALLERGIES table |
| `dao/JdbcUserStore.java` | Implements `UserStore` interface; maps `UserRow` → `Member` or `Guest` |
| `recipe/RecipeDao.java` | CRUD for RECIPES table; ported from zeqiang, returns `RecipeRow` |
| `recipe/JdbcRecipeRepository.java` | Implements `RecipeRepository`; builds full `Recipe` domain objects from DB for the recommendation engine |

#### Bootstrap wiring updated
`SmartPantryBootstrapListener` now checks for `PANTRY_DB_URL` at startup:
- **If set:** wires `JdbcRecipeRepository` + `JdbcUserStore` (real database)
- **If not set:** falls back to `InMemoryRecipeRepository` + `InMemoryUserStore` (demo/dev mode, no DB needed)

Frontend developers can still run the app without MySQL — nothing breaks.

#### MySQL dependency added
`pom.xml` now includes `mysql-connector-j 8.3.0`.

#### Build verified
`mvn clean package -DskipTests` → **BUILD SUCCESS** after this port.

---

## Step 4 — Missing REST endpoints added to `dev`
**Date:** 2026-04-25
**Commit:** `3434754`
**Who:** Lucia

### What was added
All endpoints were missing from the backend — DAOs existed but no servlet exposed them over HTTP.

| New file | Endpoint | What it does |
|----------|----------|-------------|
| `servlet/member/GetPantryServlet.java` | `GET /api/member/pantry` | Returns the logged-in member's full pantry item list |
| `servlet/member/DeletePantryItemServlet.java` | `DELETE /api/member/pantry/{id}` | Deletes a pantry item; verifies ownership before deleting |
| `servlet/member/GetMyRecipesServlet.java` | `GET /api/member/recipes/mine` | Lists all recipes uploaded by the logged-in member |
| `servlet/member/GetSavedRecipesServlet.java` | `GET /api/member/recipes/saved` | Lists all recipes the member has saved |
| `servlet/member/RecipeInteractionServlet.java` | `POST /api/member/recipes/{id}/like` | Like or dislike a recipe; body `{"isLike": true/false}`, omit to remove reaction |
| `servlet/member/RecipeInteractionServlet.java` | `POST /api/member/recipes/{id}/save` | Save or unsave a recipe; body `{"save": true/false}` |
| `servlet/RecipeDetailServlet.java` | `GET /api/recipes/{id}` | Full recipe detail: ingredients, steps, like/dislike/save counts, caller's own reaction |
| `servlet/RecipeDetailServlet.java` | `GET /api/recipes/{id}/comments` | Top-level threaded comments for a recipe |
| `servlet/TrendingRecipesServlet.java` | `GET /api/recipes/trending` | Top 20 recipes by likes in the last 24 hours |
| `servlet/TopRecipesServlet.java` | `GET /api/recipes/top` | Top 20 recipes by all-time like count |

### How trending/top works (two-step approach)
Instead of a complex SQL JOIN, these use two simple steps in Java:
1. Query `RECIPE_LIKES` for the top recipe IDs ordered by like count
2. Loop and call `RecipeDao.getById(id)` for each ID

Two new methods added to `RecipeLikeDao`: `getTrendingRecipeIds(limit)` and `getTopLikedRecipeIds(limit)`.

### URL routing note
`RecipeInteractionServlet` is mapped to `/api/member/recipes/*` (wildcard). The Servlet spec gives exact-pattern mappings higher priority, so the existing fixed endpoints (`/recommend`, `/upload`, `/mine`, `/saved`) are unaffected. Same applies to `RecipeDetailServlet` at `/api/recipes/*` — `/api/recipes/trending` and `/api/recipes/top` still route to their own servlets.

### Build verified
`mvn clean package -DskipTests` → **BUILD SUCCESS** (72 source files)

---

## Step 5 — T2/T3/T4/F14: thread pool, background expiry job, WebSocket alerts
**Date:** 2026-04-26
**Branch:** `lucia/background-thread-websocket` (off `dev`)

### What was added
A managed thread pool, a periodic expiry-check job that runs on it, a Jakarta WebSocket endpoint that authenticated clients connect to, and a small broadcaster facade that lets servlets and background jobs push events without depending on the WebSocket API directly. Together these satisfy CSCI 201 rubric items T2 (thread pool), T3 (background thread), T4 (WebSockets), and the user-facing F14 (expiration alerts).

| New file | Purpose |
|----------|---------|
| `background/BackgroundJobs.java` | T2: wraps a 4-thread daemon `ScheduledExecutorService`; `scheduleAtFixedRate` swallows `Throwable` so a buggy job can't kill the pool; `shutdown()` waits up to 5s on context-destroy |
| `background/ExpiryCheckJob.java` | T3 + F14: every 5 min, iterates *currently connected* userIds, calls `PantryItemDao.getExpiringSoonWithName(userId, 3)`, and for each row not previously alerted fires `PantryEventBroadcaster.expiringItem(...)`. No-ops cleanly when `PANTRY_DB_URL` is unset (dev mode) |
| `websocket/PantryWebSocket.java` | T4: `@ServerEndpoint("/ws/pantry")`; custom `Configurator` lifts the `smartpantry.userId` attribute from `HttpSession` into the per-connection `EndpointConfig` so `onOpen` can identify the caller; tracks `ConcurrentHashMap<String, Set<Session>>` for multi-tab fan-out; `pushToUser(userId, payload)` is the only outbound API |
| `websocket/PantryEventBroadcaster.java` | Thin facade in front of `PantryWebSocket`; servlets/jobs call `pantryUpdated(...)` or `expiringItem(...)` so they don't depend on the WebSocket framework |
| `websocket/AlertPayload.java` | Serialised by Gson into the WS frame. Two `type` values today: `EXPIRING_ITEM` and `PANTRY_UPDATED` |
| `dao/ExpiringPantryItem.java` | DTO for the joined PANTRY_ITEMS × INGREDIENTS result row so payloads carry a human-readable `ingredientName` |

| Modified file | Change |
|---------------|--------|
| `pom.xml` | Added `jakarta.websocket:jakarta.websocket-api:2.1.1` **and** `jakarta.websocket:jakarta.websocket-client-api:2.1.1` (both `provided`). In Jakarta WebSocket 2.1 the API was split — the client-api artifact owns `Session`, `OnOpen`, `OnClose`, `EndpointConfig`, `CloseReason`, etc., while the server-api artifact owns `@ServerEndpoint`, `ServerEndpointConfig`, `HandshakeRequest`. Tomcat 10 ships both at runtime |
| `web/ContextKeys.java` | Added `BACKGROUND_JOBS` and `EVENT_BROADCASTER` constants |
| `web/SmartPantryBootstrapListener.java` | Builds the broadcaster + thread pool in `contextInitialized`, schedules `ExpiryCheckJob` at 5-min fixed rate (initial delay 30s), and now implements `contextDestroyed` to call `BackgroundJobs.shutdown()` so Tomcat redeploys cleanly |
| `dao/PantryItemDao.java` | Added `getExpiringSoonWithName(userId, withinDays)` — `LEFT JOIN INGREDIENTS i ON i.id = p.ingredient_id` so callers get `ingredientName` without a per-row round-trip. Existing `getExpiringSoon(...)` left untouched |
| `servlet/member/AddToPantryServlet.java` | After a successful insert, fires `broadcaster.pantryUpdated(userId, "added", pantryItemId)` for live cross-tab pantry sync |
| `servlet/member/DeletePantryItemServlet.java` | After a successful delete, fires `broadcaster.pantryUpdated(userId, "removed", itemId)` |

### Design notes
- **Why scope to connected users:** the expiry job calls `PantryWebSocket.connectedUserIds()` first and skips the DB entirely if nobody is listening. With a small user base this is also faster than a global `WHERE expiration_date <= NOW() + N` over the whole table.
- **Why dedupe per-tick:** without it, the user would get the same "Tomatoes expire in 2 days" toast every 5 minutes. The job keeps a `Set<pantryItemId>` per user and only alerts the first time it sees an id; the set is pruned to whatever the DB still returns each tick, so an item that gets used (and falls out of the window) can re-alert later if the user puts a new batch in the pantry.
- **Why `provided` scope on the websocket deps:** Tomcat 10 already ships these on the container classpath; bundling them in the WAR would cause `LinkageError` at deploy time.
- **Auth model:** the existing HTTP login flow stays untouched. The browser sends `JSESSIONID` on the WebSocket handshake automatically; the `HttpSessionConfigurator` reads `SessionAttributes.CURRENT_USER_ID` from the session and stashes it on the per-connection `EndpointConfig`. Unauthenticated handshakes are closed with `VIOLATED_POLICY` on `onOpen`.

### Build verified
`mvn clean package -DskipTests` → **BUILD SUCCESS** (78 source files, was 72).

### Frontend wire-up (for Alijah)
```js
const ws = new WebSocket(`ws://${location.host}/smartpantry/ws/pantry`);
ws.onmessage = (e) => {
    const msg = JSON.parse(e.data);
    if (msg.type === 'EXPIRING_ITEM') {
        // toast: `${msg.ingredientName} expires in ${msg.daysLeft} day(s)`
    } else if (msg.type === 'PANTRY_UPDATED') {
        // re-fetch GET /api/member/pantry
    }
};
```

