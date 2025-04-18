package org.example.dao;

import org.example.model.Uzytkownik;

import java.sql.*;

public class UzytkownikDAO {
    private final Connection conn;

    public UzytkownikDAO(Connection conn) {
        this.conn = conn;
    }

    public Uzytkownik zaloguj(String login, String haslo) throws SQLException {
        String sql = "SELECT * FROM UZYTKOWNIK WHERE UPPER(login) = UPPER(?) AND UPPER(haslo) = UPPER(?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, login);
        stmt.setString(2, haslo);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Uzytkownik u = new Uzytkownik();
            u.setId(rs.getInt("id"));
            u.setImie(rs.getString("imie"));
            u.setNazwisko(rs.getString("nazwisko"));
            u.setLogin(rs.getString("login"));
            u.setHaslo(rs.getString("haslo"));
            u.setRola(rs.getString("rola"));
            return u;
        }
        return null;
    }
}
