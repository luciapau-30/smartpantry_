-- SmartPantry & Meal Planner — CSCI 201 Group 12
-- Run this once in MySQL Workbench before starting Tomcat.
-- After running, start Tomcat with PANTRY_DB_URL set — the app seeds INGREDIENTS automatically.

CREATE DATABASE IF NOT EXISTS smartpantry CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartpantry;

CREATE TABLE IF NOT EXISTS USERS (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT NOW(),
    last_login    DATETIME,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    is_guest      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS INGREDIENTS (
    id           VARCHAR(36)  NOT NULL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    category     VARCHAR(50),
    default_unit VARCHAR(20),
    image_url    VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS PANTRY_ITEMS (
    id              VARCHAR(36)   NOT NULL PRIMARY KEY,
    user_id         VARCHAR(36)   NOT NULL,
    ingredient_id   VARCHAR(36)   NOT NULL,
    quantity        DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit            VARCHAR(20),
    expiration_date DATE,
    added_at        DATETIME      NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id)       REFERENCES USERS(id)       ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES INGREDIENTS(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS RECIPES (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    author_id     VARCHAR(36)  NOT NULL,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    image_url     VARCHAR(500),
    prep_time_min INT          DEFAULT 0,
    cook_time_min INT          DEFAULT 0,
    servings      INT          DEFAULT 1,
    is_public     BOOLEAN      NOT NULL DEFAULT TRUE,
    category_tags VARCHAR(500),
    created_at    DATETIME     NOT NULL DEFAULT NOW(),
    FOREIGN KEY (author_id) REFERENCES USERS(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS RECIPE_INGREDIENTS (
    id            VARCHAR(36)   NOT NULL PRIMARY KEY,
    recipe_id     VARCHAR(36)   NOT NULL,
    ingredient_id VARCHAR(36)   NOT NULL,
    quantity      DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit          VARCHAR(20),
    notes         VARCHAR(255),
    is_optional   BOOLEAN       NOT NULL DEFAULT FALSE,
    FOREIGN KEY (recipe_id)     REFERENCES RECIPES(id)     ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES INGREDIENTS(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS RECIPE_STEPS (
    id            VARCHAR(36) NOT NULL PRIMARY KEY,
    recipe_id     VARCHAR(36) NOT NULL,
    step_number   INT         NOT NULL,
    instruction   TEXT        NOT NULL,
    timer_seconds INT         DEFAULT 0,
    FOREIGN KEY (recipe_id) REFERENCES RECIPES(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS RECIPE_LIKES (
    recipe_id  VARCHAR(36) NOT NULL,
    user_id    VARCHAR(36) NOT NULL,
    is_like    BOOLEAN     NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (recipe_id, user_id),
    FOREIGN KEY (recipe_id) REFERENCES RECIPES(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)   REFERENCES USERS(id)   ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS RECIPE_SAVES (
    recipe_id VARCHAR(36) NOT NULL,
    user_id   VARCHAR(36) NOT NULL,
    saved_at  DATETIME    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (recipe_id, user_id),
    FOREIGN KEY (recipe_id) REFERENCES RECIPES(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)   REFERENCES USERS(id)   ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS COMMENTS (
    id                VARCHAR(36) NOT NULL PRIMARY KEY,
    recipe_id         VARCHAR(36) NOT NULL,
    user_id           VARCHAR(36) NOT NULL,
    parent_comment_id VARCHAR(36),
    body              TEXT        NOT NULL,
    created_at        DATETIME    NOT NULL DEFAULT NOW(),
    is_deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    FOREIGN KEY (recipe_id)         REFERENCES RECIPES(id)  ON DELETE CASCADE,
    FOREIGN KEY (user_id)           REFERENCES USERS(id)    ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES COMMENTS(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS USER_ALLERGIES (
    user_id  VARCHAR(36)  NOT NULL,
    allergen VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, allergen),
    FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS USER_PREFERENCES (
    user_id VARCHAR(36)  NOT NULL,
    cuisine VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, cuisine),
    FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE
);
