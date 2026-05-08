// PORTED (zeqiang/database): CRUD for the RECIPES table.
// Returns RecipeRow (renamed from Zeqiang's Recipe to avoid collision with model.content.Recipe).
package edu.usc.csci201.group12.smartpantry.recipe;

import edu.usc.csci201.group12.smartpantry.dao.RecipeRow;
import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeDao {

    public boolean createRecipe(String authorId, String title, String description,
                                String imageUrl, String difficulty,
                                int prepTime, int cookTime, int servings,
                                boolean isPublic, String categoryTags) {
        String sql = """
                INSERT INTO RECIPES (id, author_id, title, description, image_url, difficulty,
                    prep_time_min, cook_time_min, servings, is_public, category_tags, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, authorId);
            stmt.setString(3, title);
            stmt.setString(4, description);
            stmt.setString(5, imageUrl);
            stmt.setString(6, difficulty);
            stmt.setInt(7, prepTime);
            stmt.setInt(8, cookTime);
            stmt.setInt(9, servings);
            stmt.setBoolean(10, isPublic);
            stmt.setString(11, categoryTags);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public RecipeRow getById(String id) {
        String sql = "SELECT * FROM RECIPES WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RecipeRow> getByAuthor(String authorId) {
        List<RecipeRow> list = new ArrayList<>();
        String sql = "SELECT * FROM RECIPES WHERE author_id = ? ORDER BY created_at DESC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RecipeRow> getPublicRecipes() {
        List<RecipeRow> list = new ArrayList<>();
        String sql = "SELECT * FROM RECIPES WHERE is_public = 1 ORDER BY created_at DESC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RecipeRow> searchByIngredientName(String keyword) {
        List<RecipeRow> list = new ArrayList<>();
        String sql = """
                SELECT DISTINCT r.* FROM RECIPES r
                JOIN RECIPE_INGREDIENTS ri ON r.id = ri.recipe_id
                JOIN INGREDIENTS i ON ri.ingredient_id = i.id
                WHERE r.is_public = 1 AND LOWER(i.name) LIKE LOWER(?)
                ORDER BY r.created_at DESC
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteRecipe(String id) {
        String sql = "DELETE FROM RECIPES WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Inserts demo recipes on first run (INSERT IGNORE — safe to call repeatedly).
    public void seedIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM RECIPES";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement count = conn.prepareStatement(countSql);
             ResultSet rs = count.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try (Connection conn = JdbcConnectionFactory.openConnection()) {
            conn.setAutoCommit(false);
            try {
                // Demo author + likers (password_hash is a placeholder bcrypt for "demo1234")
                String userSql = "INSERT IGNORE INTO USERS (id,username,email,password_hash,is_active,is_guest) VALUES (?,?,?,?,1,0)";
                String[][] users = {
                    {"user-demo-001","SmartPantry","demo@smartpantry.app","$2a$10$demo.hash.placeholder.00001"},
                    {"user-liker-001","alice_eats","alice@example.com","$2a$10$demo.hash.placeholder.00002"},
                    {"user-liker-002","bob_cooks","bob@example.com","$2a$10$demo.hash.placeholder.00003"},
                    {"user-liker-003","carol_kitchen","carol@example.com","$2a$10$demo.hash.placeholder.00004"},
                    {"user-liker-004","dave_chef","dave@example.com","$2a$10$demo.hash.placeholder.00005"},
                    {"user-liker-005","eve_foodie","eve@example.com","$2a$10$demo.hash.placeholder.00006"},
                };
                try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                    for (String[] u : users) {
                        stmt.setString(1, u[0]); stmt.setString(2, u[1]);
                        stmt.setString(3, u[2]); stmt.setString(4, u[3]);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                // Recipes: id, author_id, title, description, prep, cook, servings, is_public, category_tags
                String recipeSql = """
                        INSERT IGNORE INTO RECIPES
                        (id,author_id,title,description,prep_time_min,cook_time_min,servings,is_public,category_tags,created_at)
                        VALUES (?,?,?,?,?,?,?,1,?,NOW())
                        """;
                Object[][] recipes = {
                    {"rec-001","user-demo-001","Spaghetti Carbonara","Creamy Roman pasta with eggs, cheese, and pancetta.",15,20,4,"Italian,Pasta"},
                    {"rec-002","user-demo-001","Chicken Tacos","Quick weeknight tacos with seasoned chicken and fresh toppings.",10,15,4,"Mexican,High Protein"},
                    {"rec-003","user-demo-001","Vegetable Stir Fry","Fast, colorful stir-fry with whatever vegetables you have.",10,15,3,"Asian,Vegetarian,Gluten Free"},
                    {"rec-004","user-demo-001","Classic Omelette","Fluffy 3-egg omelette with your choice of fillings.",5,5,1,"American,High Protein,Gluten Free"},
                    {"rec-005","user-demo-001","Chicken Curry","Warming coconut milk curry with tender chicken.",15,30,4,"Indian,High Protein,Gluten Free"},
                    {"rec-006","user-demo-001","Greek Salad","Fresh Mediterranean salad with cucumber, tomatoes, and feta.",10,0,2,"Mediterranean,Vegetarian,Gluten Free"},
                    {"rec-007","user-demo-001","Ground Beef Tacos","Crispy tacos with seasoned ground beef.",10,15,4,"Mexican"},
                    {"rec-008","user-demo-001","Garlic Butter Salmon","Pan-seared salmon with garlic butter and lemon.",5,15,2,"American,High Protein,Gluten Free"},
                    {"rec-009","user-demo-001","Red Lentil Soup","Hearty, spiced lentil soup — great for meal prep.",10,35,6,"Mediterranean,Vegetarian,Vegan"},
                    {"rec-010","user-demo-001","Shrimp Fried Rice","Better-than-takeout fried rice with shrimp and vegetables.",10,15,4,"Asian,High Protein"},
                };
                try (PreparedStatement stmt = conn.prepareStatement(recipeSql)) {
                    for (Object[] r : recipes) {
                        stmt.setString(1,(String)r[0]); stmt.setString(2,(String)r[1]);
                        stmt.setString(3,(String)r[2]); stmt.setString(4,(String)r[3]);
                        stmt.setInt(5,(Integer)r[4]);   stmt.setInt(6,(Integer)r[5]);
                        stmt.setInt(7,(Integer)r[6]);   stmt.setString(8,(String)r[7]);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                // Recipe ingredients: ri-id, recipe_id, ingredient_id, quantity, unit
                String riSql = "INSERT IGNORE INTO RECIPE_INGREDIENTS (id,recipe_id,ingredient_id,quantity,unit) VALUES (?,?,?,?,?)";
                Object[][] ri = {
                    // Carbonara
                    {"ri-001","rec-001","ing-039",8.0,"oz"},  // pasta
                    {"ri-002","rec-001","ing-028",4.0,"oz"},  // bacon (pancetta sub)
                    {"ri-003","rec-001","ing-030",3.0,"piece"}, // egg
                    {"ri-004","rec-001","ing-033",2.0,"oz"},  // cheese
                    {"ri-005","rec-001","ing-060",1.0,"tsp"}, // salt
                    {"ri-006","rec-001","ing-061",1.0,"tsp"}, // pepper
                    // Chicken Tacos
                    {"ri-007","rec-002","ing-021",1.0,"lb"},  // chicken
                    {"ri-008","rec-002","ing-043",8.0,"piece"}, // tortilla
                    {"ri-009","rec-002","ing-062",1.0,"tsp"}, // cumin
                    {"ri-010","rec-002","ing-064",1.0,"tsp"}, // chili powder
                    {"ri-011","rec-002","ing-060",0.5,"tsp"}, // salt
                    {"ri-012","rec-002","ing-001",1.0,"cup"}, // tomatoes
                    // Veggie Stir Fry
                    {"ri-013","rec-003","ing-010",2.0,"cup"}, // broccoli
                    {"ri-014","rec-003","ing-009",1.0,"piece"}, // bell pepper
                    {"ri-015","rec-003","ing-013",1.0,"cup"}, // mushrooms
                    {"ri-016","rec-003","ing-052",3.0,"tbsp"}, // soy sauce
                    {"ri-017","rec-003","ing-048",2.0,"tbsp"}, // vegetable oil
                    {"ri-018","rec-003","ing-002",2.0,"clove"}, // garlic
                    // Omelette
                    {"ri-019","rec-004","ing-030",3.0,"piece"}, // egg
                    {"ri-020","rec-004","ing-032",1.0,"tbsp"}, // butter
                    {"ri-021","rec-004","ing-033",1.0,"oz"},  // cheese
                    {"ri-022","rec-004","ing-060",0.25,"tsp"}, // salt
                    {"ri-023","rec-004","ing-061",0.25,"tsp"}, // pepper
                    // Chicken Curry
                    {"ri-024","rec-005","ing-021",1.5,"lb"},  // chicken
                    {"ri-025","rec-005","ing-051",1.0,"cup"}, // coconut milk
                    {"ri-026","rec-005","ing-001",2.0,"cup"}, // tomatoes
                    {"ri-027","rec-005","ing-003",1.0,"piece"}, // onion
                    {"ri-028","rec-005","ing-002",3.0,"clove"}, // garlic
                    {"ri-029","rec-005","ing-071",1.0,"tsp"}, // ginger
                    {"ri-030","rec-005","ing-070",1.0,"tsp"}, // turmeric
                    {"ri-031","rec-005","ing-062",2.0,"tsp"}, // cumin
                    // Greek Salad
                    {"ri-032","rec-006","ing-008",1.0,"piece"}, // cucumber
                    {"ri-033","rec-006","ing-001",2.0,"cup"}, // tomatoes
                    {"ri-034","rec-006","ing-033",3.0,"oz"},  // cheese (feta)
                    {"ri-035","rec-006","ing-047",3.0,"tbsp"}, // olive oil
                    {"ri-036","rec-006","ing-018",1.0,"piece"}, // lemon
                    // Beef Tacos
                    {"ri-037","rec-007","ing-022",1.0,"lb"},  // beef
                    {"ri-038","rec-007","ing-043",8.0,"piece"}, // tortilla
                    {"ri-039","rec-007","ing-062",1.0,"tsp"}, // cumin
                    {"ri-040","rec-007","ing-064",1.0,"tsp"}, // chili powder
                    {"ri-041","rec-007","ing-001",1.0,"cup"}, // tomatoes
                    {"ri-042","rec-007","ing-003",1.0,"piece"}, // onion
                    // Garlic Butter Salmon
                    {"ri-043","rec-008","ing-025",2.0,"oz"},  // salmon (2 fillets → oz)
                    {"ri-044","rec-008","ing-032",3.0,"tbsp"}, // butter
                    {"ri-045","rec-008","ing-002",3.0,"clove"}, // garlic
                    {"ri-046","rec-008","ing-018",1.0,"piece"}, // lemon
                    {"ri-047","rec-008","ing-060",0.5,"tsp"}, // salt
                    // Red Lentil Soup
                    {"ri-048","rec-009","ing-044",2.0,"cup"}, // lentils
                    {"ri-049","rec-009","ing-050",6.0,"cup"}, // vegetable broth
                    {"ri-050","rec-009","ing-001",1.0,"cup"}, // tomatoes
                    {"ri-051","rec-009","ing-003",1.0,"piece"}, // onion
                    {"ri-052","rec-009","ing-070",1.0,"tsp"}, // turmeric
                    {"ri-053","rec-009","ing-062",1.0,"tsp"}, // cumin
                    // Shrimp Fried Rice
                    {"ri-054","rec-010","ing-024",0.5,"lb"},  // shrimp
                    {"ri-055","rec-010","ing-038",2.0,"cup"}, // rice
                    {"ri-056","rec-010","ing-030",2.0,"piece"}, // egg
                    {"ri-057","rec-010","ing-052",3.0,"tbsp"}, // soy sauce
                    {"ri-058","rec-010","ing-009",1.0,"piece"}, // bell pepper
                    {"ri-059","rec-010","ing-002",2.0,"clove"}, // garlic
                };
                try (PreparedStatement stmt = conn.prepareStatement(riSql)) {
                    for (Object[] r : ri) {
                        stmt.setString(1,(String)r[0]); stmt.setString(2,(String)r[1]);
                        stmt.setString(3,(String)r[2]); stmt.setDouble(4,(Double)r[3]);
                        stmt.setString(5,(String)r[4]);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                // Recipe steps: rs-id, recipe_id, step_number, instruction, timer_seconds
                String rsSql = "INSERT IGNORE INTO RECIPE_STEPS (id,recipe_id,step_number,instruction,timer_seconds) VALUES (?,?,?,?,?)";
                Object[][] steps = {
                    // Carbonara
                    {"rs-001","rec-001",1,"Cook pasta in salted boiling water until al dente. Reserve 1 cup pasta water before draining.",600},
                    {"rs-002","rec-001",2,"Fry bacon/pancetta in a large pan over medium heat until crispy. Remove from heat.",300},
                    {"rs-003","rec-001",3,"Whisk eggs and grated cheese together. Add hot pasta to the pan, pour egg mixture over and toss quickly, adding pasta water as needed to create a creamy sauce.",120},
                    // Chicken Tacos
                    {"rs-004","rec-002",1,"Season chicken with cumin, chili powder, salt, and pepper.",0},
                    {"rs-005","rec-002",2,"Cook chicken in a skillet over medium-high heat, 6-7 minutes per side, until cooked through. Slice or shred.",780},
                    {"rs-006","rec-002",3,"Warm tortillas and fill with chicken and fresh toppings.",60},
                    // Veggie Stir Fry
                    {"rs-007","rec-003",1,"Heat oil in a wok or large skillet over high heat.",60},
                    {"rs-008","rec-003",2,"Add garlic and cook 30 seconds, then add harder vegetables (broccoli, carrots). Stir fry 3 minutes.",210},
                    {"rs-009","rec-003",3,"Add remaining vegetables and soy sauce. Toss and cook until tender-crisp, about 2 minutes more.",120},
                    // Omelette
                    {"rs-010","rec-004",1,"Whisk eggs with salt and pepper in a bowl.",0},
                    {"rs-011","rec-004",2,"Melt butter in a non-stick pan over medium heat. Pour in eggs and gently push edges toward center as they set.",120},
                    {"rs-012","rec-004",3,"Add fillings to one half when eggs are just set. Fold omelette in half and slide onto plate.",60},
                    // Chicken Curry
                    {"rs-013","rec-005",1,"Sauté onion in oil until softened, about 5 minutes. Add garlic and ginger, cook 1 minute.",360},
                    {"rs-014","rec-005",2,"Add spices and stir for 30 seconds. Add chicken and brown on all sides.",300},
                    {"rs-015","rec-005",3,"Pour in tomatoes and coconut milk. Simmer 20 minutes until chicken is cooked and sauce thickens.",1200},
                    // Greek Salad
                    {"rs-016","rec-006",1,"Chop cucumber and tomatoes into chunks. Slice onion.",0},
                    {"rs-017","rec-006",2,"Combine vegetables in a bowl. Crumble feta cheese over top.",0},
                    {"rs-018","rec-006",3,"Drizzle with olive oil and lemon juice. Season with salt and pepper.",0},
                    // Beef Tacos
                    {"rs-019","rec-007",1,"Brown ground beef in a skillet over medium-high heat, breaking it up as it cooks.",480},
                    {"rs-020","rec-007",2,"Drain excess fat. Add cumin, chili powder, and salt. Stir to combine.",60},
                    {"rs-021","rec-007",3,"Warm tortillas and fill with beef and toppings.",60},
                    // Garlic Butter Salmon
                    {"rs-022","rec-008",1,"Pat salmon dry and season with salt and pepper.",0},
                    {"rs-023","rec-008",2,"Melt butter in an oven-safe skillet over medium-high heat. Sear salmon skin-side up for 4 minutes.",240},
                    {"rs-024","rec-008",3,"Flip salmon, add garlic to the pan, and cook 3-4 minutes until salmon flakes easily. Squeeze lemon over top.",240},
                    // Red Lentil Soup
                    {"rs-025","rec-009",1,"Sauté onion in oil 5 minutes. Add garlic, turmeric, and cumin; cook 1 minute.",360},
                    {"rs-026","rec-009",2,"Add lentils, tomatoes, and broth. Bring to a boil then reduce heat.",300},
                    {"rs-027","rec-009",3,"Simmer 25 minutes until lentils are completely soft. Blend partially if desired for creamier texture.",1500},
                    // Shrimp Fried Rice
                    {"rs-028","rec-010",1,"Use day-old cooked rice if possible. Cook shrimp in oil until pink, about 2 minutes per side. Set aside.",240},
                    {"rs-029","rec-010",2,"In the same pan, scramble eggs briefly, then add garlic, bell pepper, and rice. Stir fry 3 minutes.",300},
                    {"rs-030","rec-010",3,"Return shrimp to pan. Add soy sauce and toss everything together over high heat for 1 minute.",60},
                };
                try (PreparedStatement stmt = conn.prepareStatement(rsSql)) {
                    for (Object[] s : steps) {
                        stmt.setString(1,(String)s[0]); stmt.setString(2,(String)s[1]);
                        stmt.setInt(3,(Integer)s[2]);   stmt.setString(4,(String)s[3]);
                        stmt.setInt(5,(Integer)s[4]);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                // Likes: varying counts to populate trending sections
                String likeSql = "INSERT IGNORE INTO RECIPE_LIKES (recipe_id,user_id,is_like) VALUES (?,?,1)";
                String[][] likes = {
                    {"rec-005","user-liker-001"},{"rec-005","user-liker-002"},
                    {"rec-005","user-liker-003"},{"rec-005","user-liker-004"},{"rec-005","user-liker-005"},
                    {"rec-001","user-liker-001"},{"rec-001","user-liker-002"},{"rec-001","user-liker-003"},{"rec-001","user-liker-004"},
                    {"rec-002","user-liker-001"},{"rec-002","user-liker-002"},{"rec-002","user-liker-003"},
                    {"rec-010","user-liker-001"},{"rec-010","user-liker-002"},{"rec-010","user-liker-003"},
                    {"rec-008","user-liker-001"},{"rec-008","user-liker-002"},
                    {"rec-003","user-liker-001"},{"rec-003","user-liker-002"},
                    {"rec-007","user-liker-001"},{"rec-007","user-liker-002"},
                    {"rec-004","user-liker-001"},
                    {"rec-009","user-liker-001"},
                    {"rec-006","user-liker-001"},
                };
                try (PreparedStatement stmt = conn.prepareStatement(likeSql)) {
                    for (String[] l : likes) {
                        stmt.setString(1, l[0]); stmt.setString(2, l[1]);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Idempotent: sets image_url for the 10 demo recipes where it is still NULL.
    public void backfillImageUrls() {
        String[][] images = {
            {"rec-001", "https://images.unsplash.com/photo-1612874742237-6526221588e3?w=600&q=80"},
            {"rec-002", "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=600&q=80"},
            {"rec-003", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=600&q=80"},
            {"rec-004", "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=600&q=80"},
            {"rec-005", "https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=600&q=80"},
            {"rec-006", "https://images.unsplash.com/photo-1540189549336-e6e99eb49040?w=600&q=80"},
            {"rec-007", "https://images.unsplash.com/photo-1551504734-5da073f5b756?w=600&q=80"},
            {"rec-008", "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=600&q=80"},
            {"rec-009", "https://images.unsplash.com/photo-1547592180-85f173990554?w=600&q=80"},
            {"rec-010", "https://images.unsplash.com/photo-1512058533999-1429d3a3ec42?w=600&q=80"},
        };
        String sql = "UPDATE RECIPES SET image_url = ? WHERE id = ? AND image_url IS NULL";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String[] row : images) {
                stmt.setString(1, row[1]);
                stmt.setString(2, row[0]);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private RecipeRow mapRow(ResultSet rs) throws SQLException {
        RecipeRow r = new RecipeRow();
        r.setId(rs.getString("id"));
        r.setAuthorId(rs.getString("author_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setImageUrl(rs.getString("image_url"));
        r.setDifficulty(rs.getString("difficulty"));
        r.setPrepTimeMin(rs.getInt("prep_time_min"));
        r.setCookTimeMin(rs.getInt("cook_time_min"));
        r.setServings(rs.getInt("servings"));
        r.setPublic(rs.getBoolean("is_public"));
        r.setCategoryTags(rs.getString("category_tags"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }
}
