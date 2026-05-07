-- SmartPantry demo recipe seed — reference copy.
-- The app seeds this automatically via RecipeDao.seedIfEmpty() on first startup.
-- Safe to re-run manually: all inserts use INSERT IGNORE with fixed IDs.

USE smartpantry;

-- ─────────────────────────────────────────────────────────────
-- Demo author + community liker accounts
-- ─────────────────────────────────────────────────────────────
INSERT IGNORE INTO USERS (id, username, email, password_hash, created_at, is_active, is_guest) VALUES
  ('user-demo-001',  'SmartPantry',  'demo@smartpantry.app',    'DEMO_AUTHOR_NOT_FOR_LOGIN', NOW(), TRUE, FALSE),
  ('user-liker-001', 'alex_cooks',   'alex@example.com',        'DEMO_LIKER_NOT_FOR_LOGIN',  NOW(), TRUE, FALSE),
  ('user-liker-002', 'sofia_eats',   'sofia@example.com',       'DEMO_LIKER_NOT_FOR_LOGIN',  NOW(), TRUE, FALSE),
  ('user-liker-003', 'marcus_chef',  'marcus@example.com',      'DEMO_LIKER_NOT_FOR_LOGIN',  NOW(), TRUE, FALSE),
  ('user-liker-004', 'priya_food',   'priya@example.com',       'DEMO_LIKER_NOT_FOR_LOGIN',  NOW(), TRUE, FALSE),
  ('user-liker-005', 'jake_kitchen', 'jake@example.com',        'DEMO_LIKER_NOT_FOR_LOGIN',  NOW(), TRUE, FALSE);

-- ─────────────────────────────────────────────────────────────
-- Recipes
-- ─────────────────────────────────────────────────────────────
INSERT IGNORE INTO RECIPES (id, author_id, title, description, prep_time_min, cook_time_min, servings, is_public, category_tags, created_at) VALUES
  ('rec-001','user-demo-001','Spaghetti Carbonara',
   'A classic Italian pasta dish with a silky egg and cheese sauce, crispy bacon, and freshly cracked black pepper.',
   10, 20, 4, TRUE, 'Italian,Pasta', NOW()),

  ('rec-002','user-demo-001','Chicken Tacos',
   'Juicy spiced chicken in warm tortillas with fresh tomatoes, onion, and lime. A crowd-pleasing weeknight favorite.',
   15, 20, 4, TRUE, 'Mexican,High Protein', NOW()),

  ('rec-003','user-demo-001','Vegetable Stir Fry',
   'Crisp broccoli, bell pepper, and carrot tossed in garlic-ginger soy sauce and served over steamed rice.',
   10, 15, 4, TRUE, 'Asian,Vegetarian,Gluten Free', NOW()),

  ('rec-004','user-demo-001','Classic Omelette',
   'A perfectly folded omelette with melted cheese and your choice of vegetable filling. Ready in under 10 minutes.',
   5, 8, 1, TRUE, 'American,High Protein,Gluten Free', NOW()),

  ('rec-005','user-demo-001','Chicken Curry',
   'Tender chicken simmered in a fragrant coconut milk sauce with tomatoes, garlic, and warming spices.',
   15, 30, 4, TRUE, 'Indian,High Protein,Gluten Free', NOW()),

  ('rec-006','user-demo-001','Greek Salad',
   'Sun-ripened tomatoes, cucumber, red onion, and feta in a bright olive oil and lemon dressing.',
   10, 0, 4, TRUE, 'Mediterranean,Vegetarian,Gluten Free', NOW()),

  ('rec-007','user-demo-001','Ground Beef Tacos',
   'Seasoned ground beef with cumin and chili powder, loaded into tortillas with all your favorite toppings.',
   10, 15, 4, TRUE, 'Mexican', NOW()),

  ('rec-008','user-demo-001','Garlic Butter Salmon',
   'Pan-seared salmon fillets finished in a rich garlic butter sauce with fresh lemon and thyme.',
   5, 12, 4, TRUE, 'American,High Protein,Gluten Free', NOW()),

  ('rec-009','user-demo-001','Red Lentil Soup',
   'Hearty and warming lentil soup with carrots, cumin, and paprika — naturally vegan and endlessly comforting.',
   10, 30, 6, TRUE, 'Mediterranean,Vegetarian,Vegan', NOW()),

  ('rec-010','user-demo-001','Shrimp Fried Rice',
   'Restaurant-style fried rice with plump shrimp, scrambled egg, and a savory soy sauce glaze.',
   10, 15, 4, TRUE, 'Asian,High Protein', NOW());

