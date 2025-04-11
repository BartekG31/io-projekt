package org.example.dao;

import org.example.model.Uzytkownik;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UzytkownikDAO {
    private Connection conn;

    public UzytkownikDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Uzytkownik> znajdzWszystkich() throws SQLException {
        List<Uzytkownik> lista = new ArrayList<>();
        String sql = "SELECT * FROM UZYTKOWNICY";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Uzytkownik u = new Uzytkownik();
                u.setId(rs.getInt("id"));
                u.setLogin(rs.getString("login"));
                u.setHaslo(rs.getString("haslo"));
                u.setImie(rs.getString("imie"));
                u.setNazwisko(rs.getString("nazwisko"));
                u.setEmail(rs.getString("email"));
                u.setRolaId(rs.getInt("rola_id"));
                lista.add(u);
            }
        }

        return lista;
    }

    public Uzytkownik znajdzPoLoginie(String login) throws SQLException {
        String sql = "SELECT * FROM UZYTKOWNICY WHERE login = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Uzytkownik(
                        rs.getInt("id"),
                        rs.getString("login"),
                        rs.getString("haslo"),
                        rs.getString("imie"),
                        rs.getString("nazwisko"),
                        rs.getString("email"),
                        rs.getInt("rola_id")
                );
            }
        }
        return null;
    }
}
