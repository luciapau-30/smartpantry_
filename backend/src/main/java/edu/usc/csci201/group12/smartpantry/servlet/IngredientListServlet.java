package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.dao.Ingredient;
import edu.usc.csci201.group12.smartpantry.dao.IngredientDao;
import edu.usc.csci201.group12.smartpantry.recommendation.IngredientSynonymResolver;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/ingredients")
public final class IngredientListServlet extends AbstractJsonServlet {

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String resolve = req.getParameter("resolve");
        if (resolve != null && !resolve.isBlank()) {
            resolveIngredient(resolve.trim(), resp);
            return;
        }
        writeOk(resp, new IngredientDao().getAll());
    }

    // GET /api/ingredients?resolve=steak
    // Returns { canonical, id, exact } — canonical is the synonym-resolved name,
    // exact=true means the input already matched the DB directly.
    private void resolveIngredient(String raw, HttpServletResponse resp) throws IOException {
        IngredientSynonymResolver resolver = IngredientSynonymResolver.loadFromClasspath();
        String canonical = resolver.canonical(raw.toLowerCase());

        IngredientDao dao = new IngredientDao();
        Ingredient found = dao.getByName(canonical);
        if (found == null && !canonical.equals(raw.toLowerCase())) {
            found = dao.getByName(raw);
        }

        boolean exact = raw.equalsIgnoreCase(canonical) ||
                        (found != null && raw.equalsIgnoreCase(found.getName()));

        writeOk(resp, Map.of(
            "input",    raw,
            "canonical", found != null ? found.getName() : canonical,
            "id",        found != null ? found.getId() : "",
            "exact",     exact
        ));
    }
}
