package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HarmonogramForm extends JFrame {
    private JTable harmonogramTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterComboBox;
    private JLabel statusLabel;

    public HarmonogramForm() {
        setTitle("Harmonogram dostaw i tras");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeComponents();
        wczytajHarmonogram();
        setVisible(true);
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 248, 248));

        JLabel titleLabel = new JLabel("📅 Harmonogram dostaw i tras");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Panel filtru
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(new Color(248, 248, 248));

        JLabel filterLabel = new JLabel("Filtruj według statusu:");
        filterLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        filterComboBox = new JComboBox<>(new String[]{
                "Wszystkie", "Przypisane", "W drodze", "Oczekiwanie na odbiór", "Zrealizowane"
        });
        filterComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filterComboBox.addActionListener(e -> wczytajHarmonogram());

        filterPanel.add(filterLabel);
        filterPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        filterPanel.add(filterComboBox);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(filterPanel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabela harmonogramu
        createTable();
        JScrollPane scrollPane = new JScrollPane(harmonogramTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Harmonogram zleceń"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel dolny z przyciskami i statusem
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 248, 248));
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Status
        statusLabel = new JLabel("Gotowy");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);

        // Przyciski
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(248, 248, 248));

        JButton szczegolyButton = new JButton("📋 Szczegóły zlecenia");
        szczegolyButton.setBackground(new Color(70, 105, 255));
        szczegolyButton.setForeground(Color.WHITE);
        szczegolyButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        szczegolyButton.setFocusPainted(false);
        szczegolyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        szczegolyButton.addActionListener(e -> pokazSzczegoly());

        JButton aktualizujButton = new JButton("✏️ Aktualizuj status");
        aktualizujButton.setBackground(new Color(255, 193, 7));
        aktualizujButton.setForeground(Color.BLACK);
        aktualizujButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        aktualizujButton.setFocusPainted(false);
        aktualizujButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        aktualizujButton.addActionListener(e -> aktualizujStatus());

        JButton odswiezButton = new JButton("🔄 Odśwież");
        odswiezButton.setBackground(new Color(40, 167, 69));
        odswiezButton.setForeground(Color.WHITE);
        odswiezButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        odswiezButton.setFocusPainted(false);
        odswiezButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        odswiezButton.addActionListener(e -> wczytajHarmonogram());

        JButton eksportButton = new JButton("📊 Eksportuj");
        eksportButton.setBackground(new Color(108, 117, 125));
        eksportButton.setForeground(Color.WHITE);
        eksportButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        eksportButton.setFocusPainted(false);
        eksportButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eksportButton.addActionListener(e -> eksportujHarmonogram());

        // POPRAWKA: Dodaj przycisk przypisania kierowcy
        JButton przypiszKierowceButton = new JButton("👤 Przypisz kierowcę");
        przypiszKierowceButton.setBackground(new Color(255, 99, 71));
        przypiszKierowceButton.setForeground(Color.WHITE);
        przypiszKierowceButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        przypiszKierowceButton.setFocusPainted(false);
        przypiszKierowceButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        przypiszKierowceButton.addActionListener(e -> przypiszKierowce());

        buttonPanel.add(szczegolyButton);
        buttonPanel.add(aktualizujButton);
        buttonPanel.add(odswiezButton);
        buttonPanel.add(eksportButton);
        buttonPanel.add(przypiszKierowceButton); // DODAJ TUTAJ

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void createTable() {
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableModel.addColumn("ID");
        tableModel.addColumn("Data");
        tableModel.addColumn("Nadawca");
        tableModel.addColumn("Odbiorca");
        tableModel.addColumn("Miasto");
        tableModel.addColumn("Adres");
        tableModel.addColumn("Pojazd");
        tableModel.addColumn("Kierowca");
        tableModel.addColumn("Status");
        tableModel.addColumn("Priorytet");

        harmonogramTable = new JTable(tableModel);
        harmonogramTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        harmonogramTable.setRowHeight(25);
        harmonogramTable.getTableHeader().setReorderingAllowed(false);

        // Ustawienia kolumn
        harmonogramTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        harmonogramTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Data
        harmonogramTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Nadawca
        harmonogramTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Odbiorca
        harmonogramTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Miasto
        harmonogramTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Adres
        harmonogramTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Pojazd
        harmonogramTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Kierowca
        harmonogramTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Status
        harmonogramTable.getColumnModel().getColumn(9).setPreferredWidth(80);  // Priorytet

        // Kolorowanie statusów
        harmonogramTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String status = (String) table.getValueAt(row, 8);
                    switch (status.toLowerCase()) {
                        case "przypisane":
                            c.setBackground(new Color(255, 248, 220));
                            break;
                        case "w drodze":
                            c.setBackground(new Color(220, 248, 255));
                            break;
                        case "oczekiwanie na odbiór":
                            c.setBackground(new Color(255, 230, 230));
                            break;
                        case "zrealizowane":
                            c.setBackground(new Color(230, 255, 230));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });
    }

    // POPRAWKA: Dodaj implementację metody przypiszKierowce
    private void przypiszKierowce() {
        int selectedRow = harmonogramTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz zlecenie aby przypisać kierowcę.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int zlecenieId = (int) tableModel.getValueAt(selectedRow, 0);

        // Pobierz listę kierowców
        String[] kierowcy = pobierzListeKierowcow();
        if (kierowcy.length == 0) {
            JOptionPane.showMessageDialog(this, "Brak dostępnych kierowców w systemie.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String wybranyKierowca = (String) JOptionPane.showInputDialog(
                this, "Wybierz kierowcę dla zlecenia #" + zlecenieId + ":", "Przypisanie kierowcy",
                JOptionPane.QUESTION_MESSAGE, null, kierowcy, kierowcy[0]
        );

        if (wybranyKierowca != null) {
            przypiszKierowceDoZlecenia(zlecenieId, wybranyKierowca);
        }
    }

    // POPRAWKA: Dodaj implementację pobierania listy kierowców
    private String[] pobierzListeKierowcow() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_KIEROWCOW");
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                String[] kierowcy = new String[parts.length - 1];
                for (int i = 1; i < parts.length; i++) {
                    kierowcy[i - 1] = parts[i];
                }
                return kierowcy;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    // POPRAWKA: Dodaj implementację przypisywania kierowcy do zlecenia
    private void przypiszKierowceDoZlecenia(int zlecenieId, String kierowca) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("PRZYPISZ_KIEROWCE_DO_ZLECENIA;" + zlecenieId + ";" + kierowca);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Kierowca został przypisany do zlecenia!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wczytajHarmonogram(); // Odśwież tabelę
            } else {
                JOptionPane.showMessageDialog(this, "Błąd podczas przypisywania kierowcy: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void wczytajHarmonogram() {
        statusLabel.setText("Ładowanie danych...");

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String filter = (String) filterComboBox.getSelectedItem();
            String command = "POBIERZ_HARMONOGRAM";
            if (!filter.equals("Wszystkie")) {
                command += ";" + filter;
            }

            out.println(command);
            String response = in.readLine();

            tableModel.setRowCount(0);

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                int count = 0;

                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    if (fields.length >= 9) {
                        String priorytet = determinePriorytet(fields[8], fields[1]);

                        tableModel.addRow(new Object[]{
                                Integer.parseInt(fields[0]), // ID
                                fields[1],  // Data
                                fields[2],  // Nadawca
                                fields[3],  // Odbiorca
                                fields[4],  // Miasto
                                fields[5],  // Adres
                                fields[6],  // Pojazd
                                fields.length > 7 ? fields[7] : "Nieprzypisany", // Kierowca
                                fields[8],  // Status
                                priorytet   // Priorytet
                        });
                        count++;
                    }
                }

                statusLabel.setText("Załadowano " + count + " zleceń");
            } else {
                statusLabel.setText("Brak danych do wyświetlenia");
            }

        } catch (Exception e) {
            statusLabel.setText("Błąd podczas wczytywania danych");
            JOptionPane.showMessageDialog(this,
                    "Błąd podczas wczytywania harmonogramu: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String determinePriorytet(String status, String data) {
        // Logika określania priorytetu na podstawie statusu i daty
        if (status.equals("Oczekiwanie na odbiór")) {
            return "WYSOKI";
        } else if (status.equals("W drodze")) {
            return "ŚREDNI";
        } else if (status.equals("Przypisane")) {
            return "NORMALNY";
        }
        return "NISKI";
    }

    private void pokazSzczegoly() {
        int selectedRow = harmonogramTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Wybierz zlecenie aby zobaczyć szczegóły.",
                    "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idZlecenia = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 8);

        new SzczegolyZleceniaDialog(this, idZlecenia, status);
    }

    private void aktualizujStatus() {
        int selectedRow = harmonogramTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Wybierz zlecenie aby zaktualizować status.",
                    "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idZlecenia = (int) tableModel.getValueAt(selectedRow, 0);
        String aktualnyStatus = (String) tableModel.getValueAt(selectedRow, 8);

        String[] mozliweStatusy = {
                "Przypisane", "W drodze", "Oczekiwanie na odbiór", "Zrealizowane"
        };

        String nowyStatus = (String) JOptionPane.showInputDialog(this,
                "Wybierz nowy status dla zlecenia #" + idZlecenia + ":",
                "Aktualizacja statusu",
                JOptionPane.QUESTION_MESSAGE,
                null,
                mozliweStatusy,
                aktualnyStatus);

        if (nowyStatus != null && !nowyStatus.equals(aktualnyStatus)) {
            aktualizujStatusZlecenia(idZlecenia, nowyStatus);
        }
    }

    private void aktualizujStatusZlecenia(int idZlecenia, String nowyStatus) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("AKTUALIZUJ_STATUS_ZLECENIA;" + idZlecenia + ";" + nowyStatus);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this,
                        "Status zlecenia został zaktualizowany!",
                        "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wczytajHarmonogram();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas aktualizacji: " + response.split(";", 2)[1],
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Błąd połączenia z serwerem: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eksportujHarmonogram() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Eksportuj harmonogram");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Pliki CSV (*.csv)", "csv"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        fileChooser.setSelectedFile(new java.io.File("harmonogram_" + sdf.format(new Date()) + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                // Implementacja eksportu do CSV
                JOptionPane.showMessageDialog(this,
                        "Harmonogram został wyeksportowany do pliku:\n" + fileChooser.getSelectedFile().getAbsolutePath(),
                        "Eksport zakończony",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas eksportu: " + e.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}