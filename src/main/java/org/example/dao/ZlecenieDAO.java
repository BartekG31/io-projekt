package org.example.dao;

import org.example.model.Zlecenie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ZlecenieDAO {
    private Connection conn;

    public ZlecenieDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Zlecenie> znajdzWszystkie() throws SQLException {
        List<Zlecenie> lista = new ArrayList<>();
        String sql = "SELECT * FROM ZLECENIA";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Zlecenie z = new Zlecenie(
                        rs.getInt("id"),
                        rs.getInt("nadawca_id"),
                        rs.getInt("odbiorca_id"),
                        rs.getInt("pojazd_id"),
                        rs.getString("status"),
                        rs.getDate("data_utworzenia")
                );
                lista.add(z);
            }
        }

        return lista;
    }

    public List<Zlecenie> znajdzDlaKlienta(int klientId) throws SQLException {
        List<Zlecenie> lista = new ArrayList<>();
        String sql = "SELECT * FROM ZLECENIA WHERE nadawca_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, klientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Zlecenie z = new Zlecenie(
                        rs.getInt("id"),
                        rs.getInt("nadawca_id"),
                        rs.getInt("odbiorca_id"),
                        rs.getInt("pojazd_id"),
                        rs.getString("status"),
                        rs.getDate("data_utworzenia")
                );
                lista.add(z);
            }
        }

        return lista;
    }
}
