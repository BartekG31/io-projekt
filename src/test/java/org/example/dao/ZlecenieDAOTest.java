package org.example.dao;

import org.example.model.Zlecenie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class ZlecenieDAOTest {

    private Connection connection;
    private ZlecenieDAO zlecenieDAO;
    private String dbName;

    @BeforeEach
    void setUp() throws SQLException {
        // Unikalna nazwa bazy dla każdego testu
        dbName = "test_" + UUID.randomUUID().toString().replace("-", "");

        connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1", "sa", "");
        zlecenieDAO = new ZlecenieDAO(connection);

        createTestTables();
        insertTestData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            // Usuń tabelę i zamknij połączenie
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS ZLECENIA");
            }
            connection.close();
        }
    }

    private void createTestTables() throws SQLException {
        String createZleceniaSQL = """
            CREATE TABLE IF NOT EXISTS ZLECENIA (
                id INT PRIMARY KEY AUTO_INCREMENT,
                nadawca_id INT NOT NULL,
                odbiorca_id INT NOT NULL,
                pojazd_id INT,
                status VARCHAR(50) NOT NULL,
                data_utworzenia DATE NOT NULL
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createZleceniaSQL);
        }
    }

    private void insertTestData() throws SQLException {
        String insertSQL = """
            INSERT INTO ZLECENIA (nadawca_id, odbiorca_id, pojazd_id, status, data_utworzenia) VALUES
            (1, 2, 1, 'Nowe', '2024-01-15'),
            (2, 3, 2, 'W drodze', '2024-01-16'),
            (1, 4, NULL, 'Przyjęte', '2024-01-17'),
            (3, 5, 3, 'Zrealizowane', '2024-01-18'),
            (1, 6, NULL, 'Odrzucone', '2024-01-19')
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(insertSQL);
        }
    }

    @Test
    @DisplayName("Powinien znaleźć wszystkie zlecenia")
    void shouldFindAllZlecenia() throws SQLException {
        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzWszystkie();

        // Then
        assertThat(zlecenia).hasSize(5);
        assertThat(zlecenia)
                .extracting(Zlecenie::getStatus)
                .containsExactlyInAnyOrder("Nowe", "W drodze", "Przyjęte", "Zrealizowane", "Odrzucone");
    }

    @Test
    @DisplayName("Powinien znaleźć zlecenia dla konkretnego klienta")
    void shouldFindZleceniaForSpecificClient() throws SQLException {
        // Given
        int klientId = 1;

        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzDlaKlienta(klientId);

        // Then
        assertThat(zlecenia).hasSize(3);
        assertThat(zlecenia)
                .allMatch(z -> z.getNadawcaId() == klientId);
        assertThat(zlecenia)
                .extracting(Zlecenie::getStatus)
                .containsExactlyInAnyOrder("Nowe", "Przyjęte", "Odrzucone");
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę dla nieistniejącego klienta")
    void shouldReturnEmptyListForNonExistentClient() throws SQLException {
        // Given
        int nieistniejacyKlientId = 999;

        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzDlaKlienta(nieistniejacyKlientId);

        // Then
        assertThat(zlecenia).isEmpty();
    }

    @Test
    @DisplayName("Powinien poprawnie mapować wszystkie pola zlecenia")
    void shouldCorrectlyMapAllZlecenieFields() throws SQLException {
        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzWszystkie();

        // Then
        Zlecenie pierwszeZlecenie = zlecenia.get(0);
        assertThat(pierwszeZlecenie.getId()).isPositive();
        assertThat(pierwszeZlecenie.getNadawcaId()).isPositive();
        assertThat(pierwszeZlecenie.getOdbiorcaId()).isPositive();
        assertThat(pierwszeZlecenie.getStatus()).isNotEmpty();
        assertThat(pierwszeZlecenie.getDataUtworzenia()).isNotNull();
    }

    @Test
    @DisplayName("Powinien obsłużyć zlecenia z NULL pojazd_id")
    void shouldHandleZleceniaWithNullPojazdId() throws SQLException {
        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzWszystkie();

        // Then
        assertThat(zlecenia)
                .anyMatch(z -> z.getPojazdId() == 0); // W Javie NULL z bazy staje się 0 dla int
    }

    @Test
    @DisplayName("Powinien zwrócić zlecenia w odpowiedniej kolejności")
    void shouldReturnZleceniaInCorrectOrder() throws SQLException {
        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzDlaKlienta(1);

        // Then
        assertThat(zlecenia).hasSize(3);
        // Sprawdź czy zlecenia są posortowane według ID (domyślnie rosnąco)
        for (int i = 1; i < zlecenia.size(); i++) {
            assertThat(zlecenia.get(i).getId()).isGreaterThan(zlecenia.get(i-1).getId());
        }
    }

    @Test
    @DisplayName("Powinien obsłużyć pustą tabelę")
    void shouldHandleEmptyTable() throws SQLException {
        // Given - wyczyść tabelę
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM ZLECENIA");
        }

        // When
        List<Zlecenie> wszystkieZlecenia = zlecenieDAO.znajdzWszystkie();
        List<Zlecenie> zleceniaKlienta = zlecenieDAO.znajdzDlaKlienta(1);

        // Then
        assertThat(wszystkieZlecenia).isEmpty();
        assertThat(zleceniaKlienta).isEmpty();
    }

    @Test
    @DisplayName("Powinien prawidłowo obsłużyć różne statusy zleceń")
    void shouldCorrectlyHandleDifferentZlecenieStatuses() throws SQLException {
        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzWszystkie();

        // Then
        assertThat(zlecenia)
                .extracting(Zlecenie::getStatus)
                .contains("Nowe", "W drodze", "Przyjęte", "Zrealizowane", "Odrzucone");
    }

    @Test
    @DisplayName("Powinien obsłużyć błąd połączenia z bazą")
    void shouldHandleDatabaseConnectionError() throws SQLException {
        // Given - zamknij połączenie
        connection.close();

        // When & Then
        assertThatThrownBy(() -> zlecenieDAO.znajdzWszystkie())
                .isInstanceOf(SQLException.class);

        assertThatThrownBy(() -> zlecenieDAO.znajdzDlaKlienta(1))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("Powinien poprawnie obsłużyć duże ID klienta")
    void shouldHandleLargeClientId() throws SQLException {
        // Given
        int duzeId = Integer.MAX_VALUE;

        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzDlaKlienta(duzeId);

        // Then
        assertThat(zlecenia).isEmpty();
    }

    @Test
    @DisplayName("Powinien obsłużyć ujemne ID klienta")
    void shouldHandleNegativeClientId() throws SQLException {
        // Given
        int ujemneId = -1;

        // When
        List<Zlecenie> zlecenia = zlecenieDAO.znajdzDlaKlienta(ujemneId);

        // Then
        assertThat(zlecenia).isEmpty();
    }

    @Test
    @DisplayName("Powinien zwrócić kopię obiektów Zlecenie")
    void shouldReturnCopyOfZlecenieObjects() throws SQLException {
        // When
        List<Zlecenie> pierwszeWyniki = zlecenieDAO.znajdzWszystkie();
        List<Zlecenie> drugieWyniki = zlecenieDAO.znajdzWszystkie();

        // Then
        assertThat(pierwszeWyniki).isNotSameAs(drugieWyniki);
        assertThat(pierwszeWyniki).hasSize(drugieWyniki.size());

        for (int i = 0; i < pierwszeWyniki.size(); i++) {
            Zlecenie pierwsze = pierwszeWyniki.get(i);
            Zlecenie drugie = drugieWyniki.get(i);

            assertThat(pierwsze).isNotSameAs(drugie);
            assertThat(pierwsze.getId()).isEqualTo(drugie.getId());
            assertThat(pierwsze.getStatus()).isEqualTo(drugie.getStatus());
        }
    }
}