// MERGE (lucia/recommendation-engine → dev): Added recommendation engine wiring —
// imports for IngredientSynonymResolver, PantryMatcher, RecipeScorer, RecipeRecommender,
// ScoreWeights, UnitNormalizer. recipeRepo reference is now captured so the recommender
// can be built from it and stored under ContextKeys.RECOMMENDER at startup.
package edu.usc.csci201.group12.smartpantry.web;

import edu.usc.csci201.group12.smartpantry.dao.InMemoryUserStore;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.recipe.InMemoryRecipeRepository;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeRepository;
import edu.usc.csci201.group12.smartpantry.recommendation.IngredientSynonymResolver;
import edu.usc.csci201.group12.smartpantry.recommendation.PantryMatcher;
import edu.usc.csci201.group12.smartpantry.recommendation.RecipeRecommender;
import edu.usc.csci201.group12.smartpantry.recommendation.RecipeScorer;
import edu.usc.csci201.group12.smartpantry.recommendation.ScoreWeights;
import edu.usc.csci201.group12.smartpantry.recommendation.UnitNormalizer;
import edu.usc.csci201.group12.smartpantry.security.PasswordHasher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Wires default in-memory implementations. Teammates can replace attributes with JDBC-backed beans here.
 */
@WebListener
public final class SmartPantryBootstrapListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        if (ctx.getAttribute(ContextKeys.USER_STORE) == null) {
            ctx.setAttribute(ContextKeys.USER_STORE, new InMemoryUserStore());
        }
        if (ctx.getAttribute(ContextKeys.PASSWORD_HASHER) == null) {
            ctx.setAttribute(ContextKeys.PASSWORD_HASHER, new PasswordHasher());
        }

        RecipeRepository recipeRepo;
        if (ctx.getAttribute(ContextKeys.RECIPE_REPOSITORY) == null) {
            recipeRepo = new InMemoryRecipeRepository();
            ctx.setAttribute(ContextKeys.RECIPE_REPOSITORY, recipeRepo);
        } else {
            recipeRepo = (RecipeRepository) ctx.getAttribute(ContextKeys.RECIPE_REPOSITORY);
        }

        if (ctx.getAttribute(ContextKeys.RECOMMENDER) == null) {
            IngredientSynonymResolver synonymResolver = IngredientSynonymResolver.loadFromClasspath();
            UnitNormalizer unitNormalizer = new UnitNormalizer();
            PantryMatcher pantryMatcher = new PantryMatcher(synonymResolver, unitNormalizer);
            RecipeScorer scorer = new RecipeScorer(pantryMatcher);
            RecipeRecommender recommender = new RecipeRecommender(recipeRepo, scorer, ScoreWeights.defaults());
            ctx.setAttribute(ContextKeys.RECOMMENDER, recommender);
        }
    }
}
