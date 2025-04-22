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
                    if (data.length < 8) {
                        out.println("ERROR;Brakuje danych zlecenia");
                    } else {
                        out.println(dodajZlecenie(data));
                    }

                } else if (command.startsWith("ZATWIERDZ_ODBIOR")) {
                    String[] parts = command.split(";");
                    if (parts.length < 2) {
                        out.println("ERROR;Brakuje nazwiska odbiorcy");
                    } else {
                        out.println(zatwierdzOdbior(parts[1]));
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
                return "OK;" + u.getImie() + ";" + u.getNazwisko() + ";" + u.getRola();
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
                    "INSERT INTO ZLECENIA (odbiorca, adres, miasto, kod_pocztowy, opis, waga, data_nadania, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 'Nowe')"
            );
            stmt.setString(1, data[1]);
            stmt.setString(2, data[2]);
            stmt.setString(3, data[3]);
            stmt.setString(4, data[4]);
            stmt.setString(5, data[5]);
            stmt.setDouble(6, Double.parseDouble(data[6]));
            stmt.setDate(7, Date.valueOf(data[7]));
            stmt.executeUpdate();

            return "OK;Zlecenie zostało zapisane";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String zatwierdzOdbior(String odbiorca) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE ZLECENIA SET status = 'Zrealizowane' WHERE odbiorca = ? AND status = 'Nowe'"
            );
            stmt.setString(1, odbiorca);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                return "OK;Zatwierdzono " + rows + " zleceń dla " + odbiorca + ".";
            } else {
                return "ERROR;Brak zleceń do odbioru dla podanej osoby.";
            }
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
