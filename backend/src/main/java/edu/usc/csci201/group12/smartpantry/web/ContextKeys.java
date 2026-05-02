// MERGE (lucia/recommendation-engine → dev): Added RECOMMENDER context key for the
// RecipeRecommender instance wired at startup in SmartPantryBootstrapListener.
// T2/T3/T4: Added BACKGROUND_JOBS (thread pool) and EVENT_BROADCASTER (WebSocket fan-out).
package edu.usc.csci201.group12.smartpantry.web;

public final class ContextKeys {
    public static final String USER_STORE        = "smartpantry.userStore";
    public static final String PASSWORD_HASHER   = "smartpantry.passwordHasher";
    public static final String RECIPE_REPOSITORY = "smartpantry.recipeRepository";
    public static final String RECOMMENDER       = "smartpantry.recommender";
    public static final String BACKGROUND_JOBS   = "smartpantry.backgroundJobs";
    public static final String EVENT_BROADCASTER = "smartpantry.eventBroadcaster";

    private ContextKeys() {
    }
}
