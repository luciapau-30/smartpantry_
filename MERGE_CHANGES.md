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

## Up Next — Step 3: Port `zeqiang/database` DAOs into Maven project

Zeqiang's work lives in a standalone Eclipse project with flat packages (`dao.*`, `model.*`).
It needs to be manually copied into the Maven project and package declarations updated.

Files to port:
- `UserDao`, `RecipeDao`, `PantryItemDao`, `IngredientDao`, `RecipeIngredientDao`
- `RecipeStepDao`, `RecipeLikeDao`, `RecipeSaveDao`, `CommentDao`, `UserAllergyDao`
- `Database.java` → replace with `JdbcConnectionFactory` already in the project

After porting, `InMemoryUserStore` and `InMemoryRecipeRepository` will be replaced with
JDBC-backed implementations behind the existing `UserStore` and `RecipeRepository` interfaces.
