package edu.usc.csci201.group12.smartpantry.web;

import edu.usc.csci201.group12.smartpantry.dao.InMemoryUserStore;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.recipe.InMemoryRecipeRepository;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeRepository;
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
        if (ctx.getAttribute(ContextKeys.RECIPE_REPOSITORY) == null) {
            ctx.setAttribute(ContextKeys.RECIPE_REPOSITORY, new InMemoryRecipeRepository());
        }
    }
}
