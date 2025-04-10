package org.example;

import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@192.168.10.40:1522/XEPDB1";
        String username = "SYSTEM";
        String password = "iop123";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println(" Połączenie z Oracle działa!");

            Statement stmt = conn.createStatement();

        } catch (SQLException e) {
            System.out.println(" Błąd połączenia: " + e.getMessage());
        }
    }
}
