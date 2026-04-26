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
- **Status: NOT merged to `main` — integration needed**

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
**What's here:** Complete recommendation pipeline — the most algorithmically complex branch.
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
- **Status: NOT merged to `main` — needs integration with archit-chenyang backend**

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
- **Status: NOT integrated into Maven backend — standalone project only**

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
- [x] Explore/Community page with search, filter chips, guest/member distinction
- [x] My Pantry page (UI layout)
- [x] Add Item form with inline suggestions and validation
- [x] My Recipes page (UI layout)
- [x] Login button UI (wired to backend in `Jeff_integration`)
- [x] Tailwind CSS dark theme across all pages
- [x] Guest banner / restricted-action messaging

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
- [x] CORS filter
- [x] `JdbcConnectionFactory` — reads `PANTRY_DB_URL` env var

### Recommendation Engine
- [x] Ingredient synonym resolution with modifier stripping
- [x] Unit normalization (volume, weight, count families)
- [x] Pantry coverage scoring with optional-ingredient bonus
- [x] User preference scoring against category tags
- [x] Popularity scoring (comment count, normalized)
- [x] Tunable score weights
- [x] Full recommendation pipeline servlet

### Database
- [x] Full MySQL schema (all 10 tables)
- [x] All 10 DAOs with prepared statements
- [x] All 10 model POJOs
- [x] Full test suite (10 test classes)
- [x] `RecipeLikeDao` — upsert like/dislike with existing-record detection
- [x] `RecipeSaveDao` — save/unsave with lookup
- [x] `CommentDao` — threaded comments (parent_comment_id nullable FK)

---

## What Still Needs to Be Done

### Critical — Integration Work
| Task | Details |
|------|---------|
| **Merge backend branches to `main`** | `archit-chenyang/integration` is the base; merge `lucia/recommendation-engine` on top; then bring in `zeqiang/database` DAO layer to replace in-memory stores |
| **Replace `InMemoryUserStore` with JDBC** | `UserDao` from Zeqiang already exists — wire it into `SmartPantryBootstrapListener` behind the `UserStore` interface |
| **Replace `InMemoryRecipeRepository` with JDBC** | `RecipeDao` from Zeqiang already exists — needs a `RecipeRepository` adapter that calls `RecipeDao` methods |
| **Wire frontend fetch() calls to backend** | `index.html`, `pantry.html`, `recipes.html` make no API calls; all data is hardcoded. Each page needs JS fetch to the servlet endpoints |
| **Thread pool for Tomcat** | Configure `server.xml` Executor element or use `Executors.newFixedThreadPool` in `SmartPantryBootstrapListener` for internal background tasks |

### Backend — Missing Endpoints
| Endpoint | Purpose |
|----------|---------|
| `GET /api/member/pantry` | Retrieve logged-in user's pantry items |
| `DELETE /api/member/pantry/{id}` | Remove pantry item |
| `GET /api/recipes/{id}` | Full recipe detail (ingredients, steps, like counts) |
| `POST /api/member/recipes/{id}/like` | Like or dislike a recipe |
| `POST /api/member/recipes/{id}/save` | Save/unsave a recipe |
| `GET /api/recipes/{id}/comments` | Get threaded comments for a recipe |
| `GET /api/member/recipes/saved` | List user's saved recipes |
| `GET /api/member/recipes/mine` | List user's uploaded recipes |
| `GET /api/recipes/trending` | Trending recipes (activity window) |
| `GET /api/recipes/top` | Top recipes (like ratio) |
| `PUT /api/member/preferences` | Set dietary preferences + allergies |
| `POST /api/member/recipes/{id}/make` | "Make recipe" — deduct ingredients from pantry |
| `GET /api/member/shopping-list` | Generate shopping list from meal plan |