-- ─────────────────────────────────────────────────────────────
-- Recipe ingredients  (references ing-001 … ing-071)
-- ─────────────────────────────────────────────────────────────

-- rec-001  Spaghetti Carbonara
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-001-1','rec-001','ing-039', 8,    'oz',    NULL,                    FALSE),
  ('ri-001-2','rec-001','ing-028', 4,    'oz',    NULL,                    FALSE),
  ('ri-001-3','rec-001','ing-030', 3,    'piece', NULL,                    FALSE),
  ('ri-001-4','rec-001','ing-033', 2,    'oz',    'Parmesan or Pecorino',  FALSE),
  ('ri-001-5','rec-001','ing-002', 2,    'clove', NULL,                    FALSE),
  ('ri-001-6','rec-001','ing-060', 1,    'tsp',   NULL,                    FALSE),
  ('ri-001-7','rec-001','ing-061', 1,    'tsp',   'freshly ground',        FALSE),
  ('ri-001-8','rec-001','ing-034', 0.25, 'cup',   NULL,                    TRUE);

-- rec-002  Chicken Tacos
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-002-1','rec-002','ing-021', 1,    'lb',    'boneless skinless',     FALSE),
  ('ri-002-2','rec-002','ing-043', 8,    'piece', 'small flour tortillas', FALSE),
  ('ri-002-3','rec-002','ing-001', 1,    'cup',   'diced',                 FALSE),
  ('ri-002-4','rec-002','ing-003', 0.5,  'piece', 'diced',                 FALSE),
  ('ri-002-5','rec-002','ing-019', 2,    'piece', NULL,                    FALSE),
  ('ri-002-6','rec-002','ing-062', 1,    'tsp',   NULL,                    FALSE),
  ('ri-002-7','rec-002','ing-064', 1,    'tsp',   NULL,                    FALSE),
  ('ri-002-8','rec-002','ing-060', 1,    'tsp',   NULL,                    FALSE),
  ('ri-002-9','rec-002','ing-007', 1,    'cup',   'shredded',              TRUE);

-- rec-003  Vegetable Stir Fry
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-003-1','rec-003','ing-038', 2,    'cup',   'cooked',                FALSE),
  ('ri-003-2','rec-003','ing-010', 2,    'cup',   'florets',               FALSE),
  ('ri-003-3','rec-003','ing-009', 2,    'piece', 'sliced',                FALSE),
  ('ri-003-4','rec-003','ing-004', 2,    'piece', 'julienned',             FALSE),
  ('ri-003-5','rec-003','ing-052', 3,    'tbsp',  NULL,                    FALSE),
  ('ri-003-6','rec-003','ing-002', 3,    'clove', NULL,                    FALSE),
  ('ri-003-7','rec-003','ing-071', 1,    'tsp',   NULL,                    FALSE),
  ('ri-003-8','rec-003','ing-048', 2,    'tbsp',  NULL,                    FALSE);

