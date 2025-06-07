package org.example;

import org.example.dao.UzytkownikDAO;
import org.example.model.Uzytkownik;
import org.example.DatabaseTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseIntegrationTest {

    private Connection connection;
    private UzytkownikDAO uzytkownikDAO;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:integrationtest", "", "");
        DatabaseTestUtils.setupTestDatabase(connection);
        DatabaseTestUtils.insertTestData(connection);
        uzytkownikDAO = new UzytkownikDAO(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void integration_Login_ShouldWorkWithRealDatabase() throws SQLException {
        // When
        Uzytkownik result = uzytkownikDAO.zaloguj("jkowalski", "password123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImie()).isEqualTo("Jan");
        assertThat(result.getNazwisko()).isEqualTo("Kowalski");
        assertThat(result.getRola()).isEqualTo("KLIENT");
    }

    @Test
    void integration_Login_ShouldReturnNull_ForInvalidCredentials() throws SQLException {
        // When
        Uzytkownik result = uzytkownikDAO.zaloguj("jkowalski", "wrongpassword");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void integration_Login_ShouldReturnNull_ForNonExistentUser() throws SQLException {
        // When
        Uzytkownik result = uzytkownikDAO.zaloguj("nonexistent", "password");

        // Then
        assertThat(result).isNull();
    }
}
