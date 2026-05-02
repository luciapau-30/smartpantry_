package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.dao.IngredientDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/ingredients")
public final class IngredientListServlet extends AbstractJsonServlet {

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeOk(resp, new IngredientDao().getAll());
    }
}
