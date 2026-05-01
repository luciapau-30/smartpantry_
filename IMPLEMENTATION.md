# SmartPantry & Meal Planner — Implementation Status
**CSCI 201 Group 12 | Spring 2026**

---

## Project Requirements Summary

### Core Features
| # | Requirement |
|---|-------------|
| F1 | User registration, login, logout — authenticated vs. guest experience |
| F2 | Persistent user accounts (pantry, recipes, preferences, history) |
| F3 | Pantry management — add items with name, quantity, expiration date |
| F4 | Recipe upload — title, description, photo, ingredients, steps, cuisine tags |
| F5 | Community / Explore page — browse, search, filter recipes |
| F6 | Like, dislike, save recipes; threaded comments |
| F7 | "Trending Now" section (activity in recent time window) |
| F8 | "Top Recipes" section (highest like ratio) |
| F9 | "In Your Pantry" section (recipes matchable from your pantry) |
| F10 | Recipe recommendation engine (pantry coverage + preferences + popularity) |
| F11 | "Make Recipe" action — removes matching pantry items |
| F12 | Shopping list generation |
| F13 | Weekly meal plan generation |
| F14 | Expiration-date alerts to users |
| F15 | Guest demo — one-time browse and recipe generation, no save |
| F16 | Ingredient synonym matching (e.g. "Roma tomatoes" → "tomatoes") |
| F17 | Unit normalization for quantity comparison (cups, oz, grams, etc.) |

### CSCI 201 Technical Requirements
| # | Requirement |
|---|-------------|
| T1 | Java Servlets on Apache Tomcat |
| T2 | Thread pool for handling concurrent client requests |
| T3 | Background thread — checks expiring items every X minutes, fires notifications/recommendations |
| T4 | WebSockets/Sockets — live updates: expiring alerts, pantry sync, recipe suggestion refresh |
| T5 | MySQL persistent storage |
| T6 | BCrypt password hashing |
| T7 | Prepared statements throughout (SQL injection prevention) |
| T8 | Client–server model over networking |
| T9 | Support ≥ 8 concurrent users without failure |
| T10 | Recipe suggestions load in < 5 seconds |

---

## Branch-by-Branch Status

### `main` / `dev`
**Owner:** Team (merged PRs from David, Alijah, Jeffrey)
**What's here:** Frontend only — 4 HTML pages with Tailwind CSS, dark theme, purple accent.
- `index.html` — Explore/Community page: search bar, dietary filter chips, guest banner, sections for Trending / Top / In Your Pantry (hardcoded demo data, no backend calls)
- `pantry.html` — My Pantry UI (static placeholder)
- `add-item.html` — Add Item form with inline ingredient-name suggestions and validation
- `recipes.html` — My Recipes page (static placeholder)

---