-- rec-004  Classic Omelette
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-004-1','rec-004','ing-030', 3,    'piece', NULL,                    FALSE),
  ('ri-004-2','rec-004','ing-032', 1,    'tbsp',  NULL,                    FALSE),
  ('ri-004-3','rec-004','ing-033', 1,    'oz',    'shredded',              FALSE),
  ('ri-004-4','rec-004','ing-060', 0.5,  'tsp',   NULL,                    FALSE),
  ('ri-004-5','rec-004','ing-061', 0.25, 'tsp',   NULL,                    FALSE),
  ('ri-004-6','rec-004','ing-003', 0.25, 'piece', 'diced',                 TRUE),
  ('ri-004-7','rec-004','ing-009', 0.5,  'piece', 'diced',                 TRUE);

-- rec-005  Chicken Curry
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-005-1','rec-005','ing-021', 1.5,  'lb',    'cubed',                 FALSE),
  ('ri-005-2','rec-005','ing-003', 1,    'piece', 'diced',                 FALSE),
  ('ri-005-3','rec-005','ing-001', 2,    'cup',   'chopped',               FALSE),
  ('ri-005-4','rec-005','ing-002', 4,    'clove', NULL,                    FALSE),
  ('ri-005-5','rec-005','ing-071', 1,    'tsp',   NULL,                    FALSE),
  ('ri-005-6','rec-005','ing-051', 1,    'cup',   NULL,                    FALSE),
  ('ri-005-7','rec-005','ing-062', 1,    'tsp',   NULL,                    FALSE),
  ('ri-005-8','rec-005','ing-070', 0.5,  'tsp',   NULL,                    FALSE),
  ('ri-005-9','rec-005','ing-069', 1,    'tsp',   NULL,                    FALSE),
  ('ri-005-10','rec-005','ing-047',2,    'tbsp',  NULL,                    FALSE),
  ('ri-005-11','rec-005','ing-060',1,    'tsp',   NULL,                    FALSE);

-- rec-006  Greek Salad
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-006-1','rec-006','ing-001', 2,    'cup',   'chopped',               FALSE),
  ('ri-006-2','rec-006','ing-008', 1,    'piece', 'sliced',                FALSE),
  ('ri-006-3','rec-006','ing-003', 0.5,  'piece', 'thinly sliced',         FALSE),
  ('ri-006-4','rec-006','ing-047', 3,    'tbsp',  NULL,                    FALSE),
  ('ri-006-5','rec-006','ing-018', 1,    'piece', 'juiced',                FALSE),
  ('ri-006-6','rec-006','ing-060', 0.5,  'tsp',   NULL,                    FALSE),
  ('ri-006-7','rec-006','ing-061', 0.25, 'tsp',   NULL,                    FALSE),
  ('ri-006-8','rec-006','ing-033', 2,    'oz',    'feta',                  TRUE);

-- rec-007  Ground Beef Tacos
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-007-1','rec-007','ing-022', 1,    'lb',    'ground',                FALSE),
  ('ri-007-2','rec-007','ing-043', 8,    'piece', 'small tortillas',       FALSE),
  ('ri-007-3','rec-007','ing-033', 2,    'oz',    'shredded',              FALSE),
  ('ri-007-4','rec-007','ing-001', 1,    'cup',   'diced',                 FALSE),
  ('ri-007-5','rec-007','ing-007', 1,    'cup',   'shredded',              FALSE),
  ('ri-007-6','rec-007','ing-062', 1,    'tsp',   NULL,                    FALSE),
  ('ri-007-7','rec-007','ing-064', 1,    'tsp',   NULL,                    FALSE),
  ('ri-007-8','rec-007','ing-060', 1,    'tsp',   NULL,                    FALSE),
  ('ri-007-9','rec-007','ing-035', 0.25, 'cup',   NULL,                    TRUE);

