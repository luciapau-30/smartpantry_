// MERGE (lucia/recommendation-engine → dev): Javadoc conflict only — kept Archit's more
// descriptive comment referencing Member domain methods. Code is identical on both branches.
package edu.usc.csci201.group12.smartpantry.jdbc;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC entry point for Chenyang-style domain methods ({@code Member} pantry/recipe/comment SQL).
 * Configure JNDI {@code java:comp/env/jdbc/PantryAppDB} or env {@code PANTRY_DB_URL} (+ optional user/password).
 */
public final class JdbcConnectionFactory {

    private JdbcConnectionFactory() {
    }

    public static Connection openConnection() throws SQLException {
        try {
            InitialContext context = new InitialContext();
            Object value = context.lookup("java:comp/env/jdbc/PantryAppDB");
            if (value instanceof DataSource dataSource) {
                return dataSource.getConnection();
            }
        } catch (NamingException ignored) {
            // fall through to env-based URL
        }

        String url = firstNonBlank(System.getProperty("pantry.db.url"), System.getenv("PANTRY_DB_URL"));
        String user = firstNonBlank(System.getProperty("pantry.db.user"), System.getenv("PANTRY_DB_USER"));
        String password = firstNonBlank(System.getProperty("pantry.db.password"), System.getenv("PANTRY_DB_PASSWORD"));

        if (url == null) {
            throw new SQLException("No database connection configured. Set JNDI jdbc/PantryAppDB or PANTRY_DB_URL.");
        }
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException ignored) {}
        if (user == null) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, password == null ? "" : password);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
