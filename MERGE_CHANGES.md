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

## Step 5 — Background expiry thread + WebSocket push alerts
**Date:** 2026-04-26
**Commit:** `4388609`
**Who:** Lucia

### What was added

#### Backend — new files
| File | What it does |
|------|-------------|
| `websocket/HttpSessionConfigurator.java` | `ServerEndpointConfig.Configurator` subclass; copies the HTTP session into the WebSocket handshake properties so the endpoint can identify the user |
| `websocket/AlertWebSocketEndpoint.java` | `@ServerEndpoint("/ws/alerts")`; maintains a `ConcurrentHashMap<userId, Session>`; exposes `sendToUser()` and `getConnectedUserIds()` for the background thread |
| `web/ExpiryCheckerThread.java` | Single-daemon-thread `ScheduledExecutorService`; fires `checkAndNotify()` every 60 minutes; queries `PantryItemDao.getAllExpiringSoon(3)`, groups results by userId, pushes JSON alerts to connected users only |

#### Backend — modified files
| File | Change |
|------|--------|
| `dao/PantryItemDao.java` | Added `getAllExpiringSoon(int withinDays)` — queries all users' items expiring within N days (no userId filter; background thread needs all users) |
| `dao/RecipeLikeDao.java` | Added `getTrendingRecipeIds(limit)` and `getTopLikedRecipeIds(limit)` using a shared private helper |
| `web/ContextKeys.java` | Added `EXPIRY_CHECKER` constant |
| `web/SmartPantryBootstrapListener.java` | Added block to create and start `ExpiryCheckerThread` at app startup; added `contextDestroyed()` to stop it cleanly |
| `pom.xml` | Added `jakarta.websocket-client-api:2.1.0` (provided) — required in addition to `jakarta.websocket-api` because the 2.1 API is split: `websocket-api` is server-only and is missing `Session`, `OnOpen`, `OnClose`, `OnError`, `EndpointConfig`; Tomcat ships both at runtime |

#### Frontend — modified files
| File | Change |
|------|--------|
| `FrontendCode/auth.js` | Added `connectAlertSocket()` — opens `ws://.../ws/alerts`, handles `expiry_alert` messages, auto-reconnects after 10s if still logged in. Added `disconnectAlertSocket()` — called on logout, clears `window._alertWs`. Added `showExpiryToast(items)` — dynamically creates a fixed purple toast showing item count and soonest expiry days. Called from `onLoggedIn`/`onLoggedOut` respectively. |

### Alert JSON format
```json
{ "type": "expiry_alert", "items": [{ "id": "...", "ingredientId": "...", "daysLeft": 2, "expirationDate": "2026-04-28" }] }
```

### WebSocket dependency note
`jakarta.websocket-api:2.1.0` on Maven Central is server-side only. The shared/client-side classes (`Session`, `OnOpen`, `OnClose`, `OnError`, `EndpointConfig`, `HandshakeResponse`) are published as a separate `jakarta.websocket-client-api:2.1.0` artifact. Both must be on the compile classpath; Tomcat 10.1 provides both at runtime.

### Build verified
`mvn clean package -DskipTests` → **BUILD SUCCESS**
