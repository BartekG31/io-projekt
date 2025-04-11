package org.example;

import org.example.dao.UzytkownikDAO;
import org.example.dao.ZlecenieDAO;
import org.example.model.Uzytkownik;
import org.example.model.Zlecenie;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@192.168.0.247:1521";
        String username = "SYSTEM";
        String password = "iop123";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("Połączenie z Oracle działa!\n");

            UzytkownikDAO uzytkownikDAO = new UzytkownikDAO(conn);
            List<Uzytkownik> uzytkownicy = uzytkownikDAO.znajdzWszystkich();

            System.out.println("Lista użytkowników:");
            for (Uzytkownik u : uzytkownicy) {
                System.out.println("🔸 " + u.getId() + ": " + u.getImie() + " " + u.getNazwisko() + " (" + u.getLogin() + ")");
            }

            ZlecenieDAO zlecenieDAO = new ZlecenieDAO(conn);
            List<Zlecenie> zlecenia = zlecenieDAO.znajdzWszystkie();

            System.out.println("\nLista zleceń:");
            for (Zlecenie z : zlecenia) {
                System.out.println("Zlecenie #" + z.getId()
                        + " | Nadawca ID: " + z.getNadawcaId()
                        + " | Odbiorca ID: " + z.getOdbiorcaId()
                        + " | Pojazd ID: " + z.getPojazdId()
                        + " | Status: " + z.getStatus()
                        + " | Data: " + z.getDataUtworzenia());
            }

        } catch (SQLException e) {
            System.out.println("Błąd połączenia: " + e.getMessage());
        }
    }
}
