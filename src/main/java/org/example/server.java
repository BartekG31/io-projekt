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
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String command = in.readLine();
                if (command == null) {
                    out.println("ERROR;Brak komendy");
                    continue;
                }

                if (command.startsWith("LOGIN")) {
                    String[] parts = command.split(";");
                    out.println(handleLogin(parts[1], parts[2]));

                } else if (command.startsWith("DODAJ_ZLECENIE")) {
                    out.println(dodajZlecenie(command.split(";")));

                } else if (command.startsWith("POBIERZ_DO_ODBIORU")) {
                    out.println(pobierzDoOdbioru(command.split(";")[1]));

                } else if (command.startsWith("ZATWIERDZ_POJEDYNCZE")) {
                    out.println(zmienStatus(command.split(";")[1], "Zrealizowane"));

                } else if (command.startsWith("ODRZUC_POJEDYNCZE")) {
                    out.println(zmienStatus(command.split(";")[1], "Odrzucone"));

                } else if (command.startsWith("HISTORIA_ZLECEN")) {
                    out.println(pobierzHistorie(command.split(";")[1]));

                } else if (command.startsWith("ZGLOS_PROBLEM")) {
                    String[] parts = command.split(";", 3);
                    out.println(zglosProblem(parts[1], parts[2]));

                } else if (command.startsWith("POBIERZ_ZREALIZOWANE_DO_PROBLEMU")) {
                    out.println(pobierzZrealizowaneDoProblemu(command.split(";")[1]));

                } else if (command.startsWith("ZLECENIA_DO_REKLAMACJI")) {
                    out.println(pobierzZleceniaDoReklamacji(command.split(";")[1]));

                } else if (command.startsWith("ZGLOS_REKLAMACJE")) {
                    String[] parts = command.split(";", 4);
                    out.println(zglosReklamacje(parts[1], parts[3]));

                } else if (command.startsWith("AKTUALIZUJ_DANE")) {
                    out.println(aktualizujDane(command.split(";", 6)));

                } else if (command.startsWith("POBIERZ_UZYTKOWNIKA")) {
                    out.println(pobierzUzytkownika(command.split(";")[1]));

                } else if (command.startsWith("POBIERZ_PACZKI_NOWE")) {
                    out.println(pobierzNowePaczki());

                } else if (command.startsWith("OZNACZ_GOTOWE")) {
                    out.println(zmienStatus(command.split(";")[1], "Gotowe do wysyłki"));

                } else if (command.startsWith("POBIERZ_DO_PRZYJECIA")) {
                    out.println(pobierzDoPrzyjecia());

                } else if (command.startsWith("PRZYJMIJ_ZLECENIE")) {
                    out.println(zmienStatus(command.split(";")[1], "Przyjęte"));

                }else if (command.startsWith("POBIERZ_INWENTARYZACJE")) {
                    out.println(pobierzInwentaryzacje());


                } else if (command.startsWith("POBIERZ_GOTOWE_DLA_KURIERA")) {
                    out.println(pobierzGotoweDlaKuriera());

                } else if (command.startsWith("ODEBRANA_PRZEZ_KURIERA")) {
                    out.println(zmienStatus(command.split(";")[1], "W drodze"));
                } else if (command.startsWith("ODEBRANA_PRZEZ_KURIERA")) {
                    out.println(zmienStatus(command.split(";")[1], "W drodze"));

                } else if (command.startsWith("POBIERZ_W_DRODZE")) {
                    out.println(pobierzWDrodze());

                } else if (command.startsWith("ZATWIERDZ_DOSTARCZENIE")) {
                    out.println(zmienStatus(command.split(";")[1], "Oczekiwanie na odbiór"));

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
            return u != null
                    ? "OK;" + u.getId() + ";" + u.getImie() + ";" + u.getNazwisko() + ";" + u.getRola()
                    : "ERROR;Niepoprawny login lub hasło";
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

    private static String pobierzDoPrzyjecia() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, u.imie || ' ' || u.nazwisko AS nadawca, z.odbiorca, z.opis, z.waga, z.data_nadania " +
                            "FROM ZLECENIA z JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                            "WHERE z.status = 'Nowe'"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");

            while (rs.next()) {
                sb.append(";")
                        .append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("nadawca")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania"));
            }

            return sb.length() == 2 ? "ERROR;Brak zleceń do przyjęcia" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzInwentaryzacje() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id_zlecenia, odbiorca, opis, waga, data_nadania FROM ZLECENIA WHERE status = 'Przyjęte'"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania"));
            }
            return sb.length() == 2 ? "ERROR;Brak przyjętych paczek" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }


    private static String pobierzDoOdbioru(String odbiorca) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, z.opis, z.waga, z.data_nadania, u.imie, u.nazwisko " +
                            "FROM ZLECENIA z JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                            "WHERE z.odbiorca = ? AND z.status = 'Oczekiwanie na odbiór'"
            );
            stmt.setString(1, odbiorca);
            ResultSet rs = stmt.executeQuery();
            StringBuilder response = new StringBuilder("OK");
            while (rs.next()) {
                response.append(";").append(rs.getInt("id_zlecenia")).append("|")
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

    private static String pobierzNowePaczki() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id_zlecenia, odbiorca, opis, waga, data_nadania FROM ZLECENIA WHERE status = 'Przyjęte'"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania"));
            }
            return sb.length() == 2 ? "ERROR;Brak paczek do przygotowania" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }


    private static String zmienStatus(String idZlecenia, String nowyStatus) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE ZLECENIA SET status = ? WHERE id_zlecenia = ?");
            stmt.setString(1, nowyStatus);
            stmt.setInt(2, Integer.parseInt(idZlecenia));
            return stmt.executeUpdate() > 0
                    ? "OK;Przesyłka została odebrana."
                    : "ERROR;Nie znaleziono zlecenia";
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
                sb.append(";").append(rs.getString("odbiorca")).append("|")
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

    private static String pobierzZrealizowaneDoProblemu(String odbiorca) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, z.opis, z.waga, z.data_nadania, u.imie || ' ' || u.nazwisko AS nadawca " +
                            "FROM ZLECENIA z JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                            "WHERE z.odbiorca = ? AND z.status = 'Zrealizowane' " +
                            "AND z.id_zlecenia NOT IN (SELECT id_zlecenia FROM PROBLEMY)"
            );
            stmt.setString(1, odbiorca);
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania")).append("|")
                        .append(rs.getString("nadawca"));
            }
            return sb.length() == 2 ? "ERROR;Brak przesyłek do zgłoszenia problemu" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzZleceniaDoReklamacji(String odbiorca) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, z.opis FROM ZLECENIA z " +
                            "WHERE z.odbiorca = ? AND z.status = 'Zrealizowane' " +
                            "AND z.id_zlecenia IN (SELECT id_zlecenia FROM PROBLEMY) " +
                            "AND z.id_zlecenia NOT IN (SELECT id_zlecenia FROM REKLAMACJE)"
            );
            stmt.setString(1, odbiorca);
            ResultSet rs = stmt.executeQuery();
            StringBuilder response = new StringBuilder("OK");
            while (rs.next()) {
                response.append(";").append(rs.getInt("id_zlecenia")).append("|")
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

    private static String aktualizujDane(String[] parts) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE UZYTKOWNIK SET imie = ?, nazwisko = ?, login = ?, haslo = ? WHERE id = ?"
            );
            stmt.setString(1, parts[2]);
            stmt.setString(2, parts[3]);
            stmt.setString(3, parts[4]);
            stmt.setString(4, parts[5]);
            stmt.setInt(5, Integer.parseInt(parts[1]));
            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Dane zaktualizowane" : "ERROR;Nie znaleziono użytkownika";
        } catch (SQLIntegrityConstraintViolationException e) {
            return "ERROR;Ten login jest już zajęty.";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzUzytkownika(String id) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT imie, nazwisko, login, haslo FROM UZYTKOWNIK WHERE id = ?"
            );
            stmt.setInt(1, Integer.parseInt(id));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "OK;" + rs.getString("imie") + ";" + rs.getString("nazwisko") + ";" +
                        rs.getString("login") + ";" + rs.getString("haslo");
            } else {
                return "ERROR;Nie znaleziono użytkownika";
            }
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }


    private static String pobierzGotoweDlaKuriera() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id_zlecenia, odbiorca, opis, waga, data_nadania FROM ZLECENIA WHERE status = 'Gotowe do wysyłki'"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";")
                        .append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania"));
            }
            return sb.length() == 2 ? "ERROR;Brak paczek gotowych do odbioru" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzWDrodze() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id_zlecenia, odbiorca, opis, waga, data_nadania FROM ZLECENIA WHERE status = 'W drodze'"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";")
                        .append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania"));
            }
            return sb.length() == 2 ? "ERROR;Brak paczek w drodze" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }


    private static Connection getConnection() throws SQLException {
        String url = "jdbc:oracle:thin:@192.168.0.17:1521";
        String username = "SYSTEM";
        String password = "iop123";
        return DriverManager.getConnection(url, username, password);
    }
}
