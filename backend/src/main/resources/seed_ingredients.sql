-- Seed data for INGREDIENTS table.
-- Canonical names match synonyms.json so the recommendation engine resolves
-- pantry items correctly. Use INSERT IGNORE so re-running is safe.
-- Run against your MySQL DB: mysql -u root -p smartpantry < seed_ingredients.sql

USE smartpantry;

INSERT IGNORE INTO INGREDIENTS (id, name, category, default_unit, image_url) VALUES

-- Produce
('ing-001', 'tomatoes',    'produce', 'cup',   NULL),
('ing-002', 'garlic',      'produce', 'clove',  NULL),
('ing-003', 'onion',       'produce', 'piece',  NULL),
('ing-004', 'carrot',      'produce', 'piece',  NULL),
('ing-005', 'potatoes',    'produce', 'lb',     NULL),
('ing-006', 'spinach',     'produce', 'cup',    NULL),
('ing-007', 'lettuce',     'produce', 'cup',    NULL),
('ing-008', 'cucumber',    'produce', 'piece',  NULL),
('ing-009', 'bell pepper', 'produce', 'piece',  NULL),
('ing-010', 'broccoli',    'produce', 'cup',    NULL),
('ing-011', 'cauliflower', 'produce', 'cup',    NULL),
('ing-012', 'zucchini',    'produce', 'piece',  NULL),
('ing-013', 'mushrooms',   'produce', 'cup',    NULL),
('ing-014', 'corn',        'produce', 'cup',    NULL),
('ing-015', 'peas',        'produce', 'cup',    NULL),
('ing-016', 'green beans', 'produce', 'cup',    NULL),
('ing-017', 'celery',      'produce', 'stalk',  NULL),
('ing-018', 'lemon',       'produce', 'piece',  NULL),
('ing-019', 'lime',        'produce', 'piece',  NULL),
('ing-020', 'tomato paste','produce', 'tbsp',   NULL),

-- Protein
('ing-021', 'chicken',  'protein', 'lb',  NULL),
('ing-022', 'beef',     'protein', 'lb',  NULL),
('ing-023', 'pork',     'protein', 'lb',  NULL),
('ing-024', 'shrimp',   'protein', 'lb',  NULL),
('ing-025', 'salmon',   'protein', 'oz',  NULL),
('ing-026', 'tuna',     'protein', 'oz',  NULL),
('ing-027', 'turkey',   'protein', 'lb',  NULL),
('ing-028', 'bacon',    'protein', 'oz',  NULL),
('ing-029', 'sausage',  'protein', 'oz',  NULL),
('ing-030', 'egg',      'protein', 'piece', NULL),

-- Dairy
('ing-031', 'milk',         'dairy', 'cup',  NULL),
('ing-032', 'butter',       'dairy', 'tbsp', NULL),
('ing-033', 'cheese',       'dairy', 'oz',   NULL),
('ing-034', 'heavy cream',  'dairy', 'cup',  NULL),
('ing-035', 'sour cream',   'dairy', 'cup',  NULL),
('ing-036', 'yogurt',       'dairy', 'cup',  NULL),
('ing-037', 'cream cheese', 'dairy', 'oz',   NULL),

-- Grains & Legumes
('ing-038', 'rice',     'grains', 'cup',  NULL),
('ing-039', 'pasta',    'grains', 'oz',   NULL),
('ing-040', 'flour',    'grains', 'cup',  NULL),
('ing-041', 'oats',     'grains', 'cup',  NULL),
('ing-042', 'bread',    'grains', 'slice',NULL),
('ing-043', 'tortilla', 'grains', 'piece',NULL),
('ing-044', 'lentils',  'grains', 'cup',  NULL),
('ing-045', 'chickpeas','grains', 'cup',  NULL),
('ing-046', 'beans',    'grains', 'cup',  NULL),

-- Pantry / Liquids
('ing-047', 'olive oil',        'pantry', 'tbsp', NULL),
('ing-048', 'vegetable oil',    'pantry', 'tbsp', NULL),
('ing-049', 'chicken broth',    'pantry', 'cup',  NULL),
('ing-050', 'vegetable broth',  'pantry', 'cup',  NULL),
('ing-051', 'coconut milk',     'pantry', 'cup',  NULL),
('ing-052', 'soy sauce',        'pantry', 'tbsp', NULL),
('ing-053', 'vinegar',          'pantry', 'tbsp', NULL),
('ing-054', 'hot sauce',        'pantry', 'tsp',  NULL),
('ing-055', 'honey',            'pantry', 'tbsp', NULL),
('ing-056', 'sugar',            'pantry', 'cup',  NULL),
('ing-057', 'brown sugar',      'pantry', 'cup',  NULL),
('ing-058', 'baking powder',    'pantry', 'tsp',  NULL),
('ing-059', 'baking soda',      'pantry', 'tsp',  NULL),

-- Spices & Herbs
('ing-060', 'salt',         'spices', 'tsp',  NULL),
('ing-061', 'pepper',       'spices', 'tsp',  NULL),
('ing-062', 'cumin',        'spices', 'tsp',  NULL),
('ing-063', 'paprika',      'spices', 'tsp',  NULL),
('ing-064', 'chili powder', 'spices', 'tsp',  NULL),
('ing-065', 'oregano',      'spices', 'tsp',  NULL),
('ing-066', 'basil',        'spices', 'tsp',  NULL),
('ing-067', 'thyme',        'spices', 'tsp',  NULL),
('ing-068', 'cinnamon',     'spices', 'tsp',  NULL),
('ing-069', 'coriander',    'spices', 'tsp',  NULL),
('ing-070', 'turmeric',     'spices', 'tsp',  NULL),
('ing-071', 'ginger',       'spices', 'tsp',  NULL);
