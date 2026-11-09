package com.spring.aurora.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.spring.aurora.PostgresIntegrationTest;

/** The command-line tool that adds a login (owner or bookkeeper) without a password in any file. */
class AddUserTest extends PostgresIntegrationTest {

    @Autowired
    private DataSource dataSource;

    private void add(String username, String role, String password) throws Exception {
        try (Connection connection = this.dataSource.getConnection()) {
            AddUser.insert(connection, username, role, password.toCharArray());
        }
    }

    @Test
    void addsAnOwnerWhoseLoginWorksAndCanReconcile() throws Exception {
        add("boss", "owner", "correct horse battery staple");

        Map<String, Object> row = this.jdbc.queryForMap("SELECT * FROM users WHERE username = 'boss'");
        assertThat(row.get("role")).isEqualTo("ROLE_OWNER");
        assertThat(row.get("enabled")).isEqualTo(true);
        assertThat((String) row.get("password")).startsWith("$2a$11$").doesNotContain("correct horse");

        MockHttpSession session = (MockHttpSession) this.mvc.perform(formLogin().user("boss").password("correct horse battery staple"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
        this.mvc.perform(MockMvcRequestBuilders.get("/session").session(session).accept("application/json"))
                .andExpect(jsonPath("$.canReconcile").value(true));
    }

    @Test
    void addsABookkeeperWhoCantReconcile() throws Exception {
        add("clerk", "bookkeeper", "another long password");

        assertThat(this.jdbc.queryForObject("SELECT role FROM users WHERE username = 'clerk'", String.class)).isEqualTo("ROLE_ADMIN");
        MockHttpSession session = (MockHttpSession) this.mvc.perform(formLogin().user("clerk").password("another long password"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
        this.mvc.perform(MockMvcRequestBuilders.get("/session").session(session).accept("application/json"))
                .andExpect(jsonPath("$.canReconcile").value(false));
    }

    @Test
    void aTakenUsernameIsRefusedWhateverItsCase() throws Exception {
        add("boss", "owner", "first password here");

        assertThatThrownBy(() -> add("BOSS", "bookkeeper", "second password"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already exists");
        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM users", Integer.class)).isEqualTo(1);
    }

    @Test
    void badInputIsRefusedBeforeAnythingIsSaved() {
        assertThatThrownBy(() -> add(" ", "owner", "a fine password")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> add("x".repeat(46), "owner", "a fine password")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> add("boss", "manager", "a fine password")).hasMessageContaining("owner or bookkeeper");
        assertThatThrownBy(() -> add("boss", "owner", "short")).hasMessageContaining("at least 8");
        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM users", Integer.class)).isZero();
    }
}
