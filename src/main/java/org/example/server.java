package org.example;

import org.example.dao.UzytkownikDAO;
import org.example.model.Uzytkownik;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class server {

    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer nasłuchuje na porcie " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String command = in.readLine();
                System.out.println("Odebrano komendę: " + command);

                if (command == null) {
                    out.println("ERROR;Brak komendy");
                    continue;
                }

                if (command.startsWith("LOGIN")) {
                    String[] parts = command.split(";");
                    if (parts.length < 3) {
                        out.println("ERROR;Niepoprawny format logowania");
                    } else {
                        String login = parts[1];
                        String haslo = parts[2];
                        out.println(handleLogin(login, haslo));
                    }

                } else if (command.startsWith("DODAJ_ZLECENIE")) {
                    String[] data = command.split(";");
                    if (data.length < 9) {
                        out.println("ERROR;Brakuje danych zlecenia");
                    } else {
                        out.println(dodajZlecenie(data));
                    }

                } else if (command.startsWith("POBIERZ_DO_ODBIORU")) {
                    String[] parts = command.split(";");
                    out.println(pobierzDoOdbioru(parts[1]));

                } else if (command.startsWith("ZATWIERDZ_POJEDYNCZE")) {
                    String[] parts = command.split(";");
                    out.println(zmienStatus(parts[1], "Zrealizowane"));

                } else if (command.startsWith("ODRZUC_POJEDYNCZE")) {
                    String[] parts = command.split(";");
                    out.println(zmienStatus(parts[1], "Odrzucone"));

                } else if (command.startsWith("HISTORIA_ZLECEN")) {
                    String[] parts = command.split(";");
                    if (parts.length < 2) {
                        out.println("ERROR;Brakuje ID użytkownika");
                    } else {
                        out.println(pobierzHistorie(parts[1]));
                    }

                } else if (command.startsWith("ZGLOS_PROBLEM")) {
                    String[] parts = command.split(";", 3);
                    if (parts.length < 3) {
                        out.println("ERROR;Brakuje ID zlecenia lub opisu problemu");
                    } else {
                        out.println(zglosProblem(parts[1], parts[2]));
                    }

                } else if (command.startsWith("ZLECENIA_DO_REKLAMACJI")) {
                    String[] parts = command.split(";");
                    out.println(pobierzZleceniaDoReklamacji(parts[1]));

                } else if (command.startsWith("ZGLOS_REKLAMACJE")) {
                    String[] parts = command.split(";", 4);
                    if (parts.length < 4) {
                        out.println("ERROR;Brakuje danych reklamacji");
                    } else {
                        out.println(zglosReklamacje(parts[1], parts[2]));
                    }

                } else {
                    out.println("ERROR;Nieznana komenda");
                }

                clientSocket.close();
            }

        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
        }
    }

    private static String handleLogin(String login, String haslo) {
        try (Connection conn = getConnection()) {
            UzytkownikDAO dao = new UzytkownikDAO(conn);
            Uzytkownik u = dao.zaloguj(login, haslo);
            if (u != null) {
                return "OK;" + u.getId() + ";" + u.getImie() + ";" + u.getNazwisko() + ";" + u.getRola();
            } else {
                return "ERROR;Niepoprawny login lub hasło";
            }
        } catch (SQLException e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String dodajZlecenie(String[] data) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO ZLECENIA (nadawca_id, odbiorca, adres, miasto, kod_pocztowy, opis, waga, data_nadania, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Nowe')"
            );
            stmt.setInt(1, Integer.parseInt(data[1]));
            stmt.setString(2, data[2]);
            stmt.setString(3, data[3]);
            stmt.setString(4, data[4]);
            stmt.setString(5, data[5]);
            stmt.setString(6, data[6]);
            stmt.setDouble(7, Double.parseDouble(data[7]));
            stmt.setDate(8, Date.valueOf(data[8]));

            stmt.executeUpdate();
            return "OK;Zlecenie zostało zapisane";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzDoOdbioru(String odbiorca) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, z.opis, z.waga, z.data_nadania, u.imie, u.nazwisko " +
                            "FROM ZLECENIA z " +
                            "JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                            "WHERE z.odbiorca = ? AND z.status = 'Nowe'"
            );
            stmt.setString(1, odbiorca);
            ResultSet rs = stmt.executeQuery();

            StringBuilder response = new StringBuilder("OK");
            while (rs.next()) {
                response.append(";")
                        .append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania")).append("|")
                        .append(rs.getString("imie")).append(" ").append(rs.getString("nazwisko"));
            }

            return response.length() == 2 ? "ERROR;Brak zleceń" : response.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String zmienStatus(String idZlecenia, String nowyStatus) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE ZLECENIA SET status = ? WHERE id_zlecenia = ?"
            );
            stmt.setString(1, nowyStatus);
            stmt.setInt(2, Integer.parseInt(idZlecenia));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return "OK;Zmieniono status na " + nowyStatus;
            } else {
                return "ERROR;Nie znaleziono zlecenia o podanym ID";
            }
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzHistorie(String nadawcaId) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT odbiorca, opis, status, data_nadania FROM ZLECENIA WHERE nadawca_id = ? ORDER BY data_nadania DESC"
            );
            stmt.setInt(1, Integer.parseInt(nadawcaId));
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getString("status")).append("|")
                        .append(rs.getDate("data_nadania"));
            }

            return sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String zglosProblem(String zlecenieId, String opis) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO PROBLEMY (ID_ZLECENIA, OPIS_PROBLEMU) VALUES (?, ?)"
            );
            stmt.setInt(1, Integer.parseInt(zlecenieId));
            stmt.setString(2, opis);
            stmt.executeUpdate();

            return "OK;Zgłoszenie zostało zapisane";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzZleceniaDoReklamacji(String odbiorca) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id_zlecenia, opis FROM ZLECENIA " +
                            "WHERE odbiorca = ? AND status = 'Zrealizowane' " +
                            "AND id_zlecenia NOT IN (SELECT id_zlecenia FROM REKLAMACJE)"
            );
            stmt.setString(1, odbiorca);
            ResultSet rs = stmt.executeQuery();

            StringBuilder response = new StringBuilder("OK");
            while (rs.next()) {
                response.append(";")
                        .append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("opis"));
            }

            return response.length() == 2 ? "ERROR;Brak dostępnych zleceń" : response.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String zglosReklamacje(String zlecenieId, String opisReklamacji) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO REKLAMACJE (ID_ZLECENIA, OPIS_REKLAMACJI) VALUES (?, ?)"
            );
            stmt.setInt(1, Integer.parseInt(zlecenieId));
            stmt.setString(2, opisReklamacji);
            stmt.executeUpdate();

            return "OK;Reklamacja została zgłoszona";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }


    private static Connection getConnection() throws SQLException {
        String url = "jdbc:oracle:thin:@xxx.xxx.xxx.xxx:1521";
        String username = "SYSTEM";
        String password = "iop123";
        return DriverManager.getConnection(url, username, password);
    }
}
