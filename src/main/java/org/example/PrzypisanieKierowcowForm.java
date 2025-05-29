package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class PrzypisanieKierowcowForm extends JFrame {
    private JTable pojazdyTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> kierowcaBox;
    private Map<String, Integer> kierowcyMap = new HashMap<>();

    public PrzypisanieKierowcowForm() {
        setTitle("Przypisanie kierowców do pojazdów");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeComponents();
        wczytajDane();
        setVisible(true);
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JLabel titleLabel = new JLabel("🚛👤 Przypisanie kierowców do pojazdów");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Tabela z pojazdami
        createTable();
        JScrollPane scrollPane = new JScrollPane(pojazdyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista pojazdów"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel przypisania
        JPanel assignPanel = createAssignmentPanel();
        mainPanel.add(assignPanel, BorderLayout.SOUTH);

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
        tableModel.addColumn("Marka");
        tableModel.addColumn("Model");
        tableModel.addColumn("Rejestracja");
        tableModel.addColumn("Status");
        tableModel.addColumn("Aktualny kierowca");

        pojazdyTable = new JTable(tableModel);
        pojazdyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pojazdyTable.setRowHeight(25);
        pojazdyTable.getTableHeader().setReorderingAllowed(false);

        // Ukryj kolumnę ID
        pojazdyTable.getColumnModel().getColumn(0).setMinWidth(0);
        pojazdyTable.getColumnModel().getColumn(0).setMaxWidth(0);
        pojazdyTable.getColumnModel().getColumn(0).setWidth(0);

        // Kolorowanie wierszy
        pojazdyTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String kierowca = (String) table.getValueAt(row, 5);
                    if ("Brak przypisania".equals(kierowca)) {
                        c.setBackground(new Color(255, 230, 230)); // Czerwony - bez kierowcy
                    } else {
                        c.setBackground(new Color(230, 255, 230)); // Zielony - z kierowcą
                    }
                }

                return c;
            }
        });
    }

    private JPanel createAssignmentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 248, 248));
        panel.setBorder(BorderFactory.createTitledBorder("Przypisanie kierowcy"));
        panel.setPreferredSize(new Dimension(0, 120));

        // Panel wyboru kierowcy
        JPanel kierowcaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        kierowcaPanel.setBackground(new Color(248, 248, 248));

        kierowcaPanel.add(new JLabel("Wybierz kierowcę:"));
        kierowcaBox = new JComboBox<>();
        kierowcaBox.setPreferredSize(new Dimension(200, 30));
        kierowcaPanel.add(kierowcaBox);

        // Panel przycisków
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(248, 248, 248));

        JButton przypiszButton = new JButton("👤 Przypisz kierowcę");
        przypiszButton.setBackground(new Color(40, 167, 69));
        przypiszButton.setForeground(Color.WHITE);
        przypiszButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        przypiszButton.setFocusPainted(false);
        przypiszButton.addActionListener(e -> przypiszKierowce());

        JButton usunButton = new JButton("❌ Usuń przypisanie");
        usunButton.setBackground(new Color(220, 53, 69));
        usunButton.setForeground(Color.WHITE);
        usunButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        usunButton.setFocusPainted(false);
        usunButton.addActionListener(e -> usunPrzypisanie());

        JButton odswiezButton = new JButton("🔄 Odśwież");
        odswiezButton.setBackground(new Color(70, 105, 255));
        odswiezButton.setForeground(Color.WHITE);
        odswiezButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        odswiezButton.setFocusPainted(false);
        odswiezButton.addActionListener(e -> wczytajDane());

        buttonPanel.add(przypiszButton);
        buttonPanel.add(usunButton);
        buttonPanel.add(odswiezButton);

        panel.add(kierowcaPanel);
        panel.add(buttonPanel);

        return panel;
    }

    private void wczytajDane() {
        wczytajPojazdy();
        wczytajKierowcow();
    }

    private void wczytajPojazdy() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_POJAZDY_Z_KIEROWCAMI");
            String response = in.readLine();

            tableModel.setRowCount(0);

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    if (fields.length >= 6) {
                        tableModel.addRow(new Object[]{
                                Integer.parseInt(fields[0]), // ID
                                fields[1], // Marka
                                fields[2], // Model
                                fields[3], // Rejestracja
                                fields[4], // Status
                                fields[5]  // Kierowca
                        });
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania pojazdów: " + e.getMessage());
        }
    }

    private void wczytajKierowcow() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("=== DEBUG: Próba pobrania kierowców ===");

            // Użyj komendy POBIERZ_KIEROWCOW (która już działa)
            out.println("POBIERZ_KIEROWCOW");
            String response = in.readLine();
            System.out.println("Odpowiedź serwera: " + response);

            kierowcaBox.removeAllItems();
            kierowcyMap.clear();

            kierowcaBox.addItem("-- Wybierz kierowcę --");

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                System.out.println("Liczba wpisów: " + (entries.length - 1));

                for (int i = 1; i < entries.length; i++) {
                    String kierowcaNazwa = entries[i];
                    System.out.println("Kierowca " + i + ": " + kierowcaNazwa);

                    kierowcaBox.addItem(kierowcaNazwa);
                    // Tymczasowo użyj nazwy jako klucz (potem naprawimy)
                    kierowcyMap.put(kierowcaNazwa, i); // Używamy i jako tymczasowe ID
                }

                System.out.println("Dodano " + (kierowcaBox.getItemCount() - 1) + " kierowców do ComboBox");
            } else {
                System.out.println("Błąd odpowiedzi: " + response);
                JOptionPane.showMessageDialog(this,
                        "Błąd pobierania kierowców: " + response,
                        "Debug", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia: " + e.getMessage());
        }
    }
    private void przypiszKierowce() {
        int selectedRow = pojazdyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz pojazd z listy.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String wybranyKierowca = (String) kierowcaBox.getSelectedItem();
        if (wybranyKierowca == null || wybranyKierowca.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Wybierz kierowcę.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int pojazdId = (int) tableModel.getValueAt(selectedRow, 0);
        int kierowcaId = kierowcyMap.get(wybranyKierowca);

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("PRZYPISZ_KIEROWCE_DO_POJAZDU;" + pojazdId + ";" + kierowcaId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Kierowca został przypisany do pojazdu!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wczytajPojazdy();
            } else {
                JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void usunPrzypisanie() {
        int selectedRow = pojazdyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz pojazd z listy.", "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int pojazdId = (int) tableModel.getValueAt(selectedRow, 0);
        String aktualnyKierowca = (String) tableModel.getValueAt(selectedRow, 5);

        if ("Brak przypisania".equals(aktualnyKierowca)) {
            JOptionPane.showMessageDialog(this, "Ten pojazd nie ma przypisanego kierowcy.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "Czy usunąć przypisanie kierowcy " + aktualnyKierowca + " do tego pojazdu?",
                "Potwierdzenie",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            try (Socket socket = new Socket("localhost", 5000);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("USUN_KIEROWCE_Z_POJAZDU;" + pojazdId);
                String response = in.readLine();

                if (response.startsWith("OK")) {
                    JOptionPane.showMessageDialog(this, "Przypisanie zostało usunięte!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                    wczytajPojazdy();
                } else {
                    JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Błąd połączenia: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}