### `origin/archit422/backend`
**Owner:** Archit
**What's here:** Initial backend skeleton.
- Maven WAR project (`backend/pom.xml`), Java 17, Jakarta Servlet 6, Gson, BCrypt
- `User` / `Guest` class hierarchy with `UserPublicView`
- `PasswordHasher` (BCrypt via favre)
- `InMemoryUserStore`, `InMemoryRecipeRepository`
- Auth servlets: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`
- `AbstractJsonServlet`, `GsonProvider`, `JsonApiResponse`
- CORS filter, `SmartPantryBootstrapListener` (wires context attributes)
- **Status: Merged into `archit-chenyang/integration`**

---

### `origin/archit-chenyang/integration`
**Owner:** Archit + Chenyang
**What's here:** Full backend foundation (most complete backend branch).
- Everything from `archit422/backend`, plus:
- `Member extends User` — JDBC `addToPantry()`, `uploadRecipe()`, `postComment()` with prepared statements against the full DB schema
- `Post`, `Recipe`, `Comment` content models under `model.content`
- `JdbcConnectionFactory` — reads `PANTRY_DB_URL` env var or falls back to JNDI
- `RequestUsers` helper (resolves authenticated user from session)
- Member servlets:
  - `POST /api/member/pantry/add` — add pantry item (requires Member session)
  - `POST /api/member/recipes/upload` — upload recipe with ingredients + steps
  - `POST /api/member/comments` — post a comment (supports `parentCommentId` for threading)
- `GET /api/recipes` — `GuestRecipeServlet` — browse or search by ingredient (both guest and logged-in)
- `RegisterRequest` supports optional `accountType: "guest" | "member"`
- **Status: ✅ Merged into `dev` (2026-04-25)**

---

### `origin/yk-chenyang/backend`
**Owner:** Chenyang (early standalone version)
**What's here:** Class hierarchy as a standalone Java project (no Maven, no servlet container wiring).
- `model/` — `User`, `Guest`, `Member`, `Post`, `Recipe`, `Comment`
- `web/` — `AddToPantryServlet`, `PostCommentServlet`, `UploadRecipeServlet`
- **Status: Superseded by `archit-chenyang/integration`; safe to treat as reference only**

---

### `origin/lucia/recommendation-engine`
**Owner:** Lucia
**What's here:** Complete recommendation pipeline 
- `IngredientSynonymResolver` — loads `synonyms.json`, strips adjective modifiers (fresh/dried/chopped/etc.), maps variants to canonical names
- `UnitNormalizer` — converts volume → ml, weight → g, count → each for quantity comparison
- `PantryMatcher` — returns `CoverageResult(ingredientFound, quantityRatio)` for a recipe ingredient vs. a pantry list
- `RecipeScorer` — three sub-scores (each normalised [0,1]):
  - `pantryScore` — fraction of required ingredients covered, weighted by quantity; optional ingredients add up to 10% bonus
  - `prefScore` — matches recipe tags against user dietary preferences
  - `popularityScore` — normalized comment count
- `ScoreWeights` — tunable `w1, w2, w3` (defaults: 0.5, 0.3, 0.2)
- `RecipeRecommender` — orchestrates: fetch all public recipes → score → sort descending
- `ScoredRecipe` DTO — carries `totalScore`, `pantryScore`, `prefScore`, `popularityScore`, `missingIngredients`
- `GET /api/member/recipes/recommend` — fetches member's pantry from DB, runs recommender, returns ranked list
- `synonyms.json` resource file on classpath
- Updated `SmartPantryBootstrapListener` wires the full recommender pipeline at startup
- Also includes `JdbcConnectionFactory` (same as archit-chenyang version)
- **Status: ✅ Merged into `dev` on top of `archit-chenyang/integration` (2026-04-25)**

---

### `origin/zeqiang/database`
**Owner:** Zeqiang
**What's here:** Complete MySQL DAO layer — standalone Eclipse Java project.
- `201_database.sql` — MySQL schema dump
- `Database.java` — connection singleton reading `db.properties`
- `db.properties` — JDBC config
- `lib/mysql-connector-j-8.3.0.jar`
- All 10 DAOs (with CRUD + prepared statements):
  - `UserDao` — register, guest create, login lookup, update
  - `RecipeDao` — create, get by id/author, list public
  - `PantryItemDao` — add, get, list by user, delete
  - `IngredientDao` — create, get by name/id, list all
  - `RecipeIngredientDao` — add, list by recipe
  - `RecipeStepDao` — add, list by recipe ordered
  - `RecipeLikeDao` — upsert reaction (like/dislike), get counts
  - `RecipeSaveDao` — save/unsave, check saved, list by user
  - `CommentDao` — add comment/reply, get by recipe (threaded), soft delete
  - `UserAllergyDao` — add, list by user, delete
- All model POJOs: `User`, `Recipe`, `PantryItem`, `Ingredient`, `RecipeIngredient`, `RecipeStep`, `Comment`, `UserAllergy`
- Full test suite (10 test classes)
- **Status: ✅ Ported into Maven backend on `dev` (2026-04-25) — see MERGE_CHANGES.md for naming collision details**

---

### `origin/Jeff_integration`
**Owner:** Jeffrey
**What's here:** Backend from `archit-chenyang/integration` + updated `index.html`.
- `index.html` has a login button that attempts to call `POST /api/auth/login` and updates the session label
- **Status: Most up-to-date frontend + backend combination on a single branch; NOT merged to `main`**

---

### `origin/Jeffrey-Frontend` / `origin/Jeffrey-Fronted`
**Owner:** Jeffrey (early frontend iterations)
**What's here:** Earliest frontend work — login button, My Recipes tab stub, basic layout.
- **Status: Superseded by current `main` frontend; reference only**

---

## What Has Been Done

### Frontend
- [x] Explore/Community page — search, filter chips, guest/member distinction; trending/top/browse from real API
- [x] My Pantry page — full UI (items list, edit/delete, CSV import, text import); add/delete wired to backend
- [x] Add Item form — submit POSTs to `POST /api/member/pantry/add`
- [x] My Recipes page — fetches `GET /api/member/recipes/mine` on login
- [x] Login/register modal wired to backend via `auth.js` (all 4 pages)
- [x] Tailwind CSS dark theme across all pages
- [x] Guest banner / restricted-action messaging
- [x] Like/dislike/save buttons call real API endpoints
- [x] Comments submit to `POST /api/member/comments`
- [ ] Recipe detail panel — opens but uses local data; real ingredients/steps/comments not fetched
- [ ] Upload Recipe form — not wired to backend

### Backend (Java / Maven / Tomcat)
- [x] Maven WAR project, Java 17, Jakarta Servlet 6.0
- [x] `User` / `Guest` / `Member` class hierarchy
- [x] BCrypt password hashing
- [x] `POST /api/auth/register`
- [x] `POST /api/auth/login`
- [x] `POST /api/auth/logout`
- [x] `GET /api/auth/me`
- [x] `GET /api/recipes` — browse / search by ingredient (guest + member)
- [x] `POST /api/member/pantry/add`
- [x] `POST /api/member/recipes/upload` (recipe + ingredients + steps)
- [x] `POST /api/member/comments`
- [x] `GET /api/member/recipes/recommend` — ranked recommendation list
- [x] `GET /api/member/pantry` — read back logged-in member's pantry
- [x] `DELETE /api/member/pantry/{id}` — remove pantry item (ownership verified)
- [x] `GET /api/member/recipes/mine` — list member's own uploaded recipes
- [x] `GET /api/member/recipes/saved` — list member's saved recipes
- [x] `POST /api/member/recipes/{id}/like` — like/dislike/remove reaction
- [x] `POST /api/member/recipes/{id}/save` — save/unsave recipe
- [x] `GET /api/recipes/{id}` — full recipe detail (ingredients, steps, like counts, caller reaction)
- [x] `GET /api/recipes/{id}/comments` — threaded comments for a recipe
- [x] `GET /api/recipes/trending` — top 20 by likes in last 24 hours
- [x] `GET /api/recipes/top` — top 20 by all-time likes
- [x] CORS filter
- [x] `JdbcConnectionFactory` — reads `PANTRY_DB_URL` env var
- [x] `ExpiryCheckerThread` — daemon thread, checks PANTRY_ITEMS every 60 min, pushes WebSocket alerts
- [x] `AlertWebSocketEndpoint` — `/ws/alerts`; registers userId→Session on login, sends expiry JSON
- [x] `HttpSessionConfigurator` — passes HTTP session into WebSocket handshake for user identification
- [x] `auth.js` — shared auth across all pages; login/register/logout modals wired to backend; WebSocket client with auto-reconnect and toast notifications
- [x] `GET /api/ingredients` — `IngredientListServlet`; returns full ingredient catalog for frontend autocomplete

### Recommendation Engine
- [x] Ingredient synonym resolution with modifier stripping
- [x] Unit normalization (volume, weight, count families)
- [x] Pantry coverage scoring with optional-ingredient bonus
- [x] User preference scoring against category tags
- [x] Popularity scoring (comment count, normalized)
- [x] Tunable score weights
- [x] Full recommendation pipeline servlet

### Database & Integration (completed 2026-04-25)
- [x] Full MySQL schema (all 10 tables)
- [x] All 10 DAOs with prepared statements, ported into Maven under `dao/` and `recipe/`
- [x] `JdbcUserStore` — implements `UserStore`; maps DB rows → `Member` / `Guest`
- [x] `JdbcRecipeRepository` — implements `RecipeRepository`; builds full `Recipe` domain objects for the recommendation engine
- [x] `SmartPantryBootstrapListener` wires JDBC impls when `PANTRY_DB_URL` is set; falls back to in-memory for dev/demo (no DB required)
- [x] `RecipeLikeDao` — upsert like/dislike with existing-record detection
- [x] `RecipeSaveDao` — save/unsave with lookup
- [x] `CommentDao` — threaded comments (`parent_comment_id` nullable FK)
- [x] `UserAllergyDao` — allergy CRUD per user
- [x] `PantryItemDao` — includes `getExpiringSoon()` for background thread
- [x] MySQL connector dependency added to `pom.xml`
- [x] **`mvn clean package -DskipTests` → BUILD SUCCESS**

### Known Gap
- `Member.addToPantry()`, `uploadRecipe()`, `postComment()` call `JdbcConnectionFactory.openConnection()` directly — they require `PANTRY_DB_URL` to be set even in dev mode. These endpoints will crash without a DB connection.

---

## What Still Needs to Be Done

### Critical — Integration Work
| Task | Status | Details |
|------|--------|---------|
| **Merge backend branches into `dev`** | ✅ Done | All 3 branches merged; `dev` pushed to remote |
| **Replace `InMemoryUserStore` with JDBC** | ✅ Done | `JdbcUserStore` wired via `SmartPantryBootstrapListener` |
| **Replace `InMemoryRecipeRepository` with JDBC** | ✅ Done | `JdbcRecipeRepository` wired; builds full domain objects for recommender |
| **Add missing REST endpoints** | ✅ Done | pantry GET/DELETE, recipes mine/saved/detail/comments, like/save, trending, top |
| **Wire frontend fetch() calls to backend** | ✅ Done | `index.html`, `pantry.html`, `recipes.html` all fetch from real API endpoints |
| **Thread pool + background thread + WebSockets (T2/T3/T4/F14)** | ✅ Done | `BackgroundJobs` (4-thread `ScheduledExecutorService`) + `ExpiryCheckJob` (5-min period, 3-day window) + `PantryWebSocket` (`/ws/pantry`) + `PantryEventBroadcaster`; pantry add/delete servlets fire `PANTRY_UPDATED` events |

### Backend — Missing Endpoints
| Endpoint | Status | Purpose |
|----------|--------|---------|
| `GET /api/member/pantry` | ✅ Done | Retrieve logged-in user's pantry items |
| `DELETE /api/member/pantry/{id}` | ✅ Done | Remove pantry item |
| `GET /api/recipes/{id}` | ✅ Done | Full recipe detail (ingredients, steps, like counts) |
| `POST /api/member/recipes/{id}/like` | ✅ Done | Like or dislike a recipe |
| `POST /api/member/recipes/{id}/save` | ✅ Done | Save/unsave a recipe |
| `GET /api/recipes/{id}/comments` | ✅ Done | Get threaded comments for a recipe |
| `GET /api/member/recipes/saved` | ✅ Done | List user's saved recipes |
| `GET /api/member/recipes/mine` | ✅ Done | List user's uploaded recipes |
| `GET /api/recipes/trending` | ✅ Done | Trending recipes (last 24 hours) |
| `GET /api/recipes/top` | ✅ Done | Top recipes (all-time likes) |
| `GET /api/ingredients` | ✅ Done | List all seeded ingredients (id + name + category + defaultUnit) — used by Upload Recipe form |
| `PUT /api/member/preferences` | ❌ Not done | Set dietary preferences + allergies |
| `POST /api/member/recipes/{id}/make` | ❌ Not done | "Make recipe" — deduct ingredients from pantry |
| `GET /api/member/shopping-list` | ❌ Not done | Generate shopping list from meal plan |

### CSCI 201 Requirements — Status
| Requirement | Status | Where |
|-------------|--------|-------|
| **Thread pool (T2)** | ✅ Done | `background/BackgroundJobs.java` — 4-thread daemon `ScheduledExecutorService`, started in `SmartPantryBootstrapListener.contextInitialized` and stopped in `contextDestroyed` |
| **Background thread for expiry checking (T3)** | ✅ Done | `background/ExpiryCheckJob.java` scheduled every 5 min on the T2 pool; uses `PantryItemDao.getExpiringSoonWithName(userId, 3)` |
| **WebSockets / live updates (T4)** | ✅ Done | `websocket/PantryWebSocket.java` mapped at `/ws/pantry`; lifts userId from `HttpSession` via custom `Configurator`; tracks `Map<userId, Set<Session>>` for multi-tab fan-out |
| **Expiration alerts (F14)** | ✅ Done | `ExpiryCheckJob` calls `PantryEventBroadcaster.expiringItem(...)` which serialises `AlertPayload` (`type=EXPIRING_ITEM`) and pushes to every open socket for that user. Already-alerted items are deduped per-tick. |

### Frontend — Missing Functionality
| Page/Feature | What's Needed |
|-------------|--------------|
| Login / register modal | ✅ Done — `auth.js` shared across all pages; tabbed Sign In / Create Account modal wired to backend |
| Pantry page | ✅ Done — fetches `GET /api/member/pantry`, delete calls `DELETE /api/member/pantry/{id}` |
| Add Item form | ✅ Done — form POSTs to `POST /api/member/pantry/add`, reloads list |
| Community page | ✅ Done — trending/top/browse from API; like/dislike/save buttons call real endpoints |
| Recipe detail modal/page | ⚠️ Partial — panel opens but shows local normalized data; real ingredients/steps/comments not fetched from `GET /api/recipes/{id}` |
| "In Your Pantry" section | ✅ Done — fetches `GET /api/member/recipes/recommend` on login |
| My Recipes page | ✅ Done — fetches `GET /api/member/recipes/mine` on login |
| Upload Recipe form | ✅ Done — modal on `recipes.html`; ingredient autocomplete via `GET /api/ingredients`; POSTs to `POST /api/member/recipes/upload` |
| Shopping list page | New page; call `GET /api/member/shopping-list` |
| Guest demo | Allow unauthenticated user one browse + one recipe generation without saving |
| Real-time alerts | ✅ Done — WebSocket connects on login, toast shown on expiry alert push |

### Algorithm / Data Quality
| Item | What's Needed |
|------|--------------|
| `synonyms.json` content | Currently minimal; needs to be populated with real ingredient synonyms |
| Trending score endpoint | Query DB: `SELECT recipe_id, COUNT(*) FROM RECIPE_LIKES WHERE created_at > NOW() - INTERVAL 24 HOUR GROUP BY recipe_id ORDER BY COUNT(*) DESC` |
| Ingredient catalog seeding | `INGREDIENTS` table needs seed data so `ingredient_id` lookups work on pantry add |
| Preference scoring | `prefScore` in `RecipeScorer` matches `recipe.getCategoryTags()` against `user.getDietaryPreferences()` — `User` model needs a `preferences` field populated from DB |

---

## Suggested Integration Order

1. ✅ **Merge** `archit-chenyang/integration` → `dev` (backend foundation)
2. ✅ **Port** Zeqiang's `zeqiang/database` DAOs into the Maven project; wire `JdbcUserStore` and `JdbcRecipeRepository` behind existing interfaces
3. ✅ **Merge** `lucia/recommendation-engine` → `dev` (recommendation engine)
4. ✅ **Add** missing REST endpoints (pantry GET/DELETE, likes, saves, comments GET, trending, top, recipe detail)
5. ✅ **Add** background expiry thread + WebSocket endpoint
6. ⚠️ **Wire** each frontend page to the real API — data loading done; recipe detail panel still uses local object (no real ingredients/steps/comments from `GET /api/recipes/{id}`)
7. ✅ **Implement** login/register modal flow end-to-end (`auth.js` + tabbed modal on all 4 pages)
8. ❌ **Seed** `INGREDIENTS` table and populate `synonyms.json`
9. ❌ **Test** with ≥ 8 concurrent users; verify < 5s recommendation load time

---

## Team Ownership Reference

|         Area                          |                     Branch                          |               Owner           |
|        ------                         |--------                                             |-------                        |
| Frontend HTML/CSS                     | `main`                                              | David, Alijah, Jeffrey        |
| Auth + servlet foundation             | `archit422/backend` → `archit-chenyang/integration` | Archit                        |
| Class hierarchy + member servlets     | `archit-chenyang/integration`                       | Archit + Chenyang             |
| Recommendation engine                 | `lucia/recommendation-engine`                       | Lucia                         |
| MySQL DAO layer + schema              | `zeqiang/database`                                  | Zeqiang                       |
| Frontend–backend wiring (index.html)  | `Jeff_integration   `                               | Jeffrey                       |
