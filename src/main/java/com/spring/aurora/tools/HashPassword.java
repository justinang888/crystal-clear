package com.spring.aurora.tools;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Prints the BCrypt hash of a password for the users.password column.
 * Run from the deployed war (prompts without echoing the password):
 *
 *   java -cp aurora.war -Dloader.path=WEB-INF/classes,WEB-INF/lib -Dloader.main=com.spring.aurora.tools.HashPassword org.springframework.boot.loader.launch.PropertiesLauncher
 */
public class HashPassword {

    public static void main(String[] args) throws IOException {
        String password;
        Console console = System.console();
        if (console != null) {
            password = new String(console.readPassword("Password: "));
        } else {
            password = new BufferedReader(new InputStreamReader(System.in)).readLine();
        }
        if (password == null || password.isEmpty()) {
            System.err.println("No password given.");
            System.exit(1);
        }
        // Same strength as the application's PasswordEncoder bean (BCryptEncoder).
        System.out.println(new BCryptPasswordEncoder(11).encode(password));
    }
}
