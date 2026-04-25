package edu.usc.csci201.group12.smartpantry.servlet;

import com.google.gson.JsonSyntaxException;
import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Base servlet for AJAX/fetch endpoints: UTF-8 JSON in and out, uniform error handling.
 * Subclasses implement {@link #handlePost(HttpServletRequest, HttpServletResponse, String)}
 * or other verbs as needed.
 */
public abstract class AbstractJsonServlet extends HttpServlet {

    protected static final String JSON_UTF8 = "application/json; charset=UTF-8";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(JSON_UTF8);

        String body = readBody(req);
        try {
            handlePost(req, resp, body);
        } catch (JsonSyntaxException ex) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("Invalid JSON body"));
        } catch (IllegalArgumentException ex) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail(ex.getMessage()));
        } catch (SecurityException ex) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonApiResponse.fail("Server error"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(JSON_UTF8);
        try {
            handleGet(req, resp);
        } catch (SecurityException ex) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonApiResponse.fail("Server error"));
        }
    }

    /**
     * Override for POST JSON APIs. Default: 405.
     */
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody)
            throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Override for GET JSON APIs. Default: 405.
     */
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected static String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (var reader = req.getReader()) {
            char[] buf = new char[2048];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
        }
        return sb.toString();
    }

    protected static void writeJson(HttpServletResponse resp, int status, JsonApiResponse payload) throws IOException {
        resp.setStatus(status);
        var gson = GsonProvider.get();
        gson.toJson(payload, resp.getWriter());
    }

    protected static void writeOk(HttpServletResponse resp, Object data) throws IOException {
        writeJson(resp, HttpServletResponse.SC_OK, JsonApiResponse.ok(data));
    }

    protected static void writeOk(HttpServletResponse resp) throws IOException {
        writeJson(resp, HttpServletResponse.SC_OK, JsonApiResponse.ok());
    }
}
