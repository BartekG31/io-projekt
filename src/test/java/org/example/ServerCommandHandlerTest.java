package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerCommandHandlerTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @Test
    void handleLogin_ShouldReturnSuccess_WhenValidCredentials() throws Exception {
        // Given
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("imie")).thenReturn("Jan");
        when(mockResultSet.getString("nazwisko")).thenReturn("Kowalski");
        when(mockResultSet.getString("rola")).thenReturn("KLIENT");

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // When - używamy reflection do dostępu do private metody
            Method handleLoginMethod = server.class.getDeclaredMethod("handleLogin", String.class, String.class);
            handleLoginMethod.setAccessible(true);
            String result = (String) handleLoginMethod.invoke(null, "testuser", "password");

            // Then
            assertThat(result).startsWith("OK;1;Jan;Kowalski;KLIENT");
        }
    }

    @Test
    void handleLogin_ShouldReturnError_WhenInvalidCredentials() throws Exception {
        // Given
        when(mockResultSet.next()).thenReturn(false);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // When - używamy reflection
            Method handleLoginMethod = server.class.getDeclaredMethod("handleLogin", String.class, String.class);
            handleLoginMethod.setAccessible(true);
            String result = (String) handleLoginMethod.invoke(null, "invaliduser", "wrongpassword");

            // Then
            assertThat(result).isEqualTo("ERROR;Niepoprawny login lub hasło");
        }
    }

    @Test
    void handleLogin_ShouldReturnError_WhenDatabaseException() throws Exception {
        // Given
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("Database error"));

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // When
            Method handleLoginMethod = server.class.getDeclaredMethod("handleLogin", String.class, String.class);
            handleLoginMethod.setAccessible(true);
            String result = (String) handleLoginMethod.invoke(null, "testuser", "password");

            // Then
            assertThat(result).startsWith("ERROR;Błąd połączenia z bazą danych");
        }
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
        // Given
        when(mockStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // When - używamy reflection
            Method zmienStatusMethod = server.class.getDeclaredMethod("zmienStatus", String.class, String.class);
            zmienStatusMethod.setAccessible(true);
            String result = (String) zmienStatusMethod.invoke(null, "1", toStatus);

            // Then
            assertThat(result).startsWith(expectedPrefix);
        }
    }

    @Test
    void zmienStatus_ShouldReturnError_WhenUpdateFails() throws Exception {
        // Given
        when(mockStatement.executeUpdate()).thenReturn(0); // Nie zaktualizowano żadnego rekordu

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // When
            Method zmienStatusMethod = server.class.getDeclaredMethod("zmienStatus", String.class, String.class);
            zmienStatusMethod.setAccessible(true);
            String result = (String) zmienStatusMethod.invoke(null, "999", "W drodze");

            // Then
            assertThat(result).startsWith("ERROR");
        }
    }

    @Test
    void handleLogin_Private_ShouldWork() throws Exception {
        // Given - setup mock data
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(42);
        when(mockResultSet.getString("imie")).thenReturn("Test");
        when(mockResultSet.getString("nazwisko")).thenReturn("User");
        when(mockResultSet.getString("rola")).thenReturn("ADMIN");

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // When - użyj reflection do dostępu do private metody
            Method handleLoginMethod = server.class.getDeclaredMethod("handleLogin", String.class, String.class);
            handleLoginMethod.setAccessible(true);

            String result = (String) handleLoginMethod.invoke(null, "testuser", "password");

            // Then
            assertThat(result).startsWith("OK");
            assertThat(result).contains("42");
            assertThat(result).contains("Test");
            assertThat(result).contains("User");
            assertThat(result).contains("ADMIN");
        }
    }

    @Test
    void zmienStatus_Private_ShouldWork() throws Exception {
        // Given
        when(mockStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // When - użyj reflection
            Method zmienStatusMethod = server.class.getDeclaredMethod("zmienStatus", String.class, String.class);
            zmienStatusMethod.setAccessible(true);

            String result = (String) zmienStatusMethod.invoke(null, "1", "W drodze");

            // Then
            assertThat(result).startsWith("OK");
            assertThat(result).contains("Paczka jest w drodze");
        }
    }
}