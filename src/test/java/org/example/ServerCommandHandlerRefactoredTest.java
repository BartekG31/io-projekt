package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

class ServerCommandHandlerRefactoredTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        // Połączenie z testowym użytkownikiem Oracle
        connection = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe",
                "c##testuser",
                "testpass123"
        );
        connection.setAutoCommit(false);

        // Setup środowiska testowego
        DatabaseTestHelper.setupTestEnvironment(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            DatabaseTestHelper.cleanupTestEnvironment(connection);
            connection.close();
        }
    }

    @Test
    void handleLogin_ShouldReturnSuccess_WhenValidCredentials() throws Exception {
        // Given - wstaw testowego użytkownika
        DatabaseTestHelper.insertTestUser(connection, "Jan", "Kowalski", "testuser", "password", "KLIENT");
        connection.commit();

        // When
        String result = DatabaseTestHelper.testHandleLogin(connection, "testuser", "password");

        // Then
        assertThat(result).startsWith("OK");
        assertThat(result).contains("Jan");
        assertThat(result).contains("Kowalski");
        assertThat(result).contains("KLIENT");
    }

    @Test
    void handleLogin_ShouldReturnError_WhenInvalidCredentials() throws Exception {
        // Given - baza bez użytkownika

        // When
        String result = DatabaseTestHelper.testHandleLogin(connection, "nonexistent", "wrongpass");

        // Then
        assertThat(result).isEqualTo("ERROR;Niepoprawny login lub hasło");
    }

    @ParameterizedTest
    @CsvSource({
            "Gotowe do wysyłki, OK;Paczka jest gotowa do wysyłki",
            "W drodze, OK;Paczka jest w drodze",
            "Oczekiwanie na odbiór, OK;Paczka oczekuje na odbiór",
            "Zrealizowane, OK;Przesyłka została zrealizowana"
    })
    void zmienStatus_ShouldReturnCorrectMessage_ForValidStatusChanges(
            String toStatus, String expectedPrefix) throws Exception {

        // Given - wstaw testowe zlecenie
        DatabaseTestHelper.insertSimpleZlecenie(connection, 1, "Test Odbiorca", "Nowe");
        connection.commit();

        String zlecenieId = DatabaseTestHelper.getLastZlecenieId(connection, "Test Odbiorca");

        // When
        String result = DatabaseTestHelper.testZmienStatus(connection, zlecenieId, toStatus);

        // Then
        assertThat(result).startsWith(expectedPrefix);
    }

    @Test
    void zmienStatus_ShouldReturnError_WhenZlecenieNotExists() throws Exception {
        // When
        String result = DatabaseTestHelper.testZmienStatus(connection, "999", "W drodze");

        // Then
        assertThat(result).startsWith("ERROR");
        assertThat(result).contains("Nie znaleziono zlecenia");
    }

    @Test
    void shouldTestCompleteWorkflow() throws Exception {
        // Given - użyj helper do stworzenia danych
        DatabaseTestHelper.createSampleData(connection);

        // When & Then - test logowania
        String loginResult = DatabaseTestHelper.testHandleLogin(connection, "jkowalski", "pass123");
        assertThat(loginResult).startsWith("OK");
        assertThat(loginResult).contains("Jan");
        assertThat(loginResult).contains("KLIENT");

        // When & Then - test liczby użytkowników
        int userCount = DatabaseTestHelper.getUserCount(connection);
        assertThat(userCount).isEqualTo(3);

        // When & Then - test użytkowników po roli
        int klientCount = DatabaseTestHelper.getUserCountByRole(connection, "KLIENT");
        assertThat(klientCount).isEqualTo(1);

        int kurierCount = DatabaseTestHelper.getUserCountByRole(connection, "KURIER");
        assertThat(kurierCount).isEqualTo(1);

        // When & Then - test pojazdów
        int pojazdyCount = DatabaseTestHelper.getPojazdyCount(connection);
        assertThat(pojazdyCount).isEqualTo(2);

        // When & Then - test zleceń
        int zleceniaCount = DatabaseTestHelper.getZleceniaCount(connection);
        assertThat(zleceniaCount).isEqualTo(2);

        int noweZlecenia = DatabaseTestHelper.getZleceniaCountByStatus(connection, "Nowe");
        assertThat(noweZlecenia).isGreaterThan(0);
    }

    @Test
    void shouldValidateUserExistence() throws Exception {
        // Given
        DatabaseTestHelper.insertTestUser(connection, "Anna", "Test", "anna.test", "pass", "ADMIN");
        connection.commit();

        // When & Then
        boolean exists = DatabaseTestHelper.userExists(connection, "anna.test");
        assertThat(exists).isTrue();

        boolean notExists = DatabaseTestHelper.userExists(connection, "nonexistent.user");
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCheckDatabaseConnection() throws Exception {
        // When
        String currentUser = DatabaseTestHelper.getCurrentUser(connection);

        // Then
        assertThat(currentUser).isEqualTo("C##TESTUSER");
    }

    @Test
    void shouldHandleMultipleStatusChanges() throws Exception {
        // Given - przygotuj zlecenie
        DatabaseTestHelper.insertTestUser(connection, "Test", "User", "testuser", "pass", "KLIENT");
        DatabaseTestHelper.insertTestZlecenie(connection, 1, "Jan Kowalski", "ul. Główna 1",
                "Warszawa", "00-001", "Paczka", 1.5, "Nowe");
        connection.commit();

        String zlecenieId = DatabaseTestHelper.getLastZlecenieId(connection, "Jan Kowalski");

        // When & Then - kolejne zmiany statusu
        String result1 = DatabaseTestHelper.testZmienStatus(connection, zlecenieId, "Przyjęte");
        assertThat(result1).startsWith("OK;Towar został przyjęty");

        String result2 = DatabaseTestHelper.testZmienStatus(connection, zlecenieId, "Gotowe do wysyłki");
        assertThat(result2).startsWith("OK;Paczka jest gotowa");

        String result3 = DatabaseTestHelper.testZmienStatus(connection, zlecenieId, "W drodze");
        assertThat(result3).startsWith("OK;Paczka jest w drodze");

        String result4 = DatabaseTestHelper.testZmienStatus(connection, zlecenieId, "Zrealizowane");
        assertThat(result4).startsWith("OK;Przesyłka została zrealizowana");

        // Sprawdź końcowy status
        int zrealizowaneCount = DatabaseTestHelper.getZleceniaCountByStatus(connection, "Zrealizowane");
        assertThat(zrealizowaneCount).isEqualTo(1);
    }
}