-- rec-008  Garlic Butter Salmon
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-008-1','rec-008','ing-025', 24,   'oz',    'fillets (4 × 6 oz)',    FALSE),
  ('ri-008-2','rec-008','ing-032', 3,    'tbsp',  NULL,                    FALSE),
  ('ri-008-3','rec-008','ing-002', 4,    'clove', NULL,                    FALSE),
  ('ri-008-4','rec-008','ing-018', 1,    'piece', 'juiced',                FALSE),
  ('ri-008-5','rec-008','ing-047', 1,    'tbsp',  NULL,                    FALSE),
  ('ri-008-6','rec-008','ing-060', 1,    'tsp',   NULL,                    FALSE),
  ('ri-008-7','rec-008','ing-061', 0.5,  'tsp',   NULL,                    FALSE),
  ('ri-008-8','rec-008','ing-067', 1,    'tsp',   'fresh or dried',        TRUE);

-- rec-009  Red Lentil Soup
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-009-1','rec-009','ing-044', 1.5,  'cup',   'red lentils',           FALSE),
  ('ri-009-2','rec-009','ing-003', 1,    'piece', 'diced',                 FALSE),
  ('ri-009-3','rec-009','ing-004', 2,    'piece', 'diced',                 FALSE),
  ('ri-009-4','rec-009','ing-001', 1,    'cup',   'chopped',               FALSE),
  ('ri-009-5','rec-009','ing-002', 3,    'clove', NULL,                    FALSE),
  ('ri-009-6','rec-009','ing-050', 4,    'cup',   NULL,                    FALSE),
  ('ri-009-7','rec-009','ing-062', 1,    'tsp',   NULL,                    FALSE),
  ('ri-009-8','rec-009','ing-063', 1,    'tsp',   NULL,                    FALSE),
  ('ri-009-9','rec-009','ing-047', 2,    'tbsp',  NULL,                    FALSE),
  ('ri-009-10','rec-009','ing-060',1,    'tsp',   NULL,                    FALSE);

-- rec-010  Shrimp Fried Rice
INSERT IGNORE INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional) VALUES
  ('ri-010-1','rec-010','ing-024', 1,    'lb',    'peeled and deveined',   FALSE),
  ('ri-010-2','rec-010','ing-038', 2,    'cup',   'cooked, day-old',       FALSE),
  ('ri-010-3','rec-010','ing-030', 2,    'piece', NULL,                    FALSE),
  ('ri-010-4','rec-010','ing-052', 3,    'tbsp',  NULL,                    FALSE),
  ('ri-010-5','rec-010','ing-048', 2,    'tbsp',  NULL,                    FALSE),
  ('ri-010-6','rec-010','ing-002', 3,    'clove', NULL,                    FALSE),
  ('ri-010-7','rec-010','ing-003', 0.5,  'piece', 'diced',                 FALSE),
  ('ri-010-8','rec-010','ing-015', 0.5,  'cup',   NULL,                    TRUE),
  ('ri-010-9','rec-010','ing-004', 1,    'piece', 'small dice',            TRUE);

-- ─────────────────────────────────────────────────────────────
-- Recipe steps
-- ─────────────────────────────────────────────────────────────

-- rec-001  Spaghetti Carbonara
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-001-1','rec-001',1,'Cook pasta in a large pot of well-salted boiling water until al dente. Reserve 1 cup of pasta water before draining.',600),
  ('rs-001-2','rec-001',2,'Fry bacon in a large skillet over medium heat until crispy. Add minced garlic and cook for 1 minute. Remove pan from heat.',180),
  ('rs-001-3','rec-001',3,'In a bowl whisk together eggs, grated cheese, and plenty of black pepper.',0),
  ('rs-001-4','rec-001',4,'Add drained pasta to the skillet, pour in the egg mixture, and toss quickly off the heat, adding pasta water a splash at a time until you have a creamy sauce.',0);

