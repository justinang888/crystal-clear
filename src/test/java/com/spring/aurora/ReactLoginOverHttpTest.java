package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The React login against a real server, as the browser does it: read the XSRF-TOKEN cookie,
 * post the username and password with that token in the X-XSRF-TOKEN header, then load data.
 * (MockMvc swaps in a test CSRF store, so the cookie can only be checked over HTTP.)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReactLoginOverHttpTest extends PostgresIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final CookieManager cookies = new CookieManager();
    private final HttpClient http = HttpClient.newBuilder().cookieHandler(this.cookies).build();

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return this.http.send(request.header("Accept", "application/json").build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI url(String path) {
        return URI.create("http://localhost:" + this.port + "/aurora" + path);
    }

    private String xsrfToken() {
        return this.cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("XSRF-TOKEN")).map(HttpCookie::getValue).findFirst().orElse(null);
    }

    private HttpRequest.Builder loginPost(String password, String token) {
        HttpRequest.Builder post = HttpRequest.newBuilder(url("/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=owner&password=" + password));
        return token == null ? post : post.header("X-XSRF-TOKEN", token);
    }

    @Test
    void loginWithTheCookieTokenThenLoadData() throws Exception {
        this.jdbc.update("INSERT INTO users (email, enabled, first_name, last_name, password, role, username) "
                + "VALUES ('owner@example.test', true, 'Test', 'Owner', ?, 'ROLE_USER', 'owner')",
                this.passwordEncoder.encode("right-password"));

        assertThat(send(HttpRequest.newBuilder(url("/session"))).statusCode()).isEqualTo(401);
        String token = xsrfToken();
        assertThat(token).as("XSRF-TOKEN cookie before logging in").isNotBlank();

        assertThat(send(loginPost("right-password", null)).statusCode()).as("login without the token").isEqualTo(403);
        assertThat(send(loginPost("wrong", token)).statusCode()).isEqualTo(401);
        assertThat(send(loginPost("right-password", xsrfToken())).statusCode()).isEqualTo(200);

        HttpResponse<String> session = send(HttpRequest.newBuilder(url("/session")));
        assertThat(session.statusCode()).isEqualTo(200);
        assertThat(session.body()).contains("\"username\":\"owner\"");
    }
}
