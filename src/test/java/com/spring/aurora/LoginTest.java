package com.spring.aurora;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** Logging in through the real login form against users stored in PostgreSQL. */
class LoginTest extends PostgresIntegrationTest {

    private static final String PASSWORD = "correct horse battery staple";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void insertUser() {
        this.jdbc.update("INSERT INTO users (email, enabled, first_name, last_name, password, role, username) "
                + "VALUES ('cashier@example.test', true, 'Test', 'Cashier', ?, 'ROLE_USER', 'cashier')",
                this.passwordEncoder.encode(PASSWORD));
    }

    @Test
    void pagesRequireLogin() throws Exception {
        // Not the inherited get(), which is already logged in.
        this.mvc.perform(MockMvcRequestBuilders.get("/orders/list").accept(MediaType.TEXT_HTML))
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void usernameMatchesRegardlessOfCase() throws Exception {
        this.mvc.perform(formLogin().user("cashier").password(PASSWORD)).andExpect(authenticated());
        // MySQL compared usernames case-insensitively; PostgreSQL must keep that behavior.
        this.mvc.perform(formLogin().user("CASHIER").password(PASSWORD)).andExpect(authenticated());
    }

    @Test
    void emailCanBeUsedInsteadOfUsername() throws Exception {
        this.mvc.perform(formLogin().user("Cashier@Example.test").password(PASSWORD)).andExpect(authenticated());
    }

    @Test
    void wrongPasswordOrUnknownUserIsRejected() throws Exception {
        this.mvc.perform(formLogin().user("cashier").password("wrong")).andExpect(unauthenticated());
        this.mvc.perform(formLogin().user("nobody").password(PASSWORD)).andExpect(unauthenticated());
    }

    @Test
    void disabledUserIsRejected() throws Exception {
        this.jdbc.update("UPDATE users SET enabled = false");
        this.mvc.perform(formLogin().user("cashier").password(PASSWORD)).andExpect(unauthenticated());
    }
}
