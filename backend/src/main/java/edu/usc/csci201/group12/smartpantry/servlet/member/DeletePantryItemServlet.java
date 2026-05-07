package edu.usc.csci201.group12.smartpantry.servlet.member;

import edu.usc.csci201.group12.smartpantry.dao.PantryItemDao;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemRow;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import edu.usc.csci201.group12.smartpantry.websocket.PantryEventBroadcaster;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// DELETE /api/member/pantry/{itemId}
@WebServlet("/api/member/pantry/*")
public final class DeletePantryItemServlet extends AbstractJsonServlet {

    private final PantryItemDao pantryItemDao = new PantryItemDao();

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType(JSON_UTF8);
        resp.setCharacterEncoding("UTF-8");

        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        var userOpt = RequestUsers.currentUser(req, store);
        if (userOpt.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail("Login required"));
            return;
        }
        if (!(userOpt.get() instanceof Member member)) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, JsonApiResponse.fail("Member account required"));
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("Pantry item id required in path"));
            return;
        }
        String itemId = pathInfo.substring(1);

        // Verify ownership before deleting
        PantryItemRow item = pantryItemDao.getById(itemId);
        if (item == null) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, JsonApiResponse.fail("Pantry item not found"));
            return;
        }
        if (!item.getUserId().equals(member.getId())) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, JsonApiResponse.fail("Not your pantry item"));
            return;
        }

        boolean deleted = pantryItemDao.deleteItem(itemId);
        if (deleted) {
            PantryEventBroadcaster broadcaster = (PantryEventBroadcaster)
                    req.getServletContext().getAttribute(ContextKeys.EVENT_BROADCASTER);
            if (broadcaster != null) {
                broadcaster.pantryUpdated(member.getId(), "removed", itemId);
            }
            writeOk(resp);
        } else {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonApiResponse.fail("Delete failed"));
        }
    }
}
