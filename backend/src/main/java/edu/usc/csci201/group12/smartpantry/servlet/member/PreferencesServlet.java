package edu.usc.csci201.group12.smartpantry.servlet.member;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import edu.usc.csci201.group12.smartpantry.dao.UserPreferenceDao;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// GET  /api/member/preferences — returns current preferred cuisines
// PUT  /api/member/preferences — body: {"cuisines":["Italian","Mexican"]}
@WebServlet("/api/member/preferences")
public final class PreferencesServlet extends AbstractJsonServlet {

    private final UserPreferenceDao prefDao = new UserPreferenceDao();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var member = resolveMember(req, resp);
        if (member == null) return;
        List<String> cuisines = prefDao.getPreferences(member.getId());
        writeOk(resp, Map.of("cuisines", cuisines));
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody)
            throws IOException {
        var member = resolveMember(req, resp);
        if (member == null) return;
        List<String> cuisines = parseCuisines(jsonBody);
        prefDao.setPreferences(member.getId(), cuisines);
        member.setPreferredCuisines(cuisines);
        writeOk(resp, Map.of("cuisines", cuisines));
    }

    private Member resolveMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        var userOpt = RequestUsers.currentUser(req, store);
        if (userOpt.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail("Login required"));
            return null;
        }
        if (!(userOpt.get() instanceof Member member)) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, JsonApiResponse.fail("Member account required"));
            return null;
        }
        return member;
    }

    private static List<String> parseCuisines(String body) {
        List<String> list = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(body).getAsJsonObject().getAsJsonArray("cuisines");
            for (var el : arr) {
                String s = el.getAsString().trim();
                if (!s.isEmpty()) list.add(s);
            }
        } catch (Exception ignored) {}
        return list;
    }
}
