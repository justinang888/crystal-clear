package com.spring.aurora;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * How the React app talks to the controllers (WebConfig, ModelJsonView, SecurityConfig):
 * page loads get index.html, data requests get the controller's model as JSON, and login,
 * logout and saves answer the way the app expects.
 */
class ReactAppTest extends PostgresIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void pageLoadsGetTheReactApp() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/orders/list").with(user("tester")).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void dataRequestsGetTheControllersModelAsJson() throws Exception {
        long customerId = insertCustomer("Json Customer");
        this.mvc.perform(get("/customers/view").param("customerId", String.valueOf(customerId)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.view").value("view-customer"))
                .andExpect(jsonPath("$.customer.name").value("Json Customer"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void validationErrorsComeBackByField() throws Exception {
        long customerId = insertCustomer("Payer");
        this.mvc.perform(post("/payments/save").accept(MediaType.APPLICATION_JSON)
                .param("customerId", String.valueOf(customerId))
                .param("amount", "0")
                .param("withholdingTax", "0")
                .param("paymentType", "CASH")
                .param("remarks", "nothing")
                .param("createdAt", "2026-03-15"))
                .andExpect(jsonPath("$.view").value("new-payment"))
                .andExpect(jsonPath("$.errors.amount").value("Please enter amount greater than 0!"));
    }

    @Test
    void dataRequestsWithoutALoginGet401() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/dashboard").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pageLoadsWithoutALoginGoToTheLoginScreen() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/dashboard").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
        this.mvc.perform(MockMvcRequestBuilders.get("/login").accept(MediaType.TEXT_HTML))
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void loginAnswers200Or401() throws Exception {
        this.jdbc.update("INSERT INTO users (email, enabled, first_name, last_name, password, role, username) "
                + "VALUES ('owner@example.test', true, 'Test', 'Owner', ?, 'ROLE_USER', 'owner')",
                this.passwordEncoder.encode("right password"));
        this.mvc.perform(formLogin().user("owner").password("right password"))
                .andExpect(status().isOk());
        this.mvc.perform(formLogin().user("owner").password("wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void savesWithoutTheCsrfTokenAreRefused() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.post("/expenses/save").with(user("tester"))
                .param("amount", "10").param("description", "no token").param("createdAt", "2026-03-15"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sessionSaysWhoIsLoggedIn() throws Exception {
        this.mvc.perform(get("/session").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("tester"));
    }
}
