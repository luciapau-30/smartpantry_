# Lucia's Implementation Plan — Algo & Data Processing

## My Responsibilities
- Synonym Matching
- Quantity Normalization
- Recommendation Engine (scoring)
- Scoring Formula: `RecScore = w1*pantryScore + w2*prefScore + w3*popularityScore`

---

## Current Status Summary

| Component | File | Status |
|---|---|---|
| Synonym Matching | `IngredientSynonymResolver.java` | ✅ Done |
| Quantity Normalization | `UnitNormalizer.java` | ✅ Done |
| Pantry Matching | `PantryMatcher.java` | ✅ Done |
| Pantry Score | `RecipeScorer.pantryScore()` | ✅ Done |
| Scoring Formula / Weights | `RecipeScorer.total()`, `ScoreWeights.java` | ✅ Done |
| Popularity Score (formula) | `RecipeScorer.popularityScore()` | ✅ Done |
| Preference Score | `RecipeScorer.prefScore()` | ❌ Stub (returns 0.5) |
| Comment Count Fetch | `RecipeRecommender.getCommentCount()` | ❌ Returns 0 |
| API Endpoint | `RecommendRecipesServlet.java` | ✅ Done |
| Bootstrap Wiring | `SmartPantryBootstrapListener.java` | ✅ Done |

---

## What Still Needs to Be Done

### 1. Preference Score — `RecipeScorer.prefScore()`
**File**: `recommendation/RecipeScorer.java`

Currently a stub returning 0.5 (neutral). Needs real logic.

**Plan:**
- Check if the `User` / `Member` model (from Archit/Chenyang's branch) has a preferred cuisines field
- If it does: compare `recipe.getCuisineType()` against user's preferred cuisines list
  - Full match → 1.0
  - No preferences stored → 0.5 (neutral, keep as is)
  - Cuisine present but not preferred → 0.2
- If the User model doesn't support preferences yet: leave at 0.5 and document it

**Depends on**: Archit/Chenyang's `Member.java` model

---

### 2. Comment Count Integration — `RecipeRecommender.getCommentCount()`
**File**: `recommendation/RecipeRecommender.java`

Currently returns 0 for all recipes, making `popularityScore` always 0.5.

**Plan:**
- Check if a `CommentRepository` or `Comment` DAO exists on teammates' branches
- If yes: inject it into `RecipeRecommender` and call it in `getCommentCount(recipeId)`
- If no: query the `COMMENTS` table directly via JDBC (same pattern as `RecommendRecipesServlet.fetchPantry()`)
  ```sql
  SELECT COUNT(*) FROM COMMENTS WHERE recipe_id = ?
  ```
- Wire the repository/connection into `SmartPantryBootstrapListener`

**Depends on**: DB schema for `COMMENTS` table and/or teammates' Comment DAO

---

### 3. Integration With Teammates' Models
Before finalizing 1 and 2, I need to pull from their branches and check:

- [ ] Pull `archit-chenyang/integration` and inspect `Member.java` for preference fields
- [ ] Check if `Comment.java` / `CommentRepository` exists anywhere
- [ ] Check the DB schema for `COMMENTS` table structure

```bash
git fetch origin
git log origin/archit-chenyang/integration --oneline
```

---

### 4. Testing
Once preference score and comment count are wired up:

- [ ] Unit test `IngredientSynonymResolver.matches()` — edge cases like "Roma tomatoes" → "tomatoes"
- [ ] Unit test `UnitNormalizer.toBase()` — cups to ml, oz to g, etc.
- [ ] Unit test `RecipeScorer` — verify weighted formula output
- [ ] Integration test: call `GET /api/member/recipes/recommend` with a seeded pantry, verify sorted results

---

## Data Flow (How It All Connects)

```
Member logs in
    → RecommendRecipesServlet fetches their PANTRY_ITEMS from DB
    → Builds List<PantryItem>
    → Calls RecipeRecommender.recommend(member, pantry)
        → Fetches all public recipes from RecipeRepository
        → For each recipe:
            PantryMatcher.check() per ingredient
                → IngredientSynonymResolver.matches() (synonym check)
                → UnitNormalizer.toBase() (unit normalization)
            RecipeScorer.pantryScore()   → fraction of ingredients covered
            RecipeScorer.prefScore()     → cuisine preference match    ← TODO
            RecipeScorer.popularityScore() → comment count normalized  ← needs real count
            RecipeScorer.total()         → w1*pantry + w2*pref + w3*pop
        → Sort by totalScore DESC
    → Return JSON list of ScoredRecipe
```

---

## Scoring Formula Reference

```
RecScore = w1 * pantryScore + w2 * prefScore + w3 * popularityScore
```

Default weights (in `ScoreWeights.defaults()`):
- `w1 = 0.60` — pantry coverage (dominant)
- `w2 = 0.25` — preference match
- `w3 = 0.15` — popularity

Other presets available: `ScoreWeights.equal()`, `ScoreWeights.pantryOnly()`

---

## Key Files

| File | Package |
|---|---|
| [IngredientSynonymResolver.java](backend/src/main/java/edu/usc/csci201/group12/smartpantry/recommendation/IngredientSynonymResolver.java) | `recommendation` |
| [UnitNormalizer.java](backend/src/main/java/edu/usc/csci201/group12/smartpantry/recommendation/UnitNormalizer.java) | `recommendation` |
| [PantryMatcher.java](backend/src/main/java/edu/usc/csci201/group12/smartpantry/recommendation/PantryMatcher.java) | `recommendation` |
| [RecipeScorer.java](backend/src/main/java/edu/usc/csci201/group12/smartpantry/recommendation/RecipeScorer.java) | `recommendation` |
| [RecipeRecommender.java](backend/src/main/java/edu/usc/csci201/group12/smartpantry/recommendation/RecipeRecommender.java) | `recommendation` |
| [ScoreWeights.java](backend/src/main/java/edu/usc/csci201/group12/smartpantry/recommendation/ScoreWeights.java) | `recommendation` |
| [ScoredRecipe.java](backend/src/main/java/edu/usc/csci201/group12/smartpantry/recommendation/ScoredRecipe.java) | `recommendation` |
| [RecommendRecipesServlet.java](backend/src/main/java/edu/usc/csci201/group12/smartpantry/servlet/member/RecommendRecipesServlet.java) | `servlet/member` |
| [synonyms.json](backend/src/main/resources/synonyms.json) | `resources` |
