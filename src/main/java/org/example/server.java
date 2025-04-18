package org.example;

import org.example.dao.UzytkownikDAO;
import org.example.model.Uzytkownik;

import java.io.*;
import java.net.*;
import java.sql.*;

public class server {
    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🔌 Serwer nasłuchuje na porcie " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String command = in.readLine();
                System.out.println("Odebrano: " + command);

                if (command.startsWith("LOGIN")) {
                    String[] parts = command.split(";");
                    String login = parts[1];
                    String haslo = parts[2];
                    out.println(handleLogin(login, haslo));
                } else {
                    out.println("ERROR;Nieznana komenda");
                }

                clientSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String handleLogin(String login, String haslo) {
        try (Connection conn = getConnection()) {
            UzytkownikDAO dao = new UzytkownikDAO(conn);
            System.out.println("🔍 Próba logowania: login=" + login + ", haslo=" + haslo);
            Uzytkownik u = dao.zaloguj(login, haslo);
            if (u != null) {
                return "OK;" + u.getImie() + ";" + u.getRola();
            } else {
                return "ERROR;Niepoprawny login lub hasło";
            }
        } catch (SQLException e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static Connection getConnection() throws SQLException {
        String url = "jdbc:oracle:thin:@192.168.10.40:1522";
        String username = "SYSTEM";
        String password = "iop123";
        return DriverManager.getConnection(url, username, password);
    }
}
