package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseTestUtilsTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        // Połączenie z Oracle - UŻYTKOWNIK TESTOWY!
        connection = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe",
                "c##testuser",  // Użytkownik testowy
                "testpass123" // Hasło testowego użytkownika
        );
        connection.setAutoCommit(false); // Używamy transakcji

        // Wyczyść bazę przed każdym testem
        DatabaseTestUtils.cleanupTestDatabase(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            // Wyczyść bazę po każdym teście
            try {
                DatabaseTestUtils.cleanupTestDatabase(connection);
            } catch (Exception e) {
                // Ignoruj błędy czyszczenia
            }
            connection.rollback(); // Cofnij zmiany po teście
            connection.close();
        }
    }

    @Test
    void setupTestDatabase_ShouldCreateTablesSuccessfully() throws Exception {
        // When
        DatabaseTestUtils.setupTestDatabase(connection);

        // Then - sprawdź czy tabela UZYTKOWNIK istnieje
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM USER_TABLES WHERE table_name = 'UZYTKOWNIK'"
        );
        ResultSet rs = stmt.executeQuery();
        rs.next();
        assertThat(rs.getInt(1)).isEqualTo(1);

        // Sprawdź czy tabela POJAZDY istnieje
        PreparedStatement stmt2 = connection.prepareStatement(
                "SELECT COUNT(*) FROM USER_TABLES WHERE table_name = 'POJAZDY'"
        );
        ResultSet rs2 = stmt2.executeQuery();
        rs2.next();
        assertThat(rs2.getInt(1)).isEqualTo(1);

        // Sprawdź czy wszystkie tabele zostały utworzone
        PreparedStatement stmt3 = connection.prepareStatement(
                "SELECT COUNT(*) FROM USER_TABLES WHERE table_name IN ('UZYTKOWNIK', 'POJAZDY', 'ZLECENIA', 'INCYDENTY', 'RAPORTY_TRAS')"
        );
        ResultSet rs3 = stmt3.executeQuery();
        rs3.next();
        assertThat(rs3.getInt(1)).isEqualTo(5);

        rs.close();
        stmt.close();
        rs2.close();
        stmt2.close();
        rs3.close();
        stmt3.close();
    }

    @Test
    void insertTestData_ShouldInsertDataCorrectly() throws Exception {
        // Given
        DatabaseTestUtils.setupTestDatabase(connection);

        // When
        DatabaseTestUtils.insertTestData(connection);

        // Then - sprawdź liczbę użytkowników
        PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM UZYTKOWNIK");
        ResultSet rs = stmt.executeQuery();
        rs.next();
        assertThat(rs.getInt(1)).isEqualTo(3); // Wstawiamy 3 użytkowników

        // Sprawdź liczbę pojazdów
        PreparedStatement stmt2 = connection.prepareStatement("SELECT COUNT(*) FROM POJAZDY");
        ResultSet rs2 = stmt2.executeQuery();
        rs2.next();
        assertThat(rs2.getInt(1)).isEqualTo(2); // Wstawiamy 2 pojazdy

        rs.close();
        stmt.close();
        rs2.close();
        stmt2.close();
    }

    @Test
    void insertTestData_ShouldInsertCorrectUserData() throws Exception {
        // Given
        DatabaseTestUtils.setupTestDatabase(connection);

        // When
        DatabaseTestUtils.insertTestData(connection);

        // Then - sprawdź konkretne dane
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT imie, nazwisko, login, rola FROM UZYTKOWNIK WHERE login = 'jkowalski'"
        );
        ResultSet rs = stmt.executeQuery();

        assertThat(rs.next()).isTrue();
        assertThat(rs.getString("imie")).isEqualTo("Jan");
        assertThat(rs.getString("nazwisko")).isEqualTo("Kowalski");
        assertThat(rs.getString("login")).isEqualTo("jkowalski");
        assertThat(rs.getString("rola")).isEqualTo("KLIENT");

        rs.close();
        stmt.close();
    }

    @Test
    void setupTestDatabase_ShouldWorkMultipleTimes() throws Exception {
        // Given & When - wywołaj setup dwa razy
        DatabaseTestUtils.setupTestDatabase(connection);
        DatabaseTestUtils.setupTestDatabase(connection); // Nie powinno rzucić błędu

        // Then - tabele nadal powinny istnieć
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM USER_TABLES WHERE table_name = 'UZYTKOWNIK'"
        );
        ResultSet rs = stmt.executeQuery();
        rs.next();
        assertThat(rs.getInt(1)).isEqualTo(1);

        rs.close();
        stmt.close();
    }
}