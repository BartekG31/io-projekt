package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);

            System.out.println("LOGOWANIE DO SYSTEMU");
            System.out.print("Login: ");
            String login = scanner.nextLine();
            System.out.print("Hasło: ");
            String haslo = scanner.nextLine();

            out.println("LOGIN;" + login + ";" + haslo);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                String imie = parts[1];
                String rola = parts[2];
                System.out.println("Zalogowano jako " + imie + " [" + rola + "]");
            } else {
                System.out.println("Logowanie nieudane: " + response.split(";")[1]);
            }

        } catch (IOException e) {
            System.out.println("Błąd klienta: " + e.getMessage());
        }
    }
}