-- rec-002  Chicken Tacos
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-002-1','rec-002',1,'Season chicken breasts all over with cumin, chili powder, salt, and a drizzle of oil.',0),
  ('rs-002-2','rec-002',2,'Cook chicken in a hot pan over medium-high heat for 6–7 minutes per side until cooked through. Let rest for 5 minutes then slice or shred.',420),
  ('rs-002-3','rec-002',3,'Warm tortillas in a dry pan or directly over a gas flame for 30 seconds per side.',0),
  ('rs-002-4','rec-002',4,'Assemble tacos: fill tortillas with chicken, diced tomatoes, onion, a squeeze of lime, and any optional toppings.',0);

-- rec-003  Vegetable Stir Fry
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-003-1','rec-003',1,'Cook rice according to package instructions and set aside.',0),
  ('rs-003-2','rec-003',2,'Heat oil in a wok or large pan over high heat until smoking. Add garlic and ginger; stir-fry 30 seconds.',30),
  ('rs-003-3','rec-003',3,'Add carrots first, cook 1 minute, then add bell peppers and broccoli. Stir-fry 4–5 minutes until crisp-tender.',300),
  ('rs-003-4','rec-003',4,'Pour in soy sauce, toss to coat everything evenly, and serve immediately over rice.',0);

-- rec-004  Classic Omelette
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-004-1','rec-004',1,'Whisk eggs with salt and pepper until well combined and slightly frothy.',0),
  ('rs-004-2','rec-004',2,'Melt butter in a non-stick pan over medium heat. If using, add diced onion or bell pepper and cook 2 minutes.',120),
  ('rs-004-3','rec-004',3,'Pour in eggs. Let the edges set, sprinkle cheese over one half, then fold the omelette in half. Slide onto a plate and serve.',0);

-- rec-005  Chicken Curry
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-005-1','rec-005',1,'Heat olive oil in a large pot over medium heat. Add diced onion and cook until softened, about 5 minutes.',300),
  ('rs-005-2','rec-005',2,'Stir in garlic and ginger; cook 1 minute. Add cumin, turmeric, and coriander; stir for 30 seconds until fragrant.',90),
  ('rs-005-3','rec-005',3,'Add chicken pieces and cook, stirring, until browned on all sides, about 5 minutes.',300),
  ('rs-005-4','rec-005',4,'Add chopped tomatoes and coconut milk. Stir well and bring to a gentle simmer.',0),
  ('rs-005-5','rec-005',5,'Simmer uncovered for 20 minutes until chicken is cooked through and sauce has thickened. Season with salt.',1200);

-- rec-006  Greek Salad
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-006-1','rec-006',1,'Combine chopped tomatoes, sliced cucumber, and thinly sliced onion in a large bowl.',0),
  ('rs-006-2','rec-006',2,'Drizzle with olive oil, squeeze over lemon juice, season with salt and pepper, and toss gently. Top with crumbled feta if using.',0);

-- rec-007  Ground Beef Tacos
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-007-1','rec-007',1,'Brown ground beef in a pan over medium-high heat, breaking it up as it cooks. Drain excess fat.',0),
  ('rs-007-2','rec-007',2,'Season with cumin, chili powder, and salt. Cook 2 more minutes.',120),
  ('rs-007-3','rec-007',3,'Warm tortillas in a dry pan for 30 seconds per side.',0),
  ('rs-007-4','rec-007',4,'Assemble tacos with beef, shredded cheese, diced tomatoes, lettuce, and sour cream if using.',0);

-- rec-008  Garlic Butter Salmon
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-008-1','rec-008',1,'Pat salmon fillets completely dry. Season all over with salt, pepper, and thyme.',0),
  ('rs-008-2','rec-008',2,'Heat olive oil in an oven-safe pan over medium-high heat. Sear salmon skin-side up for 3 minutes without moving.',180),
  ('rs-008-3','rec-008',3,'Flip salmon, add butter and minced garlic to the pan, and continuously baste the fish with the melted butter for 3–4 minutes until cooked through. Finish with a squeeze of lemon juice.',240);

