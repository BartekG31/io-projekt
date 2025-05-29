package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class PrzegladIncydentowForm extends JFrame {

    private JTable incydentTable;
    private DefaultTableModel tableModel;
    private JTextArea szczególyArea;
    private JButton oznaczJakoRozwiazanyButton;
    private JButton usunIncydentButton;
    private int wybranyIncydentId = -1;

    public PrzegladIncydentowForm() {
        setTitle("Przegląd incydentów");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JLabel title = new JLabel("🚨 Przegląd incydentów zgłoszonych przez kurierów");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Panel główny
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(248, 248, 248));

        // Tabela z incydentami
        createIncydentTable();
        JScrollPane tableScrollPane = new JScrollPane(incydentTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 400));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Lista incydentów"));
        centerPanel.add(tableScrollPane, BorderLayout.WEST);

        // Panel szczegółów
        JPanel detailPanel = createDetailPanel();
        centerPanel.add(detailPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panel przycisków
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        // Wczytaj incydenty
        wczytajIncydenty();

        // Listener do tabeli
        incydentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = incydentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    pokazSzczegóły(selectedRow);
                }
            }
        });
    }

    private void createIncydentTable() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Typ incydentu");
        tableModel.addColumn("Lokalizacja");
        tableModel.addColumn("Data zgłoszenia");
        tableModel.addColumn("Status");

        incydentTable = new JTable(tableModel);
        incydentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        incydentTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        incydentTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Ukryj kolumnę ID
        incydentTable.getColumnModel().getColumn(0).setMinWidth(0);
        incydentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        incydentTable.getColumnModel().getColumn(0).setWidth(0);

        // Ustaw szerokości kolumn
        incydentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        incydentTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        incydentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        incydentTable.getColumnModel().getColumn(4).setPreferredWidth(100);
    }

    private JPanel createDetailPanel() {
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBackground(new Color(248, 248, 248));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Szczegóły incydentu"));
        detailPanel.setPreferredSize(new Dimension(350, 400));

        szczególyArea = new JTextArea();
        szczególyArea.setEditable(false);
        szczególyArea.setLineWrap(true);
        szczególyArea.setWrapStyleWord(true);
        szczególyArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        szczególyArea.setBackground(new Color(250, 250, 250));
        szczególyArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane detailScrollPane = new JScrollPane(szczególyArea);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);

        return detailPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(248, 248, 248));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton odswiezButton = new JButton("🔄 Odśwież listę");
        odswiezButton.setBackground(new Color(70, 105, 255));
        odswiezButton.setForeground(Color.WHITE);
        odswiezButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        odswiezButton.setFocusPainted(false);
        odswiezButton.addActionListener(e -> {
            wczytajIncydenty();
            szczególyArea.setText("");
            wybranyIncydentId = -1;
            oznaczJakoRozwiazanyButton.setEnabled(false);
            usunIncydentButton.setEnabled(false);
        });

        oznaczJakoRozwiazanyButton = new JButton("✅ Oznacz jako rozwiązany");
        oznaczJakoRozwiazanyButton.setBackground(new Color(40, 167, 69));
        oznaczJakoRozwiazanyButton.setForeground(Color.WHITE);
        oznaczJakoRozwiazanyButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        oznaczJakoRozwiazanyButton.setFocusPainted(false);
        oznaczJakoRozwiazanyButton.setEnabled(false);
        oznaczJakoRozwiazanyButton.addActionListener(e -> oznaczJakoRozwiazany());

        usunIncydentButton = new JButton("🗑️ Usuń incydent");
        usunIncydentButton.setBackground(new Color(220, 53, 69));
        usunIncydentButton.setForeground(Color.WHITE);
        usunIncydentButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        usunIncydentButton.setFocusPainted(false);
        usunIncydentButton.setEnabled(false);
        usunIncydentButton.addActionListener(e -> usunIncydent());

        JButton eksportButton = new JButton("📊 Eksportuj raport");
        eksportButton.setBackground(new Color(255, 193, 7));
        eksportButton.setForeground(Color.BLACK);
        eksportButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        eksportButton.setFocusPainted(false);
        eksportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Funkcja eksportu w trakcie rozwoju", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(odswiezButton);
        buttonPanel.add(oznaczJakoRozwiazanyButton);
        buttonPanel.add(usunIncydentButton);
        buttonPanel.add(eksportButton);

        return buttonPanel;
    }

    private void wczytajIncydenty() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_INCYDENTY");
            String response = in.readLine();

            // Wyczyść tabelę
            tableModel.setRowCount(0);

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    if (fields.length >= 5) {
                        // Formatuj datę dla lepszej czytelności
                        String dataFormatowana = formatujDate(fields[4]);

                        tableModel.addRow(new Object[]{
                                Integer.parseInt(fields[0]), // ID
                                fields[1], // Typ incydentu
                                fields[2], // Lokalizacja
                                dataFormatowana, // Data zgłoszenia
                                fields.length > 5 ? fields[5] : "Nowy" // Status
                        });
                    }
                }

                // Pokaż statystyki
                pokazStatystyki(entries.length - 1);
            } else if (!response.equals("ERROR;Brak incydentów w systemie")) {
                JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania incydentów: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem podczas wczytywania incydentów.");
        }
    }

    private void pokazSzczegóły(int row) {
        wybranyIncydentId = (int) tableModel.getValueAt(row, 0);

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_SZCZEGOLY_INCYDENTU;" + wybranyIncydentId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] details = response.split(";", 2);
                if (details.length > 1) {
                    String[] fields = details[1].split("\\|");

                    StringBuilder szczegóły = new StringBuilder();
                    szczegóły.append("🆔 ID Incydentu: ").append(wybranyIncydentId).append("\n\n");
                    szczegóły.append("📝 Typ incydentu: ").append(fields[0]).append("\n\n");
                    szczegóły.append("📍 Lokalizacja: ").append(fields[1]).append("\n\n");
                    szczegóły.append("📅 Data zgłoszenia: ").append(formatujDate(fields[2])).append("\n\n");
                    szczegóły.append("📋 Status: ").append(fields.length > 4 ? fields[4] : "Nowy").append("\n\n");
                    szczegóły.append("📄 Szczegółowy opis:\n").append(fields[3]);

                    szczególyArea.setText(szczegóły.toString());
                    szczególyArea.setCaretPosition(0);
                }
            }

        } catch (Exception e) {
            szczególyArea.setText("Błąd podczas wczytywania szczegółów incydentu.");
        }

        // Włącz przyciski
        String status = (String) tableModel.getValueAt(row, 4);
        oznaczJakoRozwiazanyButton.setEnabled(!"Rozwiązany".equals(status));
        usunIncydentButton.setEnabled(true);
    }

    private void oznaczJakoRozwiazany() {
        if (wybranyIncydentId == -1) return;

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("OZNACZ_INCYDENT_ROZWIAZANY;" + wybranyIncydentId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Incydent oznaczony jako rozwiązany!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wczytajIncydenty();
                szczególyArea.setText("");
                wybranyIncydentId = -1;
                oznaczJakoRozwiazanyButton.setEnabled(false);
                usunIncydentButton.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void usunIncydent() {
        if (wybranyIncydentId == -1) return;

        int result = JOptionPane.showConfirmDialog(this,
                "Czy na pewno chcesz usunąć ten incydent?",
                "Potwierdzenie usunięcia",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.YES_OPTION) return;

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("USUN_INCYDENT;" + wybranyIncydentId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Incydent został usunięty!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wczytajIncydenty();
                szczególyArea.setText("");
                wybranyIncydentId = -1;
                oznaczJakoRozwiazanyButton.setEnabled(false);
                usunIncydentButton.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pokazStatystyki(int liczbincydentów) {
        if (liczbincydentów == 0) {
            setTitle("Przegląd incydentów - Brak incydentów");
        } else {
            // Policz incydenty według statusu
            int nowe = 0, rozwiazane = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String status = (String) tableModel.getValueAt(i, 4);
                if ("Rozwiązany".equals(status)) {
                    rozwiazane++;
                } else {
                    nowe++;
                }
            }
            setTitle(String.format("Przegląd incydentów - Łącznie: %d (Nowe: %d, Rozwiązane: %d)",
                    liczbincydentów, nowe, rozwiazane));
        }
    }

    private String formatujDate(String data) {
        // Proste formatowanie daty - można rozszerzyć
        return data.replace("-", "/").substring(0, Math.min(16, data.length()));
    }
}