### CSCI 201 Requirements — Not Yet Implemented
| Requirement | What's Needed |
|-------------|--------------|
| **Thread pool (T2)** | Add `Executor` in `SmartPantryBootstrapListener`; Tomcat handles HTTP threads but internal background jobs need explicit pools |
| **Background thread for expiry checking (T3)** | `ScheduledExecutorService` in bootstrap: query `PANTRY_ITEMS WHERE expiration_date <= NOW() + 3 days`, fire notifications |
| **WebSockets / live updates (T4)** | Jakarta WebSocket endpoint; events: expiring-item alert, pantry sync, recipe suggestion refresh |
| **Expiration alerts (F14)** | Depends on background thread above; push message to connected WebSocket session |

### Frontend — Missing Functionality
| Page/Feature | What's Needed |
|-------------|--------------|
| Login / register modal | Form that calls `POST /api/auth/login` and `POST /api/auth/register`; store session cookie |
| Pantry page | Fetch from `GET /api/member/pantry`, render items with expiry badges, delete button |
| Add Item form | Wire submit to `POST /api/member/pantry/add` |
| Community page | Replace hardcoded recipes with `GET /api/recipes`; wire like/dislike/save buttons |
| Recipe detail modal/page | Fetch `GET /api/recipes/{id}`, show ingredients, steps, threaded comments |
| "In Your Pantry" section | Fetch from `GET /api/member/recipes/recommend`, surface `pantryScore` |
| My Recipes page | Fetch from `GET /api/member/recipes/mine`, link to upload form |
| Upload Recipe form | Wire to `POST /api/member/recipes/upload` |
| Shopping list page | New page; call `GET /api/member/shopping-list` |
| Guest demo | Allow unauthenticated user one browse + one recipe generation without saving |
| Real-time alerts | WebSocket listener — show toast on expiring item or pantry update |

### Algorithm / Data Quality
| Item | What's Needed |
|------|--------------|
| `synonyms.json` content | Currently minimal; needs to be populated with real ingredient synonyms |
| Trending score endpoint | Query DB: `SELECT recipe_id, COUNT(*) FROM RECIPE_LIKES WHERE created_at > NOW() - INTERVAL 24 HOUR GROUP BY recipe_id ORDER BY COUNT(*) DESC` |
| Ingredient catalog seeding | `INGREDIENTS` table needs seed data so `ingredient_id` lookups work on pantry add |
| Preference scoring | `prefScore` in `RecipeScorer` matches `recipe.getCategoryTags()` against `user.getDietaryPreferences()` — `User` model needs a `preferences` field populated from DB |

---

## Suggested Integration Order

1. **Merge** `archit-chenyang/integration` → `dev` (backend foundation)
2. **Port** Zeqiang's `zeqiang/database` DAOs into the Maven project under `backend/src/main/java/.../dao/`; replace `InMemoryUserStore` and `InMemoryRecipeRepository`
3. **Merge** `lucia/recommendation-engine` → `dev` (recommendation engine)
4. **Add** missing REST endpoints (pantry GET, likes, saves, comments GET, trending, top)
5. **Add** background expiry thread + WebSocket endpoint
6. **Wire** each frontend page to the real API (replace hardcoded data with fetch calls)
7. **Implement** login/register modal flow end-to-end
8. **Seed** `INGREDIENTS` table and populate `synonyms.json`
9. **Test** with ≥ 8 concurrent users; verify < 5s recommendation load time

---

## Team Ownership Reference

| Area | Branch | Owner |
|------|--------|-------|
| Frontend HTML/CSS | `main` | David, Alijah, Jeffrey |
| Auth + servlet foundation | `archit422/backend` → `archit-chenyang/integration` | Archit |
| Class hierarchy + member servlets | `archit-chenyang/integration` | Archit + Chenyang |
| Recommendation engine | `lucia/recommendation-engine` | Lucia |
| MySQL DAO layer + schema | `zeqiang/database` | Zeqiang |
| Frontend–backend wiring (index.html) | `Jeff_integration` | Jeffrey |
