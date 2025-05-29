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

                // Logowanie użytkownika
                if (command.startsWith("LOGIN")) {
                    String[] parts = command.split(";");
                    out.println(handleLogin(parts[1], parts[2]));

                    // Operacje klienta
                } else if (command.startsWith("PRZYPISZ_KIEROWCE_DO_ZLECENIA")) {
                    String[] parts = command.split(";", 3);
                    out.println(przypiszKierowcaDoZlecenia(parts[1], parts[2]));
                }else if (command.startsWith("DODAJ_ZLECENIE")) {
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

                    // Operacje magazyniera
                } else if (command.startsWith("POBIERZ_DO_PRZYJECIA")) {
                    out.println(pobierzDoPrzyjecia());

                } else if (command.startsWith("PRZYJMIJ_ZLECENIE")) {
                    out.println(zmienStatus(command.split(";")[1], "Przyjęte"));

                } else if (command.startsWith("POBIERZ_INWENTARYZACJE")) {
                    out.println(pobierzInwentaryzacje());

                } else if (command.startsWith("POBIERZ_PACZKI_NOWE")) {
                    out.println(pobierzNowePaczki());

                } else if (command.startsWith("OZNACZ_GOTOWE")) {
                    out.println(zmienStatus(command.split(";")[1], "Gotowe do wysyłki"));

                    // Operacje kuriera
                } else if (command.startsWith("POBIERZ_GOTOWE_DLA_KURIERA")) {
                    out.println(pobierzGotoweDlaKuriera());

                } else if (command.startsWith("ODEBRANA_PRZEZ_KURIERA")) {
                    out.println(zmienStatus(command.split(";")[1], "W drodze"));

                } else if (command.startsWith("POBIERZ_W_DRODZE")) {
                    out.println(pobierzWDrodze());

                } else if (command.startsWith("ZATWIERDZ_DOSTARCZENIE")) {
                    out.println(zmienStatus(command.split(";")[1], "Oczekiwanie na odbiór"));

                } else if (command.startsWith("ZGLOS_INCYDENT")) {
                    String[] parts = command.split(";", 4);
                    out.println(zglosIncydent(parts[1], parts[2], parts[3]));

                } else if (command.startsWith("SPRAWDZ_STATUS_KURIERA")) {
                    out.println(sprawdzStatusKuriera());

                } else if (command.startsWith("ZAKONCZENIE_TRASY")) {
                    String[] parts = command.split(";", 4);
                    out.println(zakonczTrase(parts[1], parts[2], parts[3]));

                    // Operacje logistyka (do implementacji w przyszłości)
                } else if (command.startsWith("POBIERZ_RAPORTY_TRAS")) {
                    out.println(pobierzRaportyTras());

                } else if (command.startsWith("POBIERZ_INCYDENTY")) {
                    out.println(pobierzIncydenty());

                } else if (command.startsWith("POBIERZ_POJAZDY")) {
                    out.println(pobierzPojazdy());

                } else if (command.startsWith("DODAJ_POJAZD")) {
                    String[] parts = command.split(";", 6);
                    out.println(dodajPojazd(parts[1], parts[2], parts[3], parts[4], parts[5]));

                } else if (command.startsWith("USUN_POJAZD")) {
                    out.println(usunPojazd(command.split(";")[1]));

                } else if (command.startsWith("PRZYPISZ_TRASE")) {
                    String[] parts = command.split(";", 3);
                    out.println(przypiszTrase(parts[1], parts[2]));

                } else if (command.startsWith("MONITORUJ_POJAZD")) {
                    out.println(monitorujPojazd(command.split(";")[1]));

                } else if (command.startsWith("GENERUJ_RAPORT_MIESIECZNY")) {
                    String[] parts = command.split(";", 3);
                    out.println(generujRaportMiesieczny(parts[1], parts[2]));

                    // Operacje administracyjne
                } else if (command.startsWith("POBIERZ_UZYTKOWNIKOW")) {
                    out.println(pobierzUzytkownikow());

                } else if (command.startsWith("DODAJ_UZYTKOWNIKA")) {
                    String[] parts = command.split(";", 6);
                    out.println(dodajUzytkownika(parts[1], parts[2], parts[3], parts[4], parts[5]));

                } else if (command.startsWith("USUN_UZYTKOWNIKA")) {
                    out.println(usunUzytkownika(command.split(";")[1]));

                } else if (command.startsWith("ZMIEN_ROLE_UZYTKOWNIKA")) {
                    String[] parts = command.split(";", 3);
                    out.println(zmienRoleUzytkownika(parts[1], parts[2]));

                    // Statystyki i raporty
                } else if (command.startsWith("POBIERZ_STATYSTYKI")) {
                    out.println(pobierzStatystyki());

                } else if (command.startsWith("POBIERZ_REKLAMACJE")) {
                    out.println(pobierzReklamacje());

                } else if (command.startsWith("POBIERZ_PROBLEMY")) {
                    out.println(pobierzProblemy());

                }else if (command.startsWith("POBIERZ_DOSTEPNE_POJAZDY")) {
                    out.println(pobierzDostepnePojazdy());

                } else if (command.startsWith("EDYTUJ_POJAZD")) {
                    String[] parts = command.split(";", 7);
                    out.println(edytujPojazd(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));

                } else if (command.startsWith("POBIERZ_KIEROWCOW")) {
                    out.println(pobierzKierowcow());

                } else if (command.startsWith("POBIERZ_ZLECENIA_DO_PRZYPISANIA")) {
                    out.println(pobierzZleceniaDoprzypisania());

                } else if (command.startsWith("POBIERZ_GOTOWE_DO_TRASY")) {
                    out.println(pobierzGotoweDOTrasy());

                } else if (command.startsWith("PRZYPISZ_POJAZD_DO_ZLECENIA")) {
                    String[] parts = command.split(";", 3);
                    out.println(przypiszPojazdDoZlecenia(parts[1], parts[2]));

                } else if (command.startsWith("POBIERZ_HARMONOGRAM")) {
                    String filter = command.split(";").length > 1 ? command.split(";")[1] : null;
                    out.println(pobierzHarmonogram(filter));

                } else if (command.startsWith("AKTUALIZUJ_STATUS_ZLECENIA")) {
                    String[] parts = command.split(";", 3);
                    out.println(aktualizujStatusZlecenia(parts[1], parts[2]));

                } else if (command.startsWith("POBIERZ_WSZYSTKIE_ZLECENIA_LOGISTYK")) {
                    out.println(pobierzWszystkieZleceniaLogistyk());

                } else if (command.startsWith("ZMIEN_STATUS_ZLECENIA")) {
                    String[] parts = command.split(";", 3);
                    out.println(zmienStatusZlecenia(parts[1], parts[2]));

                } else if (command.startsWith("POBIERZ_SZCZEGOLY_ZLECENIA")) {
                    out.println(pobierzSzczególyZlecenia(command.split(";")[1]));

                } else if (command.startsWith("POBIERZ_AKTYWNE_TRASY")) {
                    out.println(pobierzAktywneTrasy());

                } else if (command.startsWith("POBIERZ_SZCZEGOLY_TRASY")) {
                    out.println(pobierzSzczególyTrasy(command.split(";")[1]));

                } else if (command.startsWith("ANULUJ_TRASE")) {
                    out.println(anulujTrase(command.split(";")[1]));

                } else if (command.startsWith("OZNACZ_INCYDENT_ROZWIAZANY")) {
                    out.println(oznaczIncydentRozwiazany(command.split(";")[1]));

                } else if (command.startsWith("USUN_INCYDENT")) {
                    out.println(usunIncydent(command.split(";")[1]));

                } else if (command.startsWith("POBIERZ_SZCZEGOLY_INCYDENTU")) {
                    out.println(pobierzSzczególyIncydentu(command.split(";")[1]));
                }else {
                    out.println("ERROR;Nieznana komenda");
                }

                clientSocket.close();
            }

        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
        }
    }

    // ========== METODY LOGOWANIA ==========
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

    // ========== METODY KLIENTA ==========
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
                    "INSERT INTO PROBLEMY (ID_ZLECENIA, OPIS_PROBLEMU, DATA_ZGLOSZENIA) VALUES (?, ?, SYSDATE)"
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
                    "INSERT INTO REKLAMACJE (ID_ZLECENIA, OPIS_REKLAMACJI, DATA_ZGLOSZENIA) VALUES (?, ?, SYSDATE)"
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

    // ========== METODY MAGAZYNIERA ==========
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
    // Dodaj nową metodę w server.java
    private static String przypiszKierowcaDoZlecenia(String idZlecenia, String kierowcaName) {
        try (Connection conn = getConnection()) {
            // Znajdź ID kierowcy po imieniu i nazwisku
            PreparedStatement findKierowca = conn.prepareStatement(
                    "SELECT id FROM UZYTKOWNIK WHERE (imie || ' ' || nazwisko) = ? AND rola = 'KURIER'"
            );
            findKierowca.setString(1, kierowcaName);
            ResultSet rs = findKierowca.executeQuery();

            if (!rs.next()) {
                return "ERROR;Nie znaleziono kierowcy";
            }

            int kierowcaId = rs.getInt("id");

            // Przypisz kierowcę do zlecenia
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE ZLECENIA SET KIEROWCA_ID = ? WHERE ID_ZLECENIA = ?"
            );
            stmt.setInt(1, kierowcaId);
            stmt.setInt(2, Integer.parseInt(idZlecenia));

            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Kierowca został przypisany" : "ERROR;Nie znaleziono zlecenia";
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

    // ========== METODY KURIERA ==========
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

    private static String zglosIncydent(String typIncydentu, String lokalizacja, String opis) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO INCYDENTY (TYP_INCYDENTU, LOKALIZACJA, OPIS_INCYDENTU, DATA_ZGLOSZENIA) " +
                            "VALUES (?, ?, ?, SYSDATE)"
            );
            stmt.setString(1, typIncydentu);
            stmt.setString(2, lokalizacja);
            stmt.setString(3, opis);
            stmt.executeUpdate();
            return "OK;Incydent został zgłoszony do logistyka";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String sprawdzStatusKuriera() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id_zlecenia, odbiorca, opis FROM ZLECENIA WHERE status = 'W drodze'"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");

            while (rs.next()) {
                sb.append(";")
                        .append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis"));
            }

            return sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String zakonczTrase(String kilometry, String spalanie, String raport) {
        try (Connection conn = getConnection()) {
            // Sprawdź czy są jeszcze paczki w drodze
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM ZLECENIA WHERE status = 'W drodze'"
            );
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt("count") > 0) {
                return "ERROR;Nie można zakończyć trasy - pozostały niedostarczone paczki!";
            }

            // Zapisz raport z trasy
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO RAPORTY_TRAS (KILOMETRY, SPALANIE, RAPORT, DATA_ZAKONCZENIA) " +
                            "VALUES (?, ?, ?, SYSDATE)"
            );
            stmt.setInt(1, Integer.parseInt(kilometry));
            stmt.setDouble(2, Double.parseDouble(spalanie));
            stmt.setString(3, raport.isEmpty() ? "Brak uwag" : raport);
            stmt.executeUpdate();

            return "OK;Trasa została zakończona i raport przekazany do logistyka";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    // ========== METODY LOGISTYKA ==========
    private static String pobierzRaportyTras() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, kilometry, spalanie, raport, data_zakonczenia FROM RAPORTY_TRAS ORDER BY data_zakonczenia DESC"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id")).append("|")
                        .append(rs.getInt("kilometry")).append("|")
                        .append(rs.getDouble("spalanie")).append("|")
                        .append(rs.getString("raport")).append("|")
                        .append(rs.getTimestamp("data_zakonczenia"));
            }
            return sb.length() == 2 ? "ERROR;Brak raportów tras" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzIncydenty() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, typ_incydentu, lokalizacja, opis_incydentu, data_zgloszenia FROM INCYDENTY ORDER BY data_zgloszenia DESC"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id")).append("|")
                        .append(rs.getString("typ_incydentu")).append("|")
                        .append(rs.getString("lokalizacja")).append("|")
                        .append(rs.getString("opis_incydentu")).append("|")
                        .append(rs.getTimestamp("data_zgloszenia"));
            }
            return sb.length() == 2 ? "ERROR;Brak incydentów" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzPojazdy() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, marka, model, rejestracja, status, uwagi FROM POJAZDY ORDER BY marka, model"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id")).append("|")
                        .append(rs.getString("marka")).append("|")
                        .append(rs.getString("model")).append("|")
                        .append(rs.getString("rejestracja")).append("|")
                        .append(rs.getString("status")).append("|")
                        .append(rs.getString("uwagi") != null ? rs.getString("uwagi") : "");
            }
            return sb.length() == 2 ? "ERROR;Brak pojazdów w systemie" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String dodajPojazd(String marka, String model, String numerRejestracyjny, String status, String uwagi) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO POJAZDY (marka, model, rejestracja, status, uwagi) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setString(1, marka);
            stmt.setString(2, model);
            stmt.setString(3, numerRejestracyjny);
            stmt.setString(4, status);
            stmt.setString(5, uwagi);
            stmt.executeUpdate();
            return "OK;Pojazd został dodany";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String usunPojazd(String idPojazdu) {
        try (Connection conn = getConnection()) {
            // Sprawdź czy pojazd nie jest przypisany do aktywnego zlecenia
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM ZLECENIA WHERE pojazd_id = ? AND status NOT IN ('Zrealizowane', 'Odrzucone')"
            );
            checkStmt.setInt(1, Integer.parseInt(idPojazdu));
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt("count") > 0) {
                return "ERROR;Nie można usunąć pojazdu - jest przypisany do aktywnego zlecenia";
            }

            PreparedStatement stmt = conn.prepareStatement("DELETE FROM POJAZDY WHERE id = ?");
            stmt.setInt(1, Integer.parseInt(idPojazdu));
            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Pojazd został usunięty" : "ERROR;Nie znaleziono pojazdu";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String przypiszTrase(String idZlecenia, String idPojazdu) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE ZLECENIA SET pojazd_id = ?, status = 'Przypisane' WHERE id_zlecenia = ?"
            );
            stmt.setInt(1, Integer.parseInt(idPojazdu));
            stmt.setInt(2, Integer.parseInt(idZlecenia));
            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Trasa została przypisana" : "ERROR;Nie znaleziono zlecenia";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String monitorujPojazd(String idPojazdu) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, z.odbiorca, z.status, p.marka, p.model, p.numer_rejestracyjny " +
                            "FROM ZLECENIA z JOIN POJAZDY p ON z.pojazd_id = p.id " +
                            "WHERE p.id = ? AND z.status NOT IN ('Zrealizowane', 'Odrzucone')"
            );
            stmt.setInt(1, Integer.parseInt(idPojazdu));
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("status")).append("|")
                        .append(rs.getString("marka")).append(" ")
                        .append(rs.getString("model")).append("|")
                        .append(rs.getString("numer_rejestracyjny"));
            }
            return sb.length() == 2 ? "ERROR;Brak aktywnych zleceń dla tego pojazdu" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String generujRaportMiesieczny(String rok, String miesiac) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT " +
                            "COUNT(*) as total_zlecen, " +
                            "COUNT(CASE WHEN status = 'Zrealizowane' THEN 1 END) as zrealizowane, " +
                            "COUNT(CASE WHEN status = 'Odrzucone' THEN 1 END) as odrzucone, " +
                            "COUNT(CASE WHEN status IN ('Nowe', 'Przyjęte', 'W drodze') THEN 1 END) as w_trakcie, " +
                            "SUM(waga) as total_waga " +
                            "FROM ZLECENIA " +
                            "WHERE EXTRACT(YEAR FROM data_nadania) = ? AND EXTRACT(MONTH FROM data_nadania) = ?"
            );
            stmt.setInt(1, Integer.parseInt(rok));
            stmt.setInt(2, Integer.parseInt(miesiac));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "OK;" + rs.getInt("total_zlecen") + "|" +
                        rs.getInt("zrealizowane") + "|" +
                        rs.getInt("odrzucone") + "|" +
                        rs.getInt("w_trakcie") + "|" +
                        rs.getDouble("total_waga");
            } else {
                return "ERROR;Brak danych za podany okres";
            }
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    // ========== METODY ADMINISTRACYJNE ==========
    private static String pobierzUzytkownikow() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, imie, nazwisko, login, rola FROM UZYTKOWNIK ORDER BY rola, nazwisko, imie"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id")).append("|")
                        .append(rs.getString("imie")).append("|")
                        .append(rs.getString("nazwisko")).append("|")
                        .append(rs.getString("login")).append("|")
                        .append(rs.getString("rola"));
            }
            return sb.length() == 2 ? "ERROR;Brak użytkowników" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String dodajUzytkownika(String imie, String nazwisko, String login, String haslo, String rola) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO UZYTKOWNIK (imie, nazwisko, login, haslo, rola) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setString(1, imie);
            stmt.setString(2, nazwisko);
            stmt.setString(3, login);
            stmt.setString(4, haslo);
            stmt.setString(5, rola);
            stmt.executeUpdate();
            return "OK;Użytkownik został dodany";
        } catch (SQLIntegrityConstraintViolationException e) {
            return "ERROR;Login już istnieje";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String usunUzytkownika(String idUzytkownika) {
        try (Connection conn = getConnection()) {
            // Sprawdź czy użytkownik ma aktywne zlecenia
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM ZLECENIA WHERE nadawca_id = ? AND status NOT IN ('Zrealizowane', 'Odrzucone')"
            );
            checkStmt.setInt(1, Integer.parseInt(idUzytkownika));
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt("count") > 0) {
                return "ERROR;Nie można usunąć użytkownika - ma aktywne zlecenia";
            }

            PreparedStatement stmt = conn.prepareStatement("DELETE FROM UZYTKOWNIK WHERE id = ?");
            stmt.setInt(1, Integer.parseInt(idUzytkownika));
            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Użytkownik został usunięty" : "ERROR;Nie znaleziono użytkownika";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String zmienRoleUzytkownika(String idUzytkownika, String nowaRola) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE UZYTKOWNIK SET rola = ? WHERE id = ?"
            );
            stmt.setString(1, nowaRola);
            stmt.setInt(2, Integer.parseInt(idUzytkownika));
            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Rola została zmieniona" : "ERROR;Nie znaleziono użytkownika";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    // ========== STATYSTYKI I RAPORTY ==========
    private static String pobierzStatystyki() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT " +
                            "COUNT(*) as total_zlecen, " +
                            "COUNT(CASE WHEN status = 'Zrealizowane' THEN 1 END) as zrealizowane, " +
                            "COUNT(CASE WHEN status = 'W drodze' THEN 1 END) as w_drodze, " +
                            "COUNT(CASE WHEN status = 'Nowe' THEN 1 END) as nowe, " +
                            "SUM(waga) as total_waga, " +
                            "(SELECT COUNT(*) FROM UZYTKOWNIK) as total_uzytkownikow, " +
                            "(SELECT COUNT(*) FROM POJAZDY) as total_pojazdow, " +
                            "(SELECT COUNT(*) FROM INCYDENTY WHERE data_zgloszenia >= SYSDATE - 30) as incydenty_30dni " +
                            "FROM ZLECENIA"
            );
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "OK;" + rs.getInt("total_zlecen") + "|" +
                        rs.getInt("zrealizowane") + "|" +
                        rs.getInt("w_drodze") + "|" +
                        rs.getInt("nowe") + "|" +
                        rs.getDouble("total_waga") + "|" +
                        rs.getInt("total_uzytkownikow") + "|" +
                        rs.getInt("total_pojazdow") + "|" +
                        rs.getInt("incydenty_30dni");
            } else {
                return "ERROR;Brak danych statystycznych";
            }
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzReklamacje() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT r.id, r.id_zlecenia, r.opis_reklamacji, r.data_zgloszenia, " +
                            "z.odbiorca, z.opis as opis_zlecenia " +
                            "FROM REKLAMACJE r JOIN ZLECENIA z ON r.id_zlecenia = z.id_zlecenia " +
                            "ORDER BY r.data_zgloszenia DESC"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id")).append("|")
                        .append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("opis_reklamacji")).append("|")
                        .append(rs.getTimestamp("data_zgloszenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis_zlecenia"));
            }
            return sb.length() == 2 ? "ERROR;Brak reklamacji" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzProblemy() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT p.id, p.id_zlecenia, p.opis_problemu, p.data_zgloszenia, " +
                            "z.odbiorca, z.opis as opis_zlecenia " +
                            "FROM PROBLEMY p JOIN ZLECENIA z ON p.id_zlecenia = z.id_zlecenia " +
                            "ORDER BY p.data_zgloszenia DESC"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id")).append("|")
                        .append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("opis_problemu")).append("|")
                        .append(rs.getTimestamp("data_zgloszenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis_zlecenia"));
            }
            return sb.length() == 2 ? "ERROR;Brak problemów" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    // ========== METODY POMOCNICZE ==========
    private static String zmienStatus(String idZlecenia, String nowyStatus) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE ZLECENIA SET status = ? WHERE id_zlecenia = ?");
            stmt.setString(1, nowyStatus);
            stmt.setInt(2, Integer.parseInt(idZlecenia));
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                String wiadomosc = getStatusMessage(nowyStatus);
                return "OK;" + wiadomosc;
            } else {
                return "ERROR;Nie znaleziono zlecenia";
            }
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String getStatusMessage(String status) {
        switch (status) {
            case "Zrealizowane": return "Przesyłka została zrealizowana";
            case "Odrzucone": return "Przesyłka została odrzucona";
            case "Przyjęte": return "Towar został przyjęty do magazynu";
            case "Gotowe do wysyłki": return "Paczka jest gotowa do wysyłki";
            case "W drodze": return "Paczka jest w drodze";
            case "Oczekiwanie na odbiór": return "Paczka oczekuje na odbiór";
            case "Przypisane": return "Zlecenie zostało przypisane do pojazdu";
            default: return "Status został zmieniony";
        }
    }

    // Dodatkowe metody dla rozszerzonej funkcjonalności
    private static String pobierzZleceniaWStatusie(String status) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, z.odbiorca, z.opis, z.waga, z.data_nadania, " +
                            "u.imie || ' ' || u.nazwisko as nadawca " +
                            "FROM ZLECENIA z JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                            "WHERE z.status = ? ORDER BY z.data_nadania"
            );
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania")).append("|")
                        .append(rs.getString("nadawca"));
            }
            return sb.length() == 2 ? "ERROR;Brak zleceń w statusie " + status : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzSzczególyZlecenia(String idZlecenia) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.*, u.imie || ' ' || u.nazwisko as nadawca_name, " +
                            "p.marka || ' ' || p.model || ' (' || p.numer_rejestracyjny || ')' as pojazd_info " +
                            "FROM ZLECENIA z " +
                            "JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                            "LEFT JOIN POJAZDY p ON z.pojazd_id = p.id " +
                            "WHERE z.id_zlecenia = ?"
            );
            stmt.setInt(1, Integer.parseInt(idZlecenia));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "OK;" + rs.getInt("id_zlecenia") + "|" +
                        rs.getString("nadawca_name") + "|" +
                        rs.getString("odbiorca") + "|" +
                        rs.getString("adres") + "|" +
                        rs.getString("miasto") + "|" +
                        rs.getString("kod_pocztowy") + "|" +
                        rs.getString("opis") + "|" +
                        rs.getDouble("waga") + "|" +
                        rs.getDate("data_nadania") + "|" +
                        rs.getString("status") + "|" +
                        (rs.getString("pojazd_info") != null ? rs.getString("pojazd_info") : "Brak przypisania");
            } else {
                return "ERROR;Nie znaleziono zlecenia";
            }
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }
    private static String pobierzDostepnePojazdy() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, marka, model, rejestracja FROM POJAZDY WHERE status = 'Dostępny'"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id")).append("|")
                        .append(rs.getString("marka")).append("|")
                        .append(rs.getString("model")).append("|")
                        .append(rs.getString("rejestracja"));
            }
            return sb.length() == 2 ? "ERROR;Brak dostępnych pojazdów" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String edytujPojazd(String id, String marka, String model, String rejestracja, String status, String uwagi) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE POJAZDY SET marka = ?, model = ?, rejestracja = ?, status = ?, uwagi = ? WHERE id = ?"
            );
            stmt.setString(1, marka);
            stmt.setString(2, model);
            stmt.setString(3, rejestracja);
            stmt.setString(4, status);
            stmt.setString(5, uwagi);
            stmt.setInt(6, Integer.parseInt(id));
            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Pojazd został zaktualizowany" : "ERROR;Nie znaleziono pojazdu";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }
    private static String pobierzKierowcow() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT imie || ' ' || nazwisko as kierowca FROM UZYTKOWNIK WHERE rola = 'KURIER'"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getString("kierowca"));
            }
            return sb.length() == 2 ? "ERROR;Brak kierowców" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzZleceniaDoprzypisania() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, u.imie || ' ' || u.nazwisko as nadawca, z.odbiorca, z.miasto, z.waga, z.status " +
                            "FROM ZLECENIA z JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                            "WHERE z.status IN ('Gotowe do wysyłki', 'Przyjęte') AND z.pojazd_id IS NULL"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("nadawca")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("miasto")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getString("status"));
            }
            return sb.length() == 2 ? "ERROR;Brak zleceń do przypisania" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzGotoweDOTrasy() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id_zlecenia, odbiorca, miasto, adres, waga FROM ZLECENIA " +
                            "WHERE status = 'Gotowe do wysyłki' AND pojazd_id IS NOT NULL"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("miasto")).append("|")
                        .append(rs.getString("adres")).append("|")
                        .append(rs.getDouble("waga"));
            }
            return sb.length() == 2 ? "ERROR;Brak gotowych zleceń" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String przypiszPojazdDoZlecenia(String idZlecenia, String idPojazdu) {
        try (Connection conn = getConnection()) {
            // Sprawdź czy pojazd jest dostępny
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT status FROM POJAZDY WHERE id = ?"
            );
            checkStmt.setInt(1, Integer.parseInt(idPojazdu));
            ResultSet checkRs = checkStmt.executeQuery();

            if (!checkRs.next() || !"Dostępny".equals(checkRs.getString("status"))) {
                return "ERROR;Pojazd nie jest dostępny";
            }

            // Przypisz pojazd
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE ZLECENIA SET pojazd_id = ? WHERE id_zlecenia = ?"
            );
            stmt.setInt(1, Integer.parseInt(idPojazdu));
            stmt.setInt(2, Integer.parseInt(idZlecenia));

            // Zmień status pojazdu na zajęty
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE POJAZDY SET status = 'Zajęty' WHERE id = ?"
            );
            updateStmt.setInt(1, Integer.parseInt(idPojazdu));

            int rows = stmt.executeUpdate();
            updateStmt.executeUpdate();

            return rows > 0 ? "OK;Pojazd został przypisany" : "ERROR;Nie znaleziono zlecenia";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzHarmonogram(String filter) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT z.id_zlecenia, z.data_nadania, u.imie || ' ' || u.nazwisko as nadawca, " +
                    "z.odbiorca, z.miasto, z.adres, " +
                    "COALESCE(p.marka || ' ' || p.model, 'Brak') as pojazd, " +
                    "COALESCE(k.imie || ' ' || k.nazwisko, 'Nieprzypisany') as kierowca, " +
                    "z.status " +
                    "FROM ZLECENIA z " +
                    "JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                    "LEFT JOIN POJAZDY p ON z.pojazd_id = p.id " +
                    "LEFT JOIN UZYTKOWNIK k ON z.kierowca_id = k.id AND k.rola = 'KURIER'"; // POPRAWKA TUTAJ

            if (filter != null && !filter.equals("Wszystkie")) {
                sql += " WHERE z.status = ?";
            }
            sql += " ORDER BY z.data_nadania DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            if (filter != null && !filter.equals("Wszystkie")) {
                stmt.setString(1, filter);
            }

            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getDate("data_nadania")).append("|")
                        .append(rs.getString("nadawca")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("miasto")).append("|")
                        .append(rs.getString("adres")).append("|")
                        .append(rs.getString("pojazd")).append("|")
                        .append(rs.getString("kierowca")).append("|")
                        .append(rs.getString("status"));
            }
            return sb.length() == 2 ? "ERROR;Brak zleceń" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String aktualizujStatusZlecenia(String idZlecenia, String nowyStatus) {
        return zmienStatus(idZlecenia, nowyStatus);
    }

    private static String pobierzWszystkieZleceniaLogistyk() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT z.id_zlecenia, u.imie || ' ' || u.nazwisko as nadawca, z.odbiorca, " +
                            "z.adres, z.miasto, z.opis, z.waga, z.data_nadania, z.status, " +
                            "COALESCE(p.marka || ' ' || p.model, '-') as pojazd " +
                            "FROM ZLECENIA z " +
                            "JOIN UZYTKOWNIK u ON z.nadawca_id = u.id " +
                            "LEFT JOIN POJAZDY p ON z.pojazd_id = p.id " +
                            "ORDER BY z.data_nadania DESC"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("id_zlecenia")).append("|")
                        .append(rs.getString("nadawca")).append("|")
                        .append(rs.getString("odbiorca")).append("|")
                        .append(rs.getString("adres")).append("|")
                        .append(rs.getString("miasto")).append("|")
                        .append(rs.getString("opis")).append("|")
                        .append(rs.getDouble("waga")).append("|")
                        .append(rs.getDate("data_nadania")).append("|")
                        .append(rs.getString("status")).append("|")
                        .append(rs.getString("pojazd"));
            }
            return sb.length() == 2 ? "ERROR;Brak zleceń" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String zmienStatusZlecenia(String idZlecenia, String nowyStatus) {
        return zmienStatus(idZlecenia, nowyStatus);
    }

    private static String pobierzAktywneTrasy() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT DISTINCT p.id as trasa_id, k.imie || ' ' || k.nazwisko as kierowca, " +
                            "'W trakcie' as status, COUNT(z.id_zlecenia) as liczba_zlecen, " +
                            "SYSDATE as data_rozpoczecia, '120' as szacunkowy_czas " +
                            "FROM POJAZDY p " +
                            "JOIN ZLECENIA z ON p.id = z.pojazd_id " +
                            "LEFT JOIN UZYTKOWNIK k ON k.rola = 'KURIER' " +
                            "WHERE z.status IN ('W drodze', 'Przypisane') " +
                            "GROUP BY p.id, k.imie, k.nazwisko"
            );
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("OK");
            while (rs.next()) {
                sb.append(";").append(rs.getInt("trasa_id")).append("|")
                        .append(rs.getString("kierowca")).append("|")
                        .append(rs.getString("status")).append("|")
                        .append(rs.getInt("liczba_zlecen")).append("|")
                        .append(rs.getTimestamp("data_rozpoczecia")).append("|")
                        .append(rs.getString("szacunkowy_czas"));
            }
            return sb.length() == 2 ? "ERROR;Brak aktywnych tras" : sb.toString();
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzSzczególyTrasy(String idTrasy) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT k.imie || ' ' || k.nazwisko as kierowca, 'W trakcie' as status, " +
                            "COUNT(z.id_zlecenia) as liczba_zlecen, SYSDATE as data_utworzenia, " +
                            "SYSDATE as data_rozpoczecia, '120' as szacunkowy_czas, " +
                            "NULL as data_zakonczenia, 'Trasa w realizacji' as uwagi, " +
                            "p.marka || ' ' || p.model as pojazd_info " +
                            "FROM POJAZDY p " +
                            "JOIN ZLECENIA z ON p.id = z.pojazd_id " +
                            "LEFT JOIN UZYTKOWNIK k ON k.rola = 'KURIER' " +
                            "WHERE p.id = ? AND z.status IN ('W drodze', 'Przypisane') " +
                            "GROUP BY p.id, k.imie, k.nazwisko, p.marka, p.model"
            );
            stmt.setInt(1, Integer.parseInt(idTrasy));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "OK;" + rs.getString("kierowca") + "|" +
                        rs.getString("status") + "|" +
                        rs.getInt("liczba_zlecen") + "|" +
                        rs.getTimestamp("data_utworzenia") + "|" +
                        rs.getTimestamp("data_rozpoczecia") + "|" +
                        rs.getString("szacunkowy_czas") + "|" +
                        rs.getTimestamp("data_zakonczenia") + "|" +
                        rs.getString("uwagi") + "|" +
                        rs.getString("pojazd_info");
            } else {
                return "ERROR;Nie znaleziono trasy";
            }
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String anulujTrase(String idTrasy) {
        try (Connection conn = getConnection()) {
            // Zwolnij pojazd
            PreparedStatement updatePojazd = conn.prepareStatement(
                    "UPDATE POJAZDY SET status = 'Dostępny' WHERE id = ?"
            );
            updatePojazd.setInt(1, Integer.parseInt(idTrasy));
            updatePojazd.executeUpdate();

            // Zmień status zleceń
            PreparedStatement updateZlecenia = conn.prepareStatement(
                    "UPDATE ZLECENIA SET pojazd_id = NULL, status = 'Gotowe do wysyłki' " +
                            "WHERE pojazd_id = ? AND status IN ('Przypisane', 'W drodze')"
            );
            updateZlecenia.setInt(1, Integer.parseInt(idTrasy));
            int rows = updateZlecenia.executeUpdate();

            return rows > 0 ? "OK;Trasa została anulowana" : "ERROR;Nie znaleziono trasy";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String oznaczIncydentRozwiazany(String idIncydentu) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE INCYDENTY SET status = 'Rozwiązany' WHERE id = ?"
            );
            stmt.setInt(1, Integer.parseInt(idIncydentu));
            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Incydent oznaczony jako rozwiązany" : "ERROR;Nie znaleziono incydentu";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String usunIncydent(String idIncydentu) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM INCYDENTY WHERE id = ?");
            stmt.setInt(1, Integer.parseInt(idIncydentu));
            int rows = stmt.executeUpdate();
            return rows > 0 ? "OK;Incydent został usunięty" : "ERROR;Nie znaleziono incydentu";
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }

    private static String pobierzSzczególyIncydentu(String idIncydentu) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT typ_incydentu, lokalizacja, data_zgloszenia, opis_incydentu, " +
                            "COALESCE(status, 'Nowy') as status FROM INCYDENTY WHERE id = ?"
            );
            stmt.setInt(1, Integer.parseInt(idIncydentu));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "OK;" + rs.getString("typ_incydentu") + "|" +
                        rs.getString("lokalizacja") + "|" +
                        rs.getTimestamp("data_zgloszenia") + "|" +
                        rs.getString("opis_incydentu") + "|" +
                        rs.getString("status");
            } else {
                return "ERROR;Nie znaleziono incydentu";
            }
        } catch (Exception e) {
            return "ERROR;" + e.getMessage();
        }
    }
    private static Connection getConnection() throws SQLException {
        String url = "jdbc:oracle:thin:@localhost:1521";
        String username = "SYSTEM";
        String password = "admin";
        return DriverManager.getConnection(url, username, password);
    }
}