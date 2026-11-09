package com.spring.aurora.tools;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.spring.aurora.config.security.Roles;

/**
 * Adds a login: an owner (everything, including the bank reconciliation) or a bookkeeper
 * (everything except the reconciliation). Asks for the password without echoing it and stores
 * only its BCrypt hash, so no password is ever written to a file or a command line.
 * Uses the same AURORA_DB_URL, AURORA_DB_USERNAME and AURORA_DB_PASSWORD as the app, and asks
 * for the database password (hidden) when AURORA_DB_PASSWORD isn't set.
 * Run from the deployed war:
 *
 *   java -cp aurora.war -Dloader.path=WEB-INF/classes,WEB-INF/lib -Dloader.main=com.spring.aurora.tools.AddUser org.springframework.boot.loader.launch.PropertiesLauncher
 */
public class AddUser {

    static final int MIN_PASSWORD_LENGTH = 8;
    /** users.username is varchar(45). */
    static final int MAX_USERNAME_LENGTH = 45;

    public static void main(String[] args) throws IOException {
        String url = env("AURORA_DB_URL", "jdbc:postgresql://localhost:5432/aurora");
        String dbUser = env("AURORA_DB_USERNAME", "aurora");
        Console console = System.console();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        // Set on the shop PC; on a PC where the start script asks for it, ask here the same way.
        String dbPassword = System.getenv("AURORA_DB_PASSWORD");
        if (dbPassword == null || dbPassword.isEmpty()) {
            dbPassword = new String(askPassword(console, in, "Password for the " + dbUser + " database user: "));
        }

        String username = ask(console, in, "Username for the new login: ");
        String role = ask(console, in, "Role (owner or bookkeeper): ");
        char[] password = askPassword(console, in, "Password: ");
        char[] again = askPassword(console, in, "Password again: ");
        try {
            if (!Arrays.equals(password, again)) {
                System.err.println("The two passwords differ; nothing was saved.");
                System.exit(1);
            }
            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
                insert(connection, username, role, password);
            }
            System.out.println("Added " + role.strip().toLowerCase(Locale.ROOT) + " login '" + username.strip() + "'.");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage() + " Nothing was saved.");
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Couldn't save the login: " + e.getMessage());
            System.exit(1);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(again, '\0');
        }
    }

    /**
     * Checks the input and inserts the login. Throws IllegalArgumentException, saving nothing,
     * for a blank or too long username, an unknown role, a short password or a taken username.
     */
    static void insert(Connection connection, String username, String role, char[] password) throws SQLException {
        String name = username == null ? "" : username.strip();
        if (name.isEmpty() || name.length() > MAX_USERNAME_LENGTH || name.contains("@")) {
            throw new IllegalArgumentException("The username must be 1 to " + MAX_USERNAME_LENGTH + " characters, without @.");
        }
        String authority = switch (role == null ? "" : role.strip().toLowerCase(Locale.ROOT)) {
            case "owner" -> Roles.OWNER_AUTHORITY;
            case "bookkeeper" -> Roles.BOOKKEEPER_AUTHORITY;
            default -> throw new IllegalArgumentException("The role must be owner or bookkeeper.");
        };
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("The password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        try (PreparedStatement taken = connection.prepareStatement("SELECT 1 FROM users WHERE lower(username) = lower(?)")) {
            taken.setString(1, name);
            try (ResultSet rows = taken.executeQuery()) {
                if (rows.next()) {
                    throw new IllegalArgumentException("A login named '" + name + "' already exists.");
                }
            }
        }
        // Same strength as the application's PasswordEncoder bean (BCryptEncoder).
        String hash = new BCryptPasswordEncoder(11).encode(new String(password));
        try (PreparedStatement add = connection.prepareStatement(
                "INSERT INTO users (username, password, role, enabled) VALUES (?, ?, ?, true)")) {
            add.setString(1, name);
            add.setString(2, hash);
            add.setString(3, authority);
            add.executeUpdate();
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static String ask(Console console, BufferedReader in, String prompt) throws IOException {
        if (console != null) {
            return console.readLine(prompt);
        }
        System.out.print(prompt);
        return in.readLine();
    }

    private static char[] askPassword(Console console, BufferedReader in, String prompt) throws IOException {
        if (console != null) {
            char[] typed = console.readPassword(prompt);
            return typed == null ? new char[0] : typed;
        }
        System.out.print(prompt);
        String line = in.readLine();
        return line == null ? new char[0] : line.toCharArray();
    }
}
