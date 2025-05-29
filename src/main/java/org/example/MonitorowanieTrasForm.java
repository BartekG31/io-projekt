package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorowanieTrasForm extends JFrame {

    private JTable trasyTable;
    private DefaultTableModel tableModel;
    private JTextArea szczególyArea;
    private JLabel statusLabel;
    private JButton odswiezButton;
    private JButton szczególyButton;
    private JButton anulujTraseButton;
    private Timer autoRefreshTimer;
    private int wybranaTrasa = -1;

    public MonitorowanieTrasForm() {
        setTitle("🗺️ Monitorowanie tras w czasie rzeczywistym");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        // Zatrzymaj timer gdy zamykamy okno
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (autoRefreshTimer != null) {
                    autoRefreshTimer.cancel();
                }
                dispose();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek z informacjami
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel główny
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(248, 248, 248));

        // Tabela z trasami
        createTrasyTable();
        JScrollPane tableScrollPane = new JScrollPane(trasyTable);
        tableScrollPane.setPreferredSize(new Dimension(700, 400));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Aktywne trasy"));
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

        // Wczytaj dane i uruchom auto-odświeżanie
        wczytajTrasy();
        startAutoRefresh();

        // Listener do tabeli
        trasyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = trasyTable.getSelectedRow();
                if (selectedRow >= 0) {
                    pokazSzczegóły(selectedRow);
                }
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 248, 248));
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Tytuł
        JLabel title = new JLabel("🌍 Monitor Tras Transportowych");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(title, BorderLayout.CENTER);

        // Panel ze statusem
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusPanel.setBackground(new Color(248, 248, 248));

        statusLabel = new JLabel("Ładowanie danych...");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(108, 117, 125));
        statusPanel.add(new JLabel("Status: "));
        statusPanel.add(statusLabel);

        // Checkbox auto-odświeżania
        JCheckBox autoRefreshCheck = new JCheckBox("Auto-odświeżanie (30s)", true);
        autoRefreshCheck.setBackground(new Color(248, 248, 248));
        autoRefreshCheck.addActionListener(e -> {
            if (autoRefreshCheck.isSelected()) {
                startAutoRefresh();
            } else {
                stopAutoRefresh();
            }
        });
        statusPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        statusPanel.add(autoRefreshCheck);

        headerPanel.add(statusPanel, BorderLayout.SOUTH);
        return headerPanel;
    }

    private void createTrasyTable() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Kierowca");
        tableModel.addColumn("Status");
        tableModel.addColumn("Zleceń");
        tableModel.addColumn("Data rozpoczęcia");
        tableModel.addColumn("Szac. czas [h]");
        tableModel.addColumn("Postęp");

        trasyTable = new JTable(tableModel);
        trasyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trasyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        trasyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        trasyTable.setRowHeight(25);

        // Ukryj kolumnę ID
        trasyTable.getColumnModel().getColumn(0).setMinWidth(0);
        trasyTable.getColumnModel().getColumn(0).setMaxWidth(0);
        trasyTable.getColumnModel().getColumn(0).setWidth(0);

        // Ustaw szerokości kolumn
        trasyTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Kierowca
        trasyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Status
        trasyTable.getColumnModel().getColumn(3).setPreferredWidth(70);  // Zleceń
        trasyTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Data
        trasyTable.getColumnModel().getColumn(5).setPreferredWidth(90);  // Czas
        trasyTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Postęp

        // Kolorowanie wierszy według statusu
        trasyTable.setDefaultRenderer(Object.class, new TrasaStatusRenderer());
    }

    private JPanel createDetailPanel() {
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setPreferredSize(new Dimension(450, 400));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Szczegóły wybranej trasy"));
        detailPanel.setBackground(new Color(248, 248, 248));

        szczególyArea = new JTextArea();
        szczególyArea.setEditable(false);
        szczególyArea.setLineWrap(true);
        szczególyArea.setWrapStyleWord(true);
        szczególyArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        szczególyArea.setBackground(new Color(250, 250, 250));
        szczególyArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        szczególyArea.setText("Wybierz trasę z listy, aby wyświetlić szczegóły...");

        JScrollPane detailScrollPane = new JScrollPane(szczególyArea);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);

        return detailPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(248, 248, 248));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        odswiezButton = new JButton("🔄 Odśwież teraz");
        odswiezButton.setBackground(new Color(70, 105, 255));
        odswiezButton.setForeground(Color.WHITE);
        odswiezButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        odswiezButton.setFocusPainted(false);
        odswiezButton.addActionListener(e -> {
            wczytajTrasy();
            if (wybranaTrasa != -1) {
                // Odśwież szczegóły jeśli trasa była wybrana
                for (int i = 0; i < trasyTable.getRowCount(); i++) {
                    if ((int) trasyTable.getValueAt(i, 0) == wybranaTrasa) {
                        trasyTable.setRowSelectionInterval(i, i);
                        pokazSzczegóły(i);
                        break;
                    }
                }
            }
        });

        szczególyButton = new JButton("📋 Pełne szczegóły");
        szczególyButton.setBackground(new Color(40, 167, 69));
        szczególyButton.setForeground(Color.WHITE);
        szczególyButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        szczególyButton.setFocusPainted(false);
        szczególyButton.setEnabled(false);
        szczególyButton.addActionListener(e -> pokazPelneSzczegoly());

        anulujTraseButton = new JButton("❌ Anuluj trasę");
        anulujTraseButton.setBackground(new Color(220, 53, 69));
        anulujTraseButton.setForeground(Color.WHITE);
        anulujTraseButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        anulujTraseButton.setFocusPainted(false);
        anulujTraseButton.setEnabled(false);
        anulujTraseButton.addActionListener(e -> anulujTrase());

        JButton mapButton = new JButton("🗺️ Pokaż na mapie");
        mapButton.setBackground(new Color(255, 193, 7));
        mapButton.setForeground(Color.BLACK);
        mapButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        mapButton.setFocusPainted(false);
        mapButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Integracja z mapami w trakcie rozwoju", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton eksportButton = new JButton("📊 Eksportuj raport");
        eksportButton.setBackground(new Color(108, 117, 125));
        eksportButton.setForeground(Color.WHITE);
        eksportButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        eksportButton.setFocusPainted(false);
        eksportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Funkcja eksportu w trakcie rozwoju", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(odswiezButton);
        buttonPanel.add(szczególyButton);
        buttonPanel.add(anulujTraseButton);
        buttonPanel.add(mapButton);
        buttonPanel.add(eksportButton);

        return buttonPanel;
    }

    private void wczytajTrasy() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_AKTYWNE_TRASY");
            String response = in.readLine();

            tableModel.setRowCount(0);

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                int aktywne = 0, wTrakcie = 0, przypisane = 0;

                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    if (fields.length >= 7) {
                        String status = fields[2];
                        switch (status) {
                            case "Przypisana": przypisane++; break;
                            case "W trakcie": wTrakcie++; break;
                            default: aktywne++; break;
                        }

                        // Oblicz postęp jako procent (mockup)
                        String postep = obliczPostep(status, fields[4], fields[5]);

                        tableModel.addRow(new Object[]{
                                Integer.parseInt(fields[0]), // ID
                                fields[1], // Kierowca
                                status,    // Status
                                fields[3], // Liczba zleceń
                                fields[4] != null && !fields[4].equals("null") ? fields[4] : "Nie rozpoczęta", // Data rozpoczęcia
                                fields[5] != null && !fields[5].equals("null") ? formatCzas(fields[5]) : "N/A", // Szacunkowy czas
                                postep     // Postęp
                        });
                    }
                }

                // Aktualizuj status
                int total = entries.length - 1;
                statusLabel.setText(String.format("Łącznie tras: %d (Przypisane: %d, W trakcie: %d, Inne: %d) • Ostatnia aktualizacja: %s",
                        total, przypisane, wTrakcie, aktywne, java.time.LocalTime.now().toString().substring(0, 8)));

                statusLabel.setForeground(new Color(40, 167, 69));
            } else {
                statusLabel.setText("Brak aktywnych tras w systemie");
                statusLabel.setForeground(new Color(108, 117, 125));
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Błąd połączenia z serwerem");
            statusLabel.setForeground(new Color(220, 53, 69));
        }
    }

    private void pokazSzczegóły(int row) {
        wybranaTrasa = (int) tableModel.getValueAt(row, 0);

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_SZCZEGOLY_TRASY;" + wybranaTrasa);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] details = response.split(";", 2);
                if (details.length > 1) {
                    formatujSzczegoly(details[1]);
                }
            } else {
                szczególyArea.setText("Błąd podczas ładowania szczegółów trasy.");
            }

        } catch (Exception e) {
            szczególyArea.setText("Błąd połączenia z serwerem podczas ładowania szczegółów.");
        }

        // Włącz przyciski
        szczególyButton.setEnabled(true);
        String status = (String) tableModel.getValueAt(row, 2);
        anulujTraseButton.setEnabled(!"Zakończona".equals(status) && !"Anulowana".equals(status));
    }

    private void formatujSzczegoly(String rawDetails) {
        String[] fields = rawDetails.split("\\|");

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("         INFORMACJE O TRASIE\n");
        sb.append("═══════════════════════════════════════\n\n");

        sb.append("🆔 ID Trasy: ").append(wybranaTrasa).append("\n");
        sb.append("👤 Kierowca: ").append(fields[0]).append("\n");
        sb.append("📋 Status: ").append(fields[1]).append("\n");
        sb.append("📦 Liczba zleceń: ").append(fields[2]).append("\n\n");

        sb.append("⏰ HARMONOGRAM:\n");
        sb.append("   • Data utworzenia: ").append(fields[3]).append("\n");
        sb.append("   • Data rozpoczęcia: ").append(fields[4] != null && !fields[4].equals("null") ? fields[4] : "Nie rozpoczęta").append("\n");
        sb.append("   • Szacunkowy czas: ").append(fields[5] != null ? formatCzas(fields[5]) : "N/A").append("\n");

        if (fields.length > 6 && fields[6] != null && !fields[6].isEmpty()) {
            sb.append("   • Data zakończenia: ").append(fields[6]).append("\n");
        }

        sb.append("\n📝 UWAGI:\n");
        sb.append(fields.length > 7 && fields[7] != null && !fields[7].equals("null") ? fields[7] : "Brak uwag");

        if (fields.length > 8 && fields[8] != null && !fields[8].isEmpty()) {
            sb.append("\n\n🚛 INFORMACJE O POJEŹDZIE:\n");
            sb.append(fields[8]);
        }

        szczególyArea.setText(sb.toString());
        szczególyArea.setCaretPosition(0);
    }

    private void pokazPelneSzczegoly() {
        if (wybranaTrasa != -1) {
            new SzczegolyTrasyDialog(this, wybranaTrasa);
        }
    }

    private void anulujTrase() {
        if (wybranaTrasa == -1) return;

        int result = JOptionPane.showConfirmDialog(this,
                "Czy na pewno chcesz anulować trasę ID: " + wybranaTrasa + "?\n" +
                        "Spowoduje to zwolnienie wszystkich przypisanych zleceń.",
                "Potwierdzenie anulowania trasy",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            anulujTraseWSystemie();
        }
    }

    private void anulujTraseWSystemie() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("ANULUJ_TRASE;" + wybranaTrasa);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Trasa została anulowana!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wczytajTrasy();
                szczególyArea.setText("Wybierz trasę z listy, aby wyświetlić szczegóły...");
                wybranaTrasa = -1;
                szczególyButton.setEnabled(false);
                anulujTraseButton.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startAutoRefresh() {
        stopAutoRefresh(); // Zatrzymaj poprzedni timer
        autoRefreshTimer = new Timer();
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> wczytajTrasy());
            }
        }, 30000, 30000); // Co 30 sekund
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer = null;
        }
    }

    private String obliczPostep(String status, String dataRozp, String szacCzas) {
        switch (status) {
            case "Przypisana": return "0% (Oczekuje)";
            case "W trakcie": return "~50% (W drodze)";
            case "Zakończona": return "100% (Ukończona)";
            case "Anulowana": return "Anulowana";
            default: return "N/A";
        }
    }

    private String formatCzas(String minuty) {
        try {
            int min = Integer.parseInt(minuty);
            int godziny = min / 60;
            int pozostaleMin = min % 60;
            return godziny + "h " + pozostaleMin + "m";
        } catch (NumberFormatException e) {
            return minuty;
        }
    }

    // Renderer do kolorowania wierszy
    private static class TrasaStatusRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                String status = (String) table.getValueAt(row, 2);
                switch (status) {
                    case "Przypisana":
                        c.setBackground(new Color(255, 248, 225)); // Jasny żółty
                        break;
                    case "W trakcie":
                        c.setBackground(new Color(225, 245, 254)); // Jasny niebieski
                        break;
                    case "Zakończona":
                        c.setBackground(new Color(230, 255, 230)); // Jasny zielony
                        break;
                    case "Anulowana":
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