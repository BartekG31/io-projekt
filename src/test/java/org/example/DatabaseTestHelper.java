package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Pomocnicze metody do testów bazy danych
 */
public class DatabaseTestHelper {

    // === SETUP & CLEANUP ===
    public static void setupTestEnvironment(Connection connection) throws SQLException {
        // Usuń tabele w odpowiedniej kolejności
        for (String tableName : DatabaseQueries.ALL_TABLES) {
            dropTableIfExists(connection, tableName);
        }

        // Stwórz tabele
        connection.createStatement().execute(DatabaseQueries.CREATE_TABLE_UZYTKOWNIK);
        connection.createStatement().execute(DatabaseQueries.CREATE_TABLE_POJAZDY);
        connection.createStatement().execute(DatabaseQueries.CREATE_TABLE_ZLECENIA);
        connection.createStatement().execute(DatabaseQueries.CREATE_TABLE_INCYDENTY);
        connection.createStatement().execute(DatabaseQueries.CREATE_TABLE_RAPORTY_TRAS);

        connection.commit();
    }

    public static void cleanupTestEnvironment(Connection connection) throws SQLException {
        try {
            for (String tableName : DatabaseQueries.ALL_TABLES) {
                dropTableIfExists(connection, tableName);
            }
            connection.commit();
        } catch (Exception e) {
            // Ignoruj błędy czyszczenia
        }
    }

    private static void dropTableIfExists(Connection connection, String tableName) {
        try {
            PreparedStatement checkStmt = connection.prepareStatement(DatabaseQueries.CHECK_TABLE_EXISTS);
            checkStmt.setString(1, tableName);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                connection.createStatement().execute(DatabaseQueries.getDropTableQuery(tableName));
            }

            rs.close();
            checkStmt.close();
        } catch (Exception e) {
            // Ignoruj błędy
        }
    }

    // === INSERT TEST DATA ===
    public static int insertTestUser(Connection connection, String imie, String nazwisko,
                                     String login, String haslo, String rola) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.INSERT_UZYTKOWNIK);
        stmt.setString(1, imie);
        stmt.setString(2, nazwisko);
        stmt.setString(3, login);
        stmt.setString(4, haslo);
        stmt.setString(5, rola);

        int result = stmt.executeUpdate();
        stmt.close();
        return result;
    }

    public static int insertTestPojazd(Connection connection, String marka, String model,
                                       String rejestracja, String status, String uwagi) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.INSERT_POJAZDY);
        stmt.setString(1, marka);
        stmt.setString(2, model);
        stmt.setString(3, rejestracja);
        stmt.setString(4, status);
        stmt.setString(5, uwagi);
        stmt.setObject(6, null); // kierowca_id

        int result = stmt.executeUpdate();
        stmt.close();
        return result;
    }

    public static int insertTestZlecenie(Connection connection, int nadawcaId, String odbiorca,
                                         String adres, String miasto, String kodPocztowy,
                                         String opis, double waga, String status) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.INSERT_ZLECENIA);
        stmt.setInt(1, nadawcaId);
        stmt.setString(2, odbiorca);
        stmt.setString(3, adres);
        stmt.setString(4, miasto);
        stmt.setString(5, kodPocztowy);
        stmt.setString(6, opis);
        stmt.setDouble(7, waga);
        stmt.setString(8, status);

        int result = stmt.executeUpdate();
        stmt.close();
        return result;
    }

    public static int insertSimpleZlecenie(Connection connection, int nadawcaId,
                                           String odbiorca, String status) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.INSERT_ZLECENIA_SIMPLE);
        stmt.setInt(1, nadawcaId);
        stmt.setString(2, odbiorca);
        stmt.setString(3, status);

        int result = stmt.executeUpdate();
        stmt.close();
        return result;
    }

    // === QUERY HELPERS ===
    public static int getUserCount(Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_USER_COUNT);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    public static int getUserCountByRole(Connection connection, String rola) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_USER_COUNT_BY_ROLE);
        stmt.setString(1, rola);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    public static int getPojazdyCount(Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_POJAZDY_COUNT);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    public static int getZleceniaCount(Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_ZLECENIA_COUNT);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    public static int getZleceniaCountByStatus(Connection connection, String status) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_ZLECENIA_BY_STATUS);
        stmt.setString(1, status);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }

    public static String getLastZlecenieId(Connection connection, String odbiorca) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_LAST_ZLECENIE_ID);
        stmt.setString(1, odbiorca);
        ResultSet rs = stmt.executeQuery();
        String id = null;
        if (rs.next()) {
            id = rs.getString(1);
        }
        rs.close();
        stmt.close();
        return id;
    }

    // === BUSINESS LOGIC HELPERS ===
    public static String testHandleLogin(Connection connection, String login, String haslo) {
        try {
            PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_USER_BY_LOGIN_PASSWORD);
            stmt.setString(1, login);
            stmt.setString(2, haslo);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String imie = rs.getString("imie");
                String nazwisko = rs.getString("nazwisko");
                String rola = rs.getString("rola");

                rs.close();
                stmt.close();
                return String.format("OK;%d;%s;%s;%s", id, imie, nazwisko, rola);
            } else {
                rs.close();
                stmt.close();
                return "ERROR;Niepoprawny login lub hasło";
            }

        } catch (Exception e) {
            return "ERROR;Błąd połączenia z bazą danych: " + e.getMessage();
        }
    }

    public static String testZmienStatus(Connection connection, String idZlecenia, String nowyStatus) {
        try {
            PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.UPDATE_ZLECENIA_STATUS);
            stmt.setString(1, nowyStatus);
            stmt.setString(2, idZlecenia);

            int rowsUpdated = stmt.executeUpdate();
            stmt.close();

            if (rowsUpdated > 0) {
                connection.commit();
                String message = DatabaseQueries.getStatusMessage(nowyStatus);
                return "OK;" + message;
            } else {
                return "ERROR;Nie znaleziono zlecenia o podanym ID";
            }

        } catch (Exception e) {
            return "ERROR;Błąd aktualizacji statusu: " + e.getMessage();
        }
    }

    // === VALIDATION HELPERS ===
    public static boolean userExists(Connection connection, String login) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_USER_BY_LOGIN);
        stmt.setString(1, login);
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        stmt.close();
        return exists;
    }

    public static String getCurrentUser(Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.SELECT_CURRENT_USER);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        String user = rs.getString(1);
        rs.close();
        stmt.close();
        return user;
    }

    // === SAMPLE DATA CREATION ===
    public static void createSampleData(Connection connection) throws SQLException {
        // Dodaje przykładowych użytkowników
        insertTestUser(connection, "Jan", "Kowalski", "jkowalski", "pass123", "KLIENT");
        insertTestUser(connection, "Anna", "Nowak", "anowak", "pass456", "KURIER");
        insertTestUser(connection, "Piotr", "Wiśniewski", "pwisniewski", "pass789", "LOGISTYK");

        // Dodaje przykładowe pojazdy
        insertTestPojazd(connection, "Mercedes", "Sprinter", "WA12345", "Dostepny", "Pojazd w dobrym stanie");
        insertTestPojazd(connection, "Ford", "Transit", "WA67890", "Zajety", "");

        // Dodaje przykładowe zlecenia
        insertSimpleZlecenie(connection, 1, "Test Odbiorca", "Nowe");
        insertTestZlecenie(connection, 1, "Jan Testowy", "ul. Testowa 1", "Warszawa",
                "00-001", "Paczka testowa", 2.5, "Gotowe do wysyłki");

        connection.commit();
    }
}