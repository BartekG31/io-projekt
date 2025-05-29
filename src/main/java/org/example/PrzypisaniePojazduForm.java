package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class PrzypisaniePojazduForm extends JFrame {
    private JTable zleceniaTable;
    private JTable pojazdyTable;
    private DefaultTableModel zleceniaModel;
    private DefaultTableModel pojazdyModel;

    public PrzypisaniePojazduForm() {
        setTitle("Przypisanie pojazdów do zleceń");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
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
        JLabel titleLabel = new JLabel("Przypisanie pojazdów do zleceń");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel główny z dwoma tabelami
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBackground(new Color(248, 248, 248));

        // Panel zleceń
        JPanel zleceniaPanel = createTablePanel("Zlecenia do przypisania", true);
        contentPanel.add(zleceniaPanel);

        // Panel pojazdów
        JPanel pojazdyPanel = createTablePanel("Dostępne pojazdy", false);
        contentPanel.add(pojazdyPanel);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Panel przycisków
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(248, 248, 248));
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton przypiszButton = new JButton("➤ Przypisz pojazd");
        przypiszButton.setBackground(new Color(40, 167, 69));
        przypiszButton.setForeground(Color.WHITE);
        przypiszButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        przypiszButton.setFocusPainted(false);
        przypiszButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        przypiszButton.addActionListener(e -> przypiszPojazd());

        JButton odswiezButton = new JButton("🔄 Odśwież");
        odswiezButton.setBackground(new Color(70, 105, 255));
        odswiezButton.setForeground(Color.WHITE);
        odswiezButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        odswiezButton.setFocusPainted(false);
        odswiezButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        odswiezButton.addActionListener(e -> wczytajDane());

        JButton zamknijButton = new JButton("Zamknij");
        zamknijButton.setBackground(new Color(108, 117, 125));
        zamknijButton.setForeground(Color.WHITE);
        zamknijButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        zamknijButton.setFocusPainted(false);
        zamknijButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        zamknijButton.addActionListener(e -> dispose());

        buttonPanel.add(przypiszButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(odswiezButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(zamknijButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createTablePanel(String title, boolean isZlecenia) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title),
                new EmptyBorder(10, 10, 10, 10)
        ));

        if (isZlecenia) {
            zleceniaModel = new DefaultTableModel();
            zleceniaModel.addColumn("ID");
            zleceniaModel.addColumn("Nadawca");
            zleceniaModel.addColumn("Odbiorca");
            zleceniaModel.addColumn("Miasto");
            zleceniaModel.addColumn("Waga (kg)");
            zleceniaModel.addColumn("Status");

            zleceniaTable = new JTable(zleceniaModel);
            zleceniaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            zleceniaTable.getTableHeader().setReorderingAllowed(false);
            panel.add(new JScrollPane(zleceniaTable), BorderLayout.CENTER);
        } else {
            pojazdyModel = new DefaultTableModel();
            pojazdyModel.addColumn("ID");
            pojazdyModel.addColumn("Marka");
            pojazdyModel.addColumn("Model");
            pojazdyModel.addColumn("Rejestracja");
            pojazdyModel.addColumn("Status");

            pojazdyTable = new JTable(pojazdyModel);
            pojazdyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            pojazdyTable.getTableHeader().setReorderingAllowed(false);
            panel.add(new JScrollPane(pojazdyTable), BorderLayout.CENTER);
        }

        return panel;
    }

    private void wczytajDane() {
        wczytajZlecenia();
        wczytajPojazdy();
    }

    private void wczytajZlecenia() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_ZLECENIA_DO_PRZYPISANIA");
            String response = in.readLine();

            zleceniaModel.setRowCount(0);

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    zleceniaModel.addRow(new Object[]{
                            Integer.parseInt(fields[0]), // ID
                            fields[1],  // Nadawca
                            fields[2],  // Odbiorca
                            fields[3],  // Miasto
                            Double.parseDouble(fields[4]), // Waga
                            fields[5]   // Status
                    });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Błąd podczas wczytywania zleceń: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void wczytajPojazdy() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_POJAZDY");
            String response = in.readLine();

            pojazdyModel.setRowCount(0);

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    pojazdyModel.addRow(new Object[]{
                            Integer.parseInt(fields[0]), // ID
                            fields[1],  // Marka
                            fields[2],  // Model
                            fields[3],  // Rejestracja
                            fields[4]   // Status
                    });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Błąd podczas wczytywania pojazdów: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void przypiszPojazd() {
        int selectedZlecenie = zleceniaTable.getSelectedRow();
        int selectedPojazd = pojazdyTable.getSelectedRow();

        if (selectedZlecenie == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz zlecenie do przypisania.",
                    "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedPojazd == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz pojazd do przypisania.",
                    "Uwaga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idZlecenia = (int) zleceniaModel.getValueAt(selectedZlecenie, 0);
        int idPojazdu = (int) pojazdyModel.getValueAt(selectedPojazd, 0);
        String odbiorca = (String) zleceniaModel.getValueAt(selectedZlecenie, 2);
        String pojazd = pojazdyModel.getValueAt(selectedPojazd, 1) + " " +
                pojazdyModel.getValueAt(selectedPojazd, 2) + " (" +
                pojazdyModel.getValueAt(selectedPojazd, 3) + ")";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Czy chcesz przypisać pojazd:\n" + pojazd +
                        "\ndo zlecenia dla: " + odbiorca + "?",
                "Potwierdzenie przypisania",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            wykonajPrzypisanie(idZlecenia, idPojazdu);
        }
    }

    private void wykonajPrzypisanie(int idZlecenia, int idPojazdu) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("PRZYPISZ_TRASE;" + idZlecenia + ";" + idPojazdu);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this,
                        "Pojazd został pomyślnie przypisany do zlecenia!",
                        "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wczytajDane(); // Odśwież tabele
            } else {
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas przypisywania: " + response.split(";", 2)[1],
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Błąd połączenia z serwerem: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}