-- rec-009  Red Lentil Soup
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-009-1','rec-009',1,'Heat olive oil in a large pot. Add diced onion and carrot; cook 5 minutes until softened.',300),
  ('rs-009-2','rec-009',2,'Add minced garlic, cumin, and paprika; stir for 1 minute until fragrant.',60),
  ('rs-009-3','rec-009',3,'Add rinsed lentils, chopped tomatoes, and vegetable broth. Bring to a boil.',0),
  ('rs-009-4','rec-009',4,'Reduce heat and simmer for 20–25 minutes, stirring occasionally, until lentils are completely soft.',1500),
  ('rs-009-5','rec-009',5,'Season with salt. Adjust thickness with water if needed. Serve warm with crusty bread.',0);

-- rec-010  Shrimp Fried Rice
INSERT IGNORE INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds) VALUES
  ('rs-010-1','rec-010',1,'Heat oil in a wok over high heat. Add garlic and onion; stir-fry 1 minute.',60),
  ('rs-010-2','rec-010',2,'Push everything to the side of the wok. Crack in eggs and scramble until just set, then mix into the aromatics.',0),
  ('rs-010-3','rec-010',3,'Add shrimp and cook until pink and curled, about 2 minutes per side.',120),
  ('rs-010-4','rec-010',4,'Add cold cooked rice, breaking up any clumps. Stir-fry 2 minutes until heated through.',120),
  ('rs-010-5','rec-010',5,'Add soy sauce and any optional vegetables. Toss everything together and serve immediately.',0);

-- ─────────────────────────────────────────────────────────────
-- Likes  (populates trending + top sections)
-- ─────────────────────────────────────────────────────────────
INSERT IGNORE INTO RECIPE_LIKES (recipe_id, user_id, is_like, created_at) VALUES
  -- Chicken Curry — most liked (5)
  ('rec-005','user-liker-001',TRUE,NOW()),('rec-005','user-liker-002',TRUE,NOW()),
  ('rec-005','user-liker-003',TRUE,NOW()),('rec-005','user-liker-004',TRUE,NOW()),
  ('rec-005','user-liker-005',TRUE,NOW()),
  -- Spaghetti Carbonara (4)
  ('rec-001','user-liker-001',TRUE,NOW()),('rec-001','user-liker-002',TRUE,NOW()),
  ('rec-001','user-liker-003',TRUE,NOW()),('rec-001','user-liker-004',TRUE,NOW()),
  -- Garlic Butter Salmon (4)
  ('rec-008','user-liker-001',TRUE,NOW()),('rec-008','user-liker-002',TRUE,NOW()),
  ('rec-008','user-liker-004',TRUE,NOW()),('rec-008','user-liker-005',TRUE,NOW()),
  -- Shrimp Fried Rice (3)
  ('rec-010','user-liker-002',TRUE,NOW()),('rec-010','user-liker-003',TRUE,NOW()),
  ('rec-010','user-liker-005',TRUE,NOW()),
  -- Ground Beef Tacos (3)
  ('rec-007','user-liker-001',TRUE,NOW()),('rec-007','user-liker-003',TRUE,NOW()),
  ('rec-007','user-liker-004',TRUE,NOW()),
  -- Chicken Tacos (3)
  ('rec-002','user-liker-003',TRUE,NOW()),('rec-002','user-liker-004',TRUE,NOW()),
  ('rec-002','user-liker-005',TRUE,NOW()),
  -- Vegetable Stir Fry (2)
  ('rec-003','user-liker-001',TRUE,NOW()),('rec-003','user-liker-002',TRUE,NOW()),
  -- Classic Omelette (2)
  ('rec-004','user-liker-003',TRUE,NOW()),('rec-004','user-liker-004',TRUE,NOW()),
  -- Red Lentil Soup (2)
  ('rec-009','user-liker-002',TRUE,NOW()),('rec-009','user-liker-005',TRUE,NOW()),
  -- Greek Salad (1)
  ('rec-006','user-liker-001',TRUE,NOW());
