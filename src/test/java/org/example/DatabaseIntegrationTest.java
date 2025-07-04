package org.example;

import org.example.dao.UzytkownikDAO;
import org.example.model.Uzytkownik;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseIntegrationTest {

    private Connection connection;
    private UzytkownikDAO uzytkownikDAO;

    @BeforeEach
    void setUp() throws Exception {
        // Połączenie z Oracle
        connection = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:XE",
                "c##testuser",
                "testpass123"
        );
        connection.setAutoCommit(false);

        // Utwórz tabelę jeśli nie istnieje
        createTableIfNotExists();

        // Usuń stare dane testowe (jeśli są)
        removeTestData();

        // Wstaw dane testowe
        insertTestData();
        connection.commit();

        // Utwórz DAO
        uzytkownikDAO = new UzytkownikDAO(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            try {
                // Usuń dane testowe
                removeTestData();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            } finally {
                connection.close();
            }
        }
    }

    private void createTableIfNotExists() throws SQLException {
        try {
            // Sprawdź czy tabela istnieje
            PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'UZYTKOWNIK'"
            );
            java.sql.ResultSet rs = checkStmt.executeQuery();
            rs.next();
            boolean tableExists = rs.getInt(1) > 0;
            rs.close();
            checkStmt.close();

            if (!tableExists) {
                // Utwórz tabelę
                connection.createStatement().execute(
                        "CREATE TABLE UZYTKOWNIK (" +
                                "id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                                "imie VARCHAR2(50), " +
                                "nazwisko VARCHAR2(50), " +
                                "login VARCHAR2(50) UNIQUE, " +
                                "haslo VARCHAR2(100), " +
                                "rola VARCHAR2(20)" +
                                ")"
                );
                System.out.println("✓ Tabela UZYTKOWNIK utworzona automatycznie");
            } else {
                System.out.println("✓ Tabela UZYTKOWNIK już istnieje");
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas tworzenia tabeli: " + e.getMessage());
            throw e;
        }
    }

    private void removeTestData() throws SQLException {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM UZYTKOWNIK WHERE login = ?"
            );
            stmt.setString(1, "test_jkowalski");
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            // Tabela może być pusta - ignoruj błąd
        }
    }

    private void insertTestData() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO UZYTKOWNIK (imie, nazwisko, login, haslo, rola) VALUES (?, ?, ?, ?, ?)"
        );

        // Użytkownik testowy
        stmt.setString(1, "Jan");
        stmt.setString(2, "Kowalski");
        stmt.setString(3, "test_jkowalski");
        stmt.setString(4, "password123");
        stmt.setString(5, "KLIENT");
        stmt.executeUpdate();

        stmt.close();
    }

    @Test
    @DisplayName("Powinien zalogować użytkownika z poprawnymi danymi")
    void shouldLoginWithValidCredentials() throws SQLException {
        // When
        Uzytkownik result = uzytkownikDAO.zaloguj("test_jkowalski", "password123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImie()).isEqualTo("Jan");
        assertThat(result.getNazwisko()).isEqualTo("Kowalski");
        assertThat(result.getLogin()).isEqualTo("test_jkowalski");
        assertThat(result.getRola()).isEqualTo("KLIENT");
    }

    @Test
    @DisplayName("Powinien zwrócić null dla niepoprawnych danych")
    void shouldReturnNullForInvalidCredentials() throws SQLException {
        // When
        Uzytkownik result = uzytkownikDAO.zaloguj("test_jkowalski", "wrongpassword");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Powinien zwrócić null dla nieistniejącego użytkownika")
    void shouldReturnNullForNonExistentUser() throws SQLException {
        // When
        Uzytkownik result = uzytkownikDAO.zaloguj("nonexistent", "password");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Powinien obsłużyć puste wartości")
    void shouldHandleEmptyValues() throws SQLException {
        // When & Then
        assertThat(uzytkownikDAO.zaloguj("", "password123")).isNull();
        assertThat(uzytkownikDAO.zaloguj("test_jkowalski", "")).isNull();
    }

    @Test
    @DisplayName("Powinien obsłużyć null values")
    void shouldHandleNullValues() throws SQLException {
        // When & Then
        assertThat(uzytkownikDAO.zaloguj(null, "password123")).isNull();
        assertThat(uzytkownikDAO.zaloguj("test_jkowalski", null)).isNull();
    }
}