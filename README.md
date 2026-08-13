# SmartPantry & Meal Planner
**CSCI 201 — Group 12 | Spring 2026**

A community-driven pantry and recipe management web app. Track what's in your kitchen, discover recipes that match your ingredients, and share your own creations with the community.

---

## Features

- **Pantry tracking** — Add items with name, quantity, unit, and expiration date. Import via CSV or text paste.
- **Explore page** — Browse, search, and filter community recipes by dietary tags, cuisine, or prep time.
- **Recipe upload** — Share recipes with ingredients, step-by-step instructions, and cuisine tags.
- **Recommendation engine** — Ranked suggestions based on pantry coverage, cuisine preferences, and community popularity.
- **Make Recipe** — Consuming a recipe automatically removes matching pantry items (earliest-expiring first).
- **Shopping list** — Auto-generated from saved recipes vs. current pantry.
- **Expiration alerts** — Background job detects expiring items and broadcasts live alerts via WebSocket.
- **Guest demo** — Enter ingredients you have and get recipe suggestions without creating an account.
- **Dietary preferences** — Set preferred cuisines to boost relevant recommendations.
- **Ingredient synonym matching** — "Roma tomatoes" and "garbanzo beans" resolve to canonical ingredients.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Jakarta Servlet 6.0, Apache Tomcat 10.1 |
| Database | MySQL 8+ via JDBC |
| Real-time | WebSockets (`/ws/pantry`) |
| Background jobs | Java thread pool — expiry check every 10 min |
| Auth | BCrypt password hashing, session-based |
| Frontend | HTML/CSS/JS, Tailwind CSS CDN |
| Fonts | Cormorant Garamond + Jost (Google Fonts) |
| Build | Maven WAR (`finalName=smartpantry`) |

---

## Demo available here : https://youtu.be/vEIm5NRgVd0 

---

## Project Structure

```
csci201-spring26-group12/
├── FrontendCode/
│   ├── index.html        # Explore / community page
│   ├── pantry.html       # My Pantry
│   ├── recipes.html      # My Recipes + preferences
│   ├── add-item.html     # Redirect → pantry.html
│   ├── auth.js           # Login / register / session logic
│   └── style.css         # Warm editorial design system
│
├── backend/
│   └── src/main/
│       ├── java/.../smartpantry/
│       │   ├── dao/              # JDBC data access (users, pantry, recipes, etc.)
│       │   ├── model/            # User, Member, Guest, Recipe, Comment
│       │   ├── recommendation/   # RecipeRecommender, RecipeScorer, PantryMatcher
│       │   ├── servlet/          # API endpoint servlets
│       │   │   └── member/       # Member-only endpoints
│       │   ├── web/              # Bootstrap listener, ContextKeys
│       │   └── websocket/        # PantryWebSocket, PantryEventBroadcaster
│       └── resources/
│           ├── schema.sql        # Full DB setup script
│           └── synonyms.json     # Ingredient synonym mappings (64 entries)
│
├── IMPLEMENTATION.md     # Feature-by-feature status and technical notes
└── README.md
```

---

## Setup & Running

### Prerequisites
- Java 17+
- Apache Tomcat 10.1
- MySQL 8+
- Maven 3.8+

### 1. Set up the database

Open MySQL Workbench, connect to your local server, then run:

```sql
-- paste contents of backend/src/main/resources/schema.sql
```

This creates the `smartpantry` database and all 11 tables. The app seeds the `INGREDIENTS` table automatically on first startup.

### 2. Configure the database connection

Set the environment variable before starting Tomcat:

```bash
export PANTRY_DB_URL="jdbc:mysql://localhost:3306/smartpantry?user=root&password=YOUR_PASSWORD"
```

### 3. Build the WAR

```bash
cd backend
mvn clean package
```

The WAR is produced at `backend/target/smartpantry.war`.

### 4. Deploy to Tomcat

In Eclipse:
1. Add the project to a **Tomcat 10.1** server via **Servers → Add and Remove**
2. Right-click the server → **Start**

Or copy `smartpantry.war` into `$TOMCAT_HOME/webapps/` and start Tomcat manually.

### 5. Open the frontend

Open any page directly in a browser (no build step needed):

```
FrontendCode/index.html    → Explore Recipes
FrontendCode/pantry.html   → My Pantry
FrontendCode/recipes.html  → My Recipes
```

The frontend calls `http://localhost:8080/smartpantry/api/...` — make sure Tomcat is running.

---

## API Endpoints

### Public
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/recipes` | Browse all public recipes |
| `GET` | `/api/recipes/trending` | Most active recipes |
| `GET` | `/api/recipes/top` | Highest-rated recipes |
| `GET` | `/api/recipes/{id}` | Recipe detail + comments |
| `GET` | `/api/ingredients` | Full ingredient catalog |
| `POST` | `/api/auth/register` | Create account |
| `POST` | `/api/auth/login` | Log in |
| `POST` | `/api/auth/logout` | Log out |
| `GET` | `/api/auth/me` | Current session user |
| `POST` | `/api/guest/demo` | Guest recipe suggestions |

### Member-only (requires login)
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/member/pantry` | List pantry items |
| `POST` | `/api/member/pantry/add` | Add pantry item |
| `DELETE` | `/api/member/pantry/{id}` | Remove pantry item |
| `GET` | `/api/member/recipes/mine` | My uploaded recipes |
| `POST` | `/api/member/recipes/upload` | Upload a recipe |
| `POST` | `/api/member/recipes/{id}/like` | Like a recipe |
| `POST` | `/api/member/recipes/{id}/dislike` | Dislike a recipe |
| `POST` | `/api/member/recipes/{id}/save` | Save a recipe |
| `POST` | `/api/member/recipes/{id}/make` | Make recipe, consume pantry |
| `GET` | `/api/member/recipes/saved` | Saved recipes |
| `GET` | `/api/member/recipes/recommend` | Personalized recommendations |
| `POST` | `/api/member/comments` | Post a comment |
| `GET/POST` | `/api/member/preferences` | Cuisine preferences |
| `GET` | `/api/member/shopping-list` | Shopping list |

### WebSocket
| Path | Description |
|---|---|
| `/ws/pantry` | Live pantry updates and expiry alerts |

---

USC CSCI 201 — Principles of Software Development | Spring 2026
