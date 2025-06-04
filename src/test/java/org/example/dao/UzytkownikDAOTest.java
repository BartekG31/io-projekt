package org.example.dao;

import org.example.model.Uzytkownik;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class UzytkownikDAOTest {

    private Connection connection;
    private UzytkownikDAO uzytkownikDAO;
    private String dbName;

    @BeforeEach
    void setUp() throws SQLException {
        // Unikalna nazwa bazy dla każdego testu
        dbName = "test_" + UUID.randomUUID().toString().replace("-", "");

        // Konfiguracja bazy H2 w pamięci dla testów z unikalną nazwą
        connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1", "sa", "");
        uzytkownikDAO = new UzytkownikDAO(connection);

        // Utworzenie tabeli testowej
        createTestTable();
        insertTestData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            // Usuń tabelę i zamknij połączenie
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS UZYTKOWNIK");
            }
            connection.close();
        }
    }

    private void createTestTable() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS UZYTKOWNIK (
                id INT PRIMARY KEY AUTO_INCREMENT,
                imie VARCHAR(50) NOT NULL,
                nazwisko VARCHAR(50) NOT NULL,
                login VARCHAR(50) UNIQUE NOT NULL,
                haslo VARCHAR(100) NOT NULL,
                rola VARCHAR(20) NOT NULL
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    private void insertTestData() throws SQLException {
        String insertSQL = """
            INSERT INTO UZYTKOWNIK (imie, nazwisko, login, haslo, rola) VALUES
            ('Jan', 'Kowalski', 'jkowalski', 'haslo123', 'KLIENT'),
            ('Anna', 'Nowak', 'anowak', 'secure456', 'MAGAZYNIER'),
            ('Piotr', 'Wiśniewski', 'pwisniewski', 'pass789', 'KURIER'),
            ('Maria', 'Dąbrowska', 'mdabrowska', 'admin123', 'LOGISTYK'),
            ('Test', 'User', 'testuser', 'TESTPASS', 'KLIENT')
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(insertSQL);
        }
    }

    @Test
    @DisplayName("Powinien pomyślnie zalogować użytkownika z poprawnymi danymi")
    void shouldSuccessfullyLoginUserWithCorrectCredentials() throws SQLException {
        // Given
        String login = "jkowalski";
        String haslo = "haslo123";

        // When
        Uzytkownik result = uzytkownikDAO.zaloguj(login, haslo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo(login);
        assertThat(result.getImie()).isEqualTo("Jan");
        assertThat(result.getNazwisko()).isEqualTo("Kowalski");
        assertThat(result.getRola()).isEqualTo("KLIENT");
        assertThat(result.getId()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Powinien zwrócić null dla niepoprawnego loginu")
    void shouldReturnNullForIncorrectLogin() throws SQLException {
        // Given
        String login = "nieistniejacy";
        String haslo = "haslo123";

        // When
        Uzytkownik result = uzytkownikDAO.zaloguj(login, haslo);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Powinien zwrócić null dla niepoprawnego hasła")
    void shouldReturnNullForIncorrectPassword() throws SQLException {
        // Given
        String login = "jkowalski";
        String haslo = "zlehaslo";

        // When
        Uzytkownik result = uzytkownikDAO.zaloguj(login, haslo);

        // Then
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @DisplayName("Powinien być case-insensitive dla loginu i hasła")
    @CsvSource({
            "JKOWALSKI, HASLO123",
            "jkowalski, HASLO123",
            "JKOWALSKI, haslo123",
            "JKowalski, HaSLo123"
    })
    void shouldBeCaseInsensitiveForLoginAndPassword(String login, String haslo) throws SQLException {
        // When
        Uzytkownik result = uzytkownikDAO.zaloguj(login, haslo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImie()).isEqualTo("Jan");
        assertThat(result.getNazwisko()).isEqualTo("Kowalski");
    }

    @ParameterizedTest
    @DisplayName("Powinien zalogować różnych użytkowników")
    @CsvSource({
            "anowak, secure456, Anna, Nowak, MAGAZYNIER",
            "pwisniewski, pass789, Piotr, Wiśniewski, KURIER",
            "mdabrowska, admin123, Maria, Dąbrowska, LOGISTYK"
    })
    void shouldLoginDifferentUsers(String login, String haslo, String imie, String nazwisko, String rola) throws SQLException {
        // When
        Uzytkownik result = uzytkownikDAO.zaloguj(login, haslo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImie()).isEqualTo(imie);
        assertThat(result.getNazwisko()).isEqualTo(nazwisko);
        assertThat(result.getRola()).isEqualTo(rola);
    }

    @ParameterizedTest
    @DisplayName("Powinien zwrócić null dla pustych wartości")
    @ValueSource(strings = {"", " ", "   "})
    void shouldReturnNullForEmptyValues(String emptyValue) throws SQLException {
        // When & Then
        assertThat(uzytkownikDAO.zaloguj(emptyValue, "haslo123")).isNull();
        assertThat(uzytkownikDAO.zaloguj("jkowalski", emptyValue)).isNull();
    }

    @Test
    @DisplayName("Powinien zwrócić null dla wartości null")
    void shouldReturnNullForNullValues() throws SQLException {
        // When & Then
        assertThat(uzytkownikDAO.zaloguj(null, "haslo123")).isNull();
        assertThat(uzytkownikDAO.zaloguj("jkowalski", null)).isNull();
        assertThat(uzytkownikDAO.zaloguj(null, null)).isNull();
    }

    @Test
    @DisplayName("Powinien obsłużyć SQL Injection attack")
    void shouldHandleSqlInjectionAttack() throws SQLException {
        // Given
        String maliciousLogin = "jkowalski'; DROP TABLE UZYTKOWNIK; --";
        String normalPassword = "haslo123";

        // When
        Uzytkownik result = uzytkownikDAO.zaloguj(maliciousLogin, normalPassword);

        // Then
        assertThat(result).isNull();

        // Verify table still exists
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM UZYTKOWNIK");
            rs.next();
            assertThat(rs.getInt(1)).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("Powinien obsłużyć specjalne znaki w loginie i haśle")
    void shouldHandleSpecialCharacters() throws SQLException {
        // Given - dodaj użytkownika ze specjalnymi znakami
        String insertSQL = "INSERT INTO UZYTKOWNIK (imie, nazwisko, login, haslo, rola) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setString(1, "Test");
            stmt.setString(2, "Special");
            stmt.setString(3, "test@domain.com");
            stmt.setString(4, "p@ssw0rd!");
            stmt.setString(5, "KLIENT");
            stmt.executeUpdate();
        }

        // When
        Uzytkownik result = uzytkownikDAO.zaloguj("test@domain.com", "p@ssw0rd!");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo("test@domain.com");
    }

    @Test
    @DisplayName("Powinien zwrócić poprawne ID użytkownika")
    void shouldReturnCorrectUserId() throws SQLException {
        // Given
        String login = "testuser";
        String haslo = "testpass";

        // When
        Uzytkownik result = uzytkownikDAO.zaloguj(login, haslo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isPositive();

        // Verify ID is consistent
        Uzytkownik secondResult = uzytkownikDAO.zaloguj(login, haslo);
        assertThat(secondResult.getId()).isEqualTo(result.getId());
    }

    @Test
    @DisplayName("Powinien obsłużyć błąd bazy danych")
    void shouldHandleDatabaseError() throws SQLException {
        // Given - zamknij połączenie aby spowodować błąd
        connection.close();

        // When & Then
        assertThrows(SQLException.class, () -> {
            uzytkownikDAO.zaloguj("jkowalski", "haslo123");
        });
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkie pola użytkownika")
    void shouldReturnAllUserFields() throws SQLException {
        // Given
        String login = "mdabrowska";
        String haslo = "admin123";

        // When
        Uzytkownik result = uzytkownikDAO.zaloguj(login, haslo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getImie()).isNotEmpty();
        assertThat(result.getNazwisko()).isNotEmpty();
        assertThat(result.getLogin()).isNotEmpty();
        assertThat(result.getHaslo()).isNotEmpty();
        assertThat(result.getRola()).isNotEmpty();

        // Verify specific values
        assertThat(result.getImie()).isEqualTo("Maria");
        assertThat(result.getNazwisko()).isEqualTo("Dąbrowska");
        assertThat(result.getLogin()).isEqualTo("mdabrowska");
        assertThat(result.getHaslo()).isEqualTo("admin123");
        assertThat(result.getRola()).isEqualTo("LOGISTYK");
    }
}