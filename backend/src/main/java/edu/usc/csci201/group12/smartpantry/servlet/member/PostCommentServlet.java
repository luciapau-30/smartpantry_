package edu.usc.csci201.group12.smartpantry.servlet.member;

import edu.usc.csci201.group12.smartpantry.api.member.PostCommentRequest;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.model.content.Comment;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "postComment", urlPatterns = "/api/member/comments/post")
public final class PostCommentServlet extends AbstractJsonServlet {

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody) throws IOException {
        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        var userOpt = RequestUsers.currentUser(req, store);
        if (userOpt.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail("Login required"));
            return;
        }
        User user = userOpt.get();
        if (!(user instanceof Member member)) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, JsonApiResponse.fail("Member account required"));
            return;
        }

        PostCommentRequest in = GsonProvider.get().fromJson(jsonBody, PostCommentRequest.class);
        if (in == null || in.recipeId() == null || in.textBody() == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("recipeId and textBody required"));
            return;
        }

        Comment comment = new Comment(member.getId(), in.recipeId(), in.textBody());
        comment.setParentCommentId(in.parentCommentId());

        try {
            String commentId = member.postComment(comment);
            writeJson(resp, HttpServletResponse.SC_CREATED, JsonApiResponse.ok(Map.of(
                    "commentId", commentId,
                    "recipeId", in.recipeId())));
        } catch (SQLException ex) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonApiResponse.fail("Database error"));
        }
    }
}
