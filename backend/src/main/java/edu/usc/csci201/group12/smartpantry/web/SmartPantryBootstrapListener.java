// MERGE (lucia/recommendation-engine → dev): Added recommendation engine wiring.
// PORTED (zeqiang/database): Added JDBC store wiring — uses JdbcUserStore + JdbcRecipeRepository
// when PANTRY_DB_URL env var is set, falls back to in-memory implementations otherwise.
// T2/T3/T4: Adds BackgroundJobs thread pool, ExpiryCheckJob scheduled at fixed rate, and
// PantryEventBroadcaster for the WebSocket fan-out. Pool is shut down in contextDestroyed.
package edu.usc.csci201.group12.smartpantry.web;

import edu.usc.csci201.group12.smartpantry.background.BackgroundJobs;
import edu.usc.csci201.group12.smartpantry.background.ExpiryCheckJob;
import edu.usc.csci201.group12.smartpantry.dao.InMemoryUserStore;
import edu.usc.csci201.group12.smartpantry.dao.JdbcUserStore;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeStepDao;
import edu.usc.csci201.group12.smartpantry.dao.UserDao;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.recipe.InMemoryRecipeRepository;
import edu.usc.csci201.group12.smartpantry.recipe.JdbcRecipeRepository;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeDao;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeRepository;
import edu.usc.csci201.group12.smartpantry.recommendation.IngredientSynonymResolver;
import edu.usc.csci201.group12.smartpantry.recommendation.PantryMatcher;
import edu.usc.csci201.group12.smartpantry.recommendation.RecipeRecommender;
import edu.usc.csci201.group12.smartpantry.recommendation.RecipeScorer;
import edu.usc.csci201.group12.smartpantry.recommendation.ScoreWeights;
import edu.usc.csci201.group12.smartpantry.recommendation.UnitNormalizer;
import edu.usc.csci201.group12.smartpantry.security.PasswordHasher;
import edu.usc.csci201.group12.smartpantry.websocket.PantryEventBroadcaster;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.TimeUnit;

/**
 * Wires stores at startup. Uses JDBC implementations when PANTRY_DB_URL is set,
 * falls back to in-memory so the app still runs without a database (dev/demo mode).
 */
@WebListener
public final class SmartPantryBootstrapListener implements ServletContextListener {

    private static final int  BACKGROUND_POOL_SIZE   = 4;
    private static final long EXPIRY_CHECK_PERIOD_MIN = 5;
    private static final int  EXPIRY_WINDOW_DAYS     = 3;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        boolean useJdbc = System.getenv("PANTRY_DB_URL") != null
                || System.getProperty("pantry.db.url") != null;

        if (ctx.getAttribute(ContextKeys.PASSWORD_HASHER) == null) {
            ctx.setAttribute(ContextKeys.PASSWORD_HASHER, new PasswordHasher());
        }

        RecipeRepository recipeRepo;
        if (ctx.getAttribute(ContextKeys.RECIPE_REPOSITORY) == null) {
            recipeRepo = useJdbc
                    ? new JdbcRecipeRepository(new RecipeDao(), new RecipeIngredientDao(), new RecipeStepDao())
                    : new InMemoryRecipeRepository();
            ctx.setAttribute(ContextKeys.RECIPE_REPOSITORY, recipeRepo);
        } else {
            recipeRepo = (RecipeRepository) ctx.getAttribute(ContextKeys.RECIPE_REPOSITORY);
        }

        if (ctx.getAttribute(ContextKeys.USER_STORE) == null) {
            UserStore userStore = useJdbc
                    ? new JdbcUserStore(new UserDao(), recipeRepo)
                    : new InMemoryUserStore();
            ctx.setAttribute(ContextKeys.USER_STORE, userStore);
        }

        if (ctx.getAttribute(ContextKeys.RECOMMENDER) == null) {
            IngredientSynonymResolver synonymResolver = IngredientSynonymResolver.loadFromClasspath();
            UnitNormalizer unitNormalizer = new UnitNormalizer();
            PantryMatcher pantryMatcher = new PantryMatcher(synonymResolver, unitNormalizer);
            RecipeScorer scorer = new RecipeScorer(pantryMatcher);
            RecipeRecommender recommender = new RecipeRecommender(recipeRepo, scorer, ScoreWeights.defaults());
            ctx.setAttribute(ContextKeys.RECOMMENDER, recommender);
        }

        // T4: broadcaster published to context so servlets can fire pantry-update events.
        PantryEventBroadcaster broadcaster;
        if (ctx.getAttribute(ContextKeys.EVENT_BROADCASTER) == null) {
            broadcaster = new PantryEventBroadcaster();
            ctx.setAttribute(ContextKeys.EVENT_BROADCASTER, broadcaster);
        } else {
            broadcaster = (PantryEventBroadcaster) ctx.getAttribute(ContextKeys.EVENT_BROADCASTER);
        }

        // T2 + T3: thread pool + scheduled expiry job.
        if (ctx.getAttribute(ContextKeys.BACKGROUND_JOBS) == null) {
            BackgroundJobs jobs = new BackgroundJobs(BACKGROUND_POOL_SIZE);
            ExpiryCheckJob expiryJob = new ExpiryCheckJob(
                    new PantryItemDao(),
                    broadcaster,
                    EXPIRY_WINDOW_DAYS,
                    useJdbc
            );
            jobs.scheduleAtFixedRate(expiryJob, 30, EXPIRY_CHECK_PERIOD_MIN * 60, TimeUnit.SECONDS);
            ctx.setAttribute(ContextKeys.BACKGROUND_JOBS, jobs);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Object jobs = sce.getServletContext().getAttribute(ContextKeys.BACKGROUND_JOBS);
        if (jobs instanceof BackgroundJobs bg) {
            bg.shutdown();
        }
    }
}
