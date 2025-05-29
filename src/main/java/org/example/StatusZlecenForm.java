package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class StatusZlecenForm extends JFrame {

    private JTable zleceniaTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterBox;
    private JLabel statystykiLabel;
    private TableRowSorter<DefaultTableModel> sorter;

    public StatusZlecenForm() {
        setTitle("Status wszystkich zleceń - Dashboard Logistyka");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek z przyciskami kontrolnymi
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabela ze zleceniami
        createZleceniaTable();
        JScrollPane tableScrollPane = new JScrollPane(zleceniaTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Lista wszystkich zleceń"));
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Panel ze statystykami
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        // Wczytaj dane
        wczytajZlecenia();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 248, 248));
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Tytuł
        JLabel title = new JLabel("📊 Dashboard Logistyka - Status Zleceń");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(title, BorderLayout.CENTER);

        // Panel z filtrami i przyciskami
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(new Color(248, 248, 248));

        // Filtr statusu
        controlPanel.add(new JLabel("Filtruj według statusu:"));
        statusFilterBox = new JComboBox<>(new String[]{
                "Wszystkie", "Nowe", "Przyjęte", "Gotowe do wysyłki",
                "W drodze", "Oczekiwanie na odbiór", "Zrealizowane", "Odrzucone"
        });
        statusFilterBox.addActionListener(e -> filtrujTabele());
        controlPanel.add(statusFilterBox);

        controlPanel.add(Box.createRigidArea(new Dimension(20, 0)));

        // Przyciski akcji
        JButton odswiezButton = new JButton("🔄 Odśwież");
        odswiezButton.setBackground(new Color(70, 105, 255));
        odswiezButton.setForeground(Color.WHITE);
        odswiezButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        odswiezButton.setFocusPainted(false);
        odswiezButton.addActionListener(e -> wczytajZlecenia());
        controlPanel.add(odswiezButton);

        JButton szczególyButton = new JButton("🔍 Szczegóły");
        szczególyButton.setBackground(new Color(40, 167, 69));
        szczególyButton.setForeground(Color.WHITE);
        szczególyButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        szczególyButton.setFocusPainted(false);
        szczególyButton.addActionListener(e -> pokazSzczególyZlecenia());
        controlPanel.add(szczególyButton);

        JButton przypisPojazd = new JButton("🚛 Przypisz pojazd");
        przypisPojazd.setBackground(new Color(255, 193, 7));
        przypisPojazd.setForeground(Color.BLACK);
        przypisPojazd.setFont(new Font("SansSerif", Font.BOLD, 12));
        przypisPojazd.setFocusPainted(false);
        przypisPojazd.addActionListener(e -> przypisPojazd());
        controlPanel.add(przypisPojazd);

        JButton zmienStatusButton = new JButton("✏️ Zmień status");
        zmienStatusButton.setBackground(new Color(108, 117, 125));
        zmienStatusButton.setForeground(Color.WHITE);
        zmienStatusButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        zmienStatusButton.setFocusPainted(false);
        zmienStatusButton.addActionListener(e -> zmienStatusZlecenia());
        controlPanel.add(zmienStatusButton);

        headerPanel.add(controlPanel, BorderLayout.SOUTH);
        return headerPanel;
    }

    private void createZleceniaTable() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Nadawca");
        tableModel.addColumn("Odbiorca");
        tableModel.addColumn("Adres dostawy");
        tableModel.addColumn("Opis");
        tableModel.addColumn("Waga [kg]");
        tableModel.addColumn("Data nadania");
        tableModel.addColumn("Status");
        tableModel.addColumn("Pojazd");

        zleceniaTable = new JTable(tableModel);
        zleceniaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        zleceniaTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        zleceniaTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        zleceniaTable.setRowHeight(25);

        // Ukryj kolumnę ID
        zleceniaTable.getColumnModel().getColumn(0).setMinWidth(0);
        zleceniaTable.getColumnModel().getColumn(0).setMaxWidth(0);
        zleceniaTable.getColumnModel().getColumn(0).setWidth(0);

        // Ustaw szerokości kolumn
        zleceniaTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Nadawca
        zleceniaTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Odbiorca
        zleceniaTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Adres
        zleceniaTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Opis
        zleceniaTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Waga
        zleceniaTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Data
        zleceniaTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Status
        zleceniaTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Pojazd

        // Dodaj sortowanie
        sorter = new TableRowSorter<>(tableModel);
        zleceniaTable.setRowSorter(sorter);

        // Kolorowanie wierszy według statusu
        zleceniaTable.setDefaultRenderer(Object.class, new StatusColorRenderer());
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(new Color(248, 248, 248));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statystyki"));

        statystykiLabel = new JLabel("Ładowanie statystyk...");
        statystykiLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statystykiLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statsPanel.add(statystykiLabel, BorderLayout.CENTER);

        return statsPanel;
    }

    private void wczytajZlecenia() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_WSZYSTKIE_ZLECENIA_LOGISTYK");
            String response = in.readLine();

            // Wyczyść tabelę
            tableModel.setRowCount(0);

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    if (fields.length >= 8) {
                        tableModel.addRow(new Object[]{
                                Integer.parseInt(fields[0]),           // ID
                                fields[1],                            // Nadawca
                                fields[2],                            // Odbiorca
                                fields[3] + ", " + fields[4],         // Adres (adres + miasto)
                                fields[5],                            // Opis
                                Double.parseDouble(fields[6]),        // Waga
                                fields[7],                            // Data nadania
                                fields[8],                            // Status
                                fields.length > 9 ? fields[9] : "-"  // Pojazd
                        });
                    }
                }

                // Aktualizuj statystyki
                aktualizujStatystyki();
            } else if (!response.equals("ERROR;Brak zleceń w systemie")) {
                JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania zleceń: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem podczas wczytywania zleceń.");
        }
    }

    private void filtrujTabele() {
        String selectedStatus = (String) statusFilterBox.getSelectedItem();

        if ("Wszystkie".equals(selectedStatus)) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + selectedStatus, 7)); // Kolumna Status
        }

        aktualizujStatystyki();
    }

    private void aktualizujStatystyki() {
        int[] statusCounts = new int[8]; // Nowe, Przyjęte, Gotowe, W drodze, Oczekiwanie, Zrealizowane, Odrzucone, Inne
        int totalVisible = zleceniaTable.getRowCount();
        double totalWeight = 0;

        for (int i = 0; i < totalVisible; i++) {
            String status = (String) zleceniaTable.getValueAt(i, 7);
            double weight = (Double) zleceniaTable.getValueAt(i, 5);
            totalWeight += weight;

            switch (status) {
                case "Nowe": statusCounts[0]++; break;
                case "Przyjęte": statusCounts[1]++; break;
                case "Gotowe do wysyłki": statusCounts[2]++; break;
                case "W drodze": statusCounts[3]++; break;
                case "Oczekiwanie na odbiór": statusCounts[4]++; break;
                case "Zrealizowane": statusCounts[5]++; break;
                case "Odrzucone": statusCounts[6]++; break;
                default: statusCounts[7]++; break;
            }
        }

        String stats = String.format(
                "<html><div style='text-align: center;'>" +
                        "<b>Łącznie zleceń: %d</b> | <b>Łączna waga: %.1f kg</b><br>" +
                        "🆕 Nowe: %d | 📦 Przyjęte: %d | ✅ Gotowe: %d | 🚛 W drodze: %d | " +
                        "⏳ Oczekujące: %d | ✔️ Zrealizowane: %d | ❌ Odrzucone: %d" +
                        "</div></html>",
                totalVisible, totalWeight,
                statusCounts[0], statusCounts[1], statusCounts[2], statusCounts[3],
                statusCounts[4], statusCounts[5], statusCounts[6]
        );

        statystykiLabel.setText(stats);
    }

    private void pokazSzczególyZlecenia() {
        int selectedRow = zleceniaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz zlecenie do wyświetlenia szczegółów.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int zlecenieId = (int) zleceniaTable.getValueAt(selectedRow, 0);
        new SzczegolyZleceniaDialog(this, zlecenieId);
    }

    private void przypisPojazd() {
        int selectedRow = zleceniaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz zlecenie do przypisania pojazdu.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int zlecenieId = (int) zleceniaTable.getValueAt(selectedRow, 0);
        new PrzypisaniePojazduDialog(this, zlecenieId, () -> wczytajZlecenia());
    }

    private void zmienStatusZlecenia() {
        int selectedRow = zleceniaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz zlecenie do zmiany statusu.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int zlecenieId = (int) zleceniaTable.getValueAt(selectedRow, 0);
        String aktualnyStatus = (String) zleceniaTable.getValueAt(selectedRow, 7);

        String[] dostepneStatusy = {"Nowe", "Przyjęte", "Gotowe do wysyłki", "W drodze", "Oczekiwanie na odbiór", "Zrealizowane", "Odrzucone"};
        String nowyStatus = (String) JOptionPane.showInputDialog(
                this,
                "Wybierz nowy status dla zlecenia ID: " + zlecenieId + "\nAktualny status: " + aktualnyStatus,
                "Zmiana statusu zlecenia",
                JOptionPane.QUESTION_MESSAGE,
                null,
                dostepneStatusy,
                aktualnyStatus
        );

        if (nowyStatus != null && !nowyStatus.equals(aktualnyStatus)) {
            zmienStatusZlecenia(zlecenieId, nowyStatus);
        }
    }

    private void zmienStatusZlecenia(int zlecenieId, String nowyStatus) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("ZMIEN_STATUS_ZLECENIA;" + zlecenieId + ";" + nowyStatus);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Status zlecenia został zmieniony!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wczytajZlecenia();
            } else {
                JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Klasa do kolorowania wierszy według statusu
    private static class StatusColorRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                String status = (String) table.getValueAt(row, 7);
                switch (status) {
                    case "Nowe":
                        c.setBackground(new Color(255, 248, 225)); // Jasny żółty
                        break;
                    case "Przyjęte":
                        c.setBackground(new Color(225, 245, 254)); // Jasny niebieski
                        break;
                    case "Gotowe do wysyłki":
                        c.setBackground(new Color(240, 248, 255)); // Bardzo jasny niebieski
                        break;
                    case "W drodze":
                        c.setBackground(new Color(255, 243, 205)); // Jasny pomarańczowy
                        break;
                    case "Oczekiwanie na odbiór":
                        c.setBackground(new Color(253, 235, 208)); // Beżowy
                        break;
                    case "Zrealizowane":
                        c.setBackground(new Color(230, 255, 230)); // Jasny zielony
                        break;
                    case "Odrzucone":
                        c.setBackground(new Color(255, 235, 235)); // Jasny czerwony
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        break;
                }
            }

            return c;
        }
    }
}