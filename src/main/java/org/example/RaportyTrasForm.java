package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;

public class RaportyTrasForm extends JFrame {
    private JTable raportyTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JLabel statsLabel;

    public RaportyTrasForm() {
        setTitle("Raporty tras i analiza kierowców");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeComponents();
        wczytajRaporty();
        setVisible(true);
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 248, 248));

        JLabel titleLabel = new JLabel("🚛 Raporty tras i analiza kierowców");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Panel statystyk
        statsLabel = new JLabel("Statystyki będą dostępne po załadowaniu danych");
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 20, 10, 20)
        ));
        statsLabel.setBackground(Color.WHITE);
        statsLabel.setOpaque(true);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statsLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabela raportów
        createTable();
        JScrollPane scrollPane = new JScrollPane(raportyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista raportów tras"));
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

        JButton szczegolyButton = new JButton("📋 Szczegóły raportu");
        szczegolyButton.setBackground(new Color(70, 105, 255));
        szczegolyButton.setForeground(Color.WHITE);
        szczegolyButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        szczegolyButton.setFocusPainted(false);
        szczegolyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        szczegolyButton.addActionListener(e -> pokazSzczegolyRaportu());

        JButton analizaButton = new JButton("📊 Analiza kierowcy");
        analizaButton.setBackground(new Color(255, 193, 7));
        analizaButton.setForeground(Color.BLACK);
        analizaButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        analizaButton.setFocusPainted(false);
        analizaButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        analizaButton.addActionListener(e -> pokazAnalizeKierowcy());

        JButton odswiezButton = new JButton("🔄 Odśwież");
        odswiezButton.setBackground(new Color(40, 167, 69));
        odswiezButton.setForeground(Color.WHITE);
        odswiezButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        odswiezButton.setFocusPainted(false);
        odswiezButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        odswiezButton.addActionListener(e -> wczytajRaporty());

        JButton eksportButton = new JButton("📊 Eksportuj wszystkie");
        eksportButton.setBackground(new Color(108, 117, 125));
        eksportButton.setForeground(Color.WHITE);
        eksportButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        eksportButton.setFocusPainted(false);
        eksportButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eksportButton.addActionListener(e -> eksportujWszystkieRaporty());

        buttonPanel.add(szczegolyButton);
        buttonPanel.add(analizaButton);
        buttonPanel.add(odswiezButton);
        buttonPanel.add(eksportButton);

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
        tableModel.addColumn("Data zakończenia");
        tableModel.addColumn("Kierowca");
        tableModel.addColumn("Kilometry");
        tableModel.addColumn("Paliwo (l)");
        tableModel.addColumn("Spalanie (l/100km)");
        tableModel.addColumn("Czas trasy");
        tableModel.addColumn("Ocena");

        raportyTable = new JTable(tableModel);
        raportyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        raportyTable.setRowHeight(25);
        raportyTable.getTableHeader().setReorderingAllowed(false);

        // Ustawienia kolumn
        raportyTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        raportyTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Data
        raportyTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Kierowca
        raportyTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Kilometry
        raportyTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Paliwo
        raportyTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Spalanie
        raportyTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Czas
        raportyTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Ocena

        // Kolorowanie wierszy według oceny spalania
        raportyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && table.getRowCount() > row) {
                    try {
                        String spalanie = table.getValueAt(row, 5).toString();
                        double spalanieValue = Double.parseDouble(spalanie.replace(" l/100km", ""));

                        if (spalanieValue <= 8.0) {
                            c.setBackground(new Color(230, 255, 230)); // Zielony - ekonomiczny
                        } else if (spalanieValue <= 12.0) {
                            c.setBackground(new Color(255, 248, 220)); // Żółty - średni
                        } else {
                            c.setBackground(new Color(255, 230, 230)); // Czerwony - wysoki
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });
    }

    private void wczytajRaporty() {
        statusLabel.setText("Ładowanie raportów tras...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                pobierzRaportyTras();
                return null;
            }

            @Override
            protected void done() {
                statusLabel.setText("Raporty tras załadowane");
                obliczStatystyki();
            }
        };

        worker.execute();
    }

    private void pobierzRaportyTras() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_RAPORTY_TRAS");
            String response = in.readLine();

            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);

                if (response.startsWith("OK")) {
                    String[] entries = response.split(";");

                    for (int i = 1; i < entries.length; i++) {
                        String[] fields = entries[i].split("\\|");
                        if (fields.length >= 5) {
                            try {
                                int id = Integer.parseInt(fields[0]);
                                int kilometry = Integer.parseInt(fields[1]);
                                double paliwo = Double.parseDouble(fields[2]);
                                String raport = fields[3];
                                String dataZakonczenia = fields[4];

                                // Oblicz spalanie
                                double spalanie = kilometry > 0 ? (paliwo / kilometry) * 100 : 0;
                                DecimalFormat df = new DecimalFormat("#0.0");

                                // Określ ocenę spalania
                                String ocena;
                                if (spalanie <= 8.0) ocena = "BARDZO DOBRA";
                                else if (spalanie <= 10.0) ocena = "DOBRA";
                                else if (spalanie <= 12.0) ocena = "ŚREDNIA";
                                else ocena = "WYSOKA";

                                // Symulacja kierowcy i czasu (w rzeczywistej aplikacji z bazy danych)
                                String kierowca = "Kierowca " + (i % 5 + 1);
                                String czasTrasy = estimateRouteTime(kilometry);

                                tableModel.addRow(new Object[]{
                                        id,
                                        dataZakonczenia,
                                        kierowca,
                                        kilometry + " km",
                                        df.format(paliwo) + " l",
                                        df.format(spalanie) + " l/100km",
                                        czasTrasy,
                                        ocena
                                });
                            } catch (NumberFormatException e) {
                                System.out.println("Błąd parsowania danych: " + e.getMessage());
                            }
                        }
                    }
                }
            });

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Błąd podczas wczytywania raportów: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas wczytywania raportów tras: " + e.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private String estimateRouteTime(int kilometry) {
        // Symulacja czasu trasy na podstawie kilometrów (średnia prędkość 50 km/h)
        int minuty = (int) (kilometry * 1.2); // 1.2 min/km średnio
        int godziny = minuty / 60;
        int pozostaleMinuty = minuty % 60;

        if (godziny > 0) {
            return godziny + "h " + pozostaleMinuty + "min";
        } else {
            return pozostaleMinuty + "min";
        }
    }

    private void obliczStatystyki() {
        if (tableModel.getRowCount() == 0) {
            statsLabel.setText("Brak danych do analizy");
            return;
        }

        int totalTras = tableModel.getRowCount();
        double totalKilometry = 0;
        double totalPaliwo = 0;
        int bardzo_dobre = 0, dobre = 0, srednie = 0, wysokie = 0;

        for (int i = 0; i < totalTras; i++) {
            try {
                String kmStr = tableModel.getValueAt(i, 3).toString().replace(" km", "");
                String paliwoStr = tableModel.getValueAt(i, 4).toString().replace(" l", "");
                String ocena = tableModel.getValueAt(i, 7).toString();

                totalKilometry += Double.parseDouble(kmStr);
                totalPaliwo += Double.parseDouble(paliwoStr);

                switch (ocena) {
                    case "BARDZO DOBRA": bardzo_dobre++; break;
                    case "DOBRA": dobre++; break;
                    case "ŚREDNIA": srednie++; break;
                    case "WYSOKA": wysokie++; break;
                }
            } catch (Exception e) {
                // Ignoruj błędne dane
            }
        }

        double srednieSpalanie = totalKilometry > 0 ? (totalPaliwo / totalKilometry) * 100 : 0;
        DecimalFormat df = new DecimalFormat("#,##0");
        DecimalFormat df2 = new DecimalFormat("#0.0");

        String statystyki = String.format(
                "📊 Statystyki: %d tras | %s km | %s l paliwa | Średnie spalanie: %s l/100km | " +
                        "Oceny: 🟢%d 🟡%d 🟠%d 🔴%d",
                totalTras,
                df.format(totalKilometry),
                df.format(totalPaliwo),
                df2.format(srednieSpalanie),
                bardzo_dobre, dobre, srednie, wysokie
        );

        statsLabel.setText(statystyki);
    }

    private void pokazSzczegolyRaportu() {
        int selectedRow = raportyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Wybierz raport aby zobaczyć szczegóły.",
                    "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idRaportu = (int) tableModel.getValueAt(selectedRow, 0);
        new SzczegolyTrasyDialog(this, idRaportu);
    }

    private void pokazAnalizeKierowcy() {
        int selectedRow = raportyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Wybierz raport aby zobaczyć analizę kierowcy.",
                    "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kierowca = (String) tableModel.getValueAt(selectedRow, 2);
        pokazAnalizeWydajnosciKierowcy(kierowca);
    }

    private void pokazAnalizeWydajnosciKierowcy(String kierowca) {
        // Zbierz dane dla wybranego kierowcy
        StringBuilder analiza = new StringBuilder();
        analiza.append("═══════════════════════════════════════════════════════════════\n");
        analiza.append("           ANALIZA WYDAJNOŚCI KIEROWCY\n");
        analiza.append("═══════════════════════════════════════════════════════════════\n");
        analiza.append("Kierowca: ").append(kierowca).append("\n");
        analiza.append("Data analizy: ").append(java.time.LocalDate.now()).append("\n\n");

        int trasyKierowcy = 0;
        double totalKm = 0, totalPaliwo = 0;
        int bardzo_dobre = 0, dobre = 0, srednie = 0, wysokie = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 2).toString().equals(kierowca)) {
                trasyKierowcy++;
                try {
                    String kmStr = tableModel.getValueAt(i, 3).toString().replace(" km", "");
                    String paliwoStr = tableModel.getValueAt(i, 4).toString().replace(" l", "");
                    String ocena = tableModel.getValueAt(i, 7).toString();

                    totalKm += Double.parseDouble(kmStr);
                    totalPaliwo += Double.parseDouble(paliwoStr);

                    switch (ocena) {
                        case "BARDZO DOBRA": bardzo_dobre++; break;
                        case "DOBRA": dobre++; break;
                        case "ŚREDNIA": srednie++; break;
                        case "WYSOKA": wysokie++; break;
                    }
                } catch (Exception e) {
                    // Ignoruj błędne dane
                }
            }
        }

        DecimalFormat df = new DecimalFormat("#,##0");
        DecimalFormat df2 = new DecimalFormat("#0.0");
        double srednieSpalanie = totalKm > 0 ? (totalPaliwo / totalKm) * 100 : 0;

        analiza.append("📊 PODSUMOWANIE TRAS:\n");
        analiza.append("────────────────────────────────────────────────────────────────\n");
        analiza.append(String.format("• Liczba tras:           %,6d\n", trasyKierowcy));
        analiza.append(String.format("• Łączne kilometry:      %s km\n", df.format(totalKm)));
        analiza.append(String.format("• Łączne zużycie paliwa: %s l\n", df.format(totalPaliwo)));
        analiza.append(String.format("• Średnie spalanie:      %s l/100km\n", df2.format(srednieSpalanie)));
        analiza.append(String.format("• Średnio km na trasę:   %s km\n", df.format(trasyKierowcy > 0 ? totalKm/trasyKierowcy : 0)));
        analiza.append("\n");

        analiza.append("🎯 OCENA EFEKTYWNOŚCI:\n");
        analiza.append("────────────────────────────────────────────────────────────────\n");
        analiza.append(String.format("• Bardzo dobre trasy:    %d (%.1f%%)\n",
                bardzo_dobre, trasyKierowcy > 0 ? 100.0*bardzo_dobre/trasyKierowcy : 0));
        analiza.append(String.format("• Dobre trasy:           %d (%.1f%%)\n",
                dobre, trasyKierowcy > 0 ? 100.0*dobre/trasyKierowcy : 0));
        analiza.append(String.format("• Średnie trasy:         %d (%.1f%%)\n",
                srednie, trasyKierowcy > 0 ? 100.0*srednie/trasyKierowcy : 0));
        analiza.append(String.format("• Wysokie spalanie:      %d (%.1f%%)\n",
                wysokie, trasyKierowcy > 0 ? 100.0*wysokie/trasyKierowcy : 0));
        analiza.append("\n");

        // Ocena ogólna
        analiza.append("⭐ OCENA OGÓLNA:\n");
        analiza.append("────────────────────────────────────────────────────────────────\n");
        double procentDobrych = trasyKierowcy > 0 ? 100.0*(bardzo_dobre + dobre)/trasyKierowcy : 0;

        String ocenaOgolna;
        if (procentDobrych >= 80) {
            ocenaOgolna = "DOSKONAŁY KIEROWCA ⭐⭐⭐⭐⭐";
        } else if (procentDobrych >= 60) {
            ocenaOgolna = "DOBRY KIEROWCA ⭐⭐⭐⭐";
        } else if (procentDobrych >= 40) {
            ocenaOgolna = "PRZECIĘTNY KIEROWCA ⭐⭐⭐";
        } else {
            ocenaOgolna = "WYMAGA SZKOLENIA ⭐⭐";
        }

        analiza.append("• Status: ").append(ocenaOgolna).append("\n");

        analiza.append("\n📋 REKOMENDACJE:\n");
        analiza.append("────────────────────────────────────────────────────────────────\n");
        if (srednieSpalanie > 10) {
            analiza.append("• Zaleca się szkolenie z ekonomicznej jazdy\n");
            analiza.append("• Kontrola stanu technicznego pojazdu\n");
        }
        if (wysokie > 0) {
            analiza.append("• Analiza tras o wysokim spalaniu\n");
        }
        if (procentDobrych >= 80) {
            analiza.append("• Doskonałe wyniki! Kierowca może być mentorem\n");
        }

        analiza.append("\n═══════════════════════════════════════════════════════════════\n");

        // Pokaż w oknie dialogowym
        JTextArea textArea = new JTextArea(analiza.toString());
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Analiza wydajności - " + kierowca,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void eksportujWszystkieRaporty() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Brak danych do eksportu.",
                    "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Eksportuj raporty tras");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Pliki CSV (*.csv)", "csv"));
        fileChooser.setSelectedFile(new java.io.File("raporty_tras_" +
                java.time.LocalDate.now().toString() + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                // Nagłówki CSV
                writer.write("ID,Data zakończenia,Kierowca,Kilometry,Paliwo (l),Spalanie (l/100km),Czas trasy,Ocena\n");

                // Dane
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        writer.write(tableModel.getValueAt(i, j).toString());
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(this,
                        "Raporty zostały wyeksportowane do pliku:\n" + fileChooser.getSelectedFile().getAbsolutePath(),
                        "Eksport zakończony",
                        JOptionPane.INFORMATION_MESSAGE);

                statusLabel.setText("Eksport zakończony pomyślnie");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas eksportu: " + e.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}