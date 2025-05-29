package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PrzypisanieTrasy extends JFrame {

    private JTable zleceniaTable;
    private DefaultTableModel zleceniaModel;
    private JList<String> trasaList;
    private DefaultListModel<String> trasaListModel;
    private JComboBox<String> kierowcaBox;
    private JTextArea uwagaArea;
    private JButton dodajDoTrasyButton;
    private JButton usunZTrasyButton;
    private JButton przypisTrase;
    private List<Integer> wybraneZlecenia = new ArrayList<>();

    public PrzypisanieTrasy() {
        setTitle("Planowanie tras dla kierowców");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JLabel title = new JLabel("🗺️ Planowanie tras transportowych");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Panel główny
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(248, 248, 248));

        // Lewa strona - dostępne zlecenia
        JPanel leftPanel = createZleceniaPanel();
        centerPanel.add(leftPanel, BorderLayout.WEST);

        // Środek - przyciski akcji
        JPanel middlePanel = createActionPanel();
        centerPanel.add(middlePanel, BorderLayout.CENTER);

        // Prawa strona - planowana trasa
        JPanel rightPanel = createTrasaPanel();
        centerPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Dolny panel - przypisanie do kierowcy
        JPanel bottomPanel = createAssignmentPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        // Wczytaj dane
        wczytajGotoweZlecenia();
        wczytajKierowcow();
    }

    private JPanel createZleceniaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(450, 500));
        panel.setBorder(BorderFactory.createTitledBorder("Dostępne zlecenia do wysyłki"));
        panel.setBackground(new Color(248, 248, 248));

        // Tabela ze zleceniami
        zleceniaModel = new DefaultTableModel();
        zleceniaModel.addColumn("ID");
        zleceniaModel.addColumn("Odbiorca");
        zleceniaModel.addColumn("Miasto");
        zleceniaModel.addColumn("Adres");
        zleceniaModel.addColumn("Waga");

        zleceniaTable = new JTable(zleceniaModel);
        zleceniaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        zleceniaTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Ukryj kolumnę ID
        zleceniaTable.getColumnModel().getColumn(0).setMinWidth(0);
        zleceniaTable.getColumnModel().getColumn(0).setMaxWidth(0);
        zleceniaTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(zleceniaTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(150, 500));
        panel.setBackground(new Color(248, 248, 248));
        panel.setBorder(new EmptyBorder(100, 10, 100, 10));

        // Przycisk dodaj do trasy
        dodajDoTrasyButton = new JButton("➡️");
        dodajDoTrasyButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        dodajDoTrasyButton.setBackground(new Color(40, 167, 69));
        dodajDoTrasyButton.setForeground(Color.WHITE);
        dodajDoTrasyButton.setFocusPainted(false);
        dodajDoTrasyButton.setToolTipText("Dodaj zlecenie do planowanej trasy");
        dodajDoTrasyButton.setMaximumSize(new Dimension(100, 50));
        dodajDoTrasyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        dodajDoTrasyButton.addActionListener(e -> dodajDoTrasy());

        // Przycisk usuń z trasy
        usunZTrasyButton = new JButton("⬅️");
        usunZTrasyButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        usunZTrasyButton.setBackground(new Color(220, 53, 69));
        usunZTrasyButton.setForeground(Color.WHITE);
        usunZTrasyButton.setFocusPainted(false);
        usunZTrasyButton.setToolTipText("Usuń zlecenie z planowanej trasy");
        usunZTrasyButton.setMaximumSize(new Dimension(100, 50));
        usunZTrasyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        usunZTrasyButton.addActionListener(e -> usunZTrasy());

        panel.add(dodajDoTrasyButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(usunZTrasyButton);

        return panel;
    }

    private JPanel createTrasaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(450, 500));
        panel.setBorder(BorderFactory.createTitledBorder("Planowana trasa"));
        panel.setBackground(new Color(248, 248, 248));

        // Lista planowanych zleceń
        trasaListModel = new DefaultListModel<>();
        trasaList = new JList<>(trasaListModel);
        trasaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trasaList.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(trasaList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel ze statystykami trasy
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(new Color(248, 248, 248));
        statsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel statsLabel = new JLabel("Statystyki trasy:");
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statsPanel.add(statsLabel);

        JLabel zleceniaCountLabel = new JLabel("Zleceń: 0");
        JLabel wagaLabel = new JLabel("Łączna waga: 0.0 kg");
        JLabel szacunkowyCzasLabel = new JLabel("Szacunkowy czas: 0 h");

        statsPanel.add(zleceniaCountLabel);
        statsPanel.add(wagaLabel);
        statsPanel.add(szacunkowyCzasLabel);

        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAssignmentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 248, 248));
        panel.setBorder(BorderFactory.createTitledBorder("Przypisanie trasy do kierowcy"));
        panel.setPreferredSize(new Dimension(0, 180));

        // Panel wyboru kierowcy
        JPanel kierowcaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kierowcaPanel.setBackground(new Color(248, 248, 248));
        kierowcaPanel.add(new JLabel("Wybierz kierowcę:"));

        kierowcaBox = new JComboBox<>();
        kierowcaBox.setPreferredSize(new Dimension(200, 30));
        kierowcaPanel.add(kierowcaBox);

        // Panel uwag
        JPanel uwagaPanel = new JPanel(new BorderLayout());
        uwagaPanel.setBackground(new Color(248, 248, 248));
        uwagaPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        uwagaPanel.add(new JLabel("Uwagi do trasy:"), BorderLayout.NORTH);
        uwagaArea = new JTextArea(3, 50);
        uwagaArea.setLineWrap(true);
        uwagaArea.setWrapStyleWord(true);
        uwagaArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane uwagaScroll = new JScrollPane(uwagaArea);
        uwagaPanel.add(uwagaScroll, BorderLayout.CENTER);

        // Panel przycisków
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(248, 248, 248));

        JButton wyczyscButton = new JButton("🗑️ Wyczyść trasę");
        wyczyscButton.setBackground(new Color(108, 117, 125));
        wyczyscButton.setForeground(Color.WHITE);
        wyczyscButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        wyczyscButton.setFocusPainted(false);
        wyczyscButton.addActionListener(e -> wyczyscTrase());

        przypisTrase = new JButton("✅ Przypisz trasę kierowcy");
        przypisTrase.setBackground(new Color(40, 167, 69));
        przypisTrase.setForeground(Color.WHITE);
        przypisTrase.setFont(new Font("SansSerif", Font.BOLD, 14));
        przypisTrase.setFocusPainted(false);
        przypisTrase.addActionListener(e -> przypiszTraseKierowcy());

        buttonPanel.add(wyczyscButton);
        buttonPanel.add(przypisTrase);

        panel.add(kierowcaPanel);
        panel.add(uwagaPanel);
        panel.add(buttonPanel);

        return panel;
    }

    private void wczytajGotoweZlecenia() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_GOTOWE_DO_TRASY");
            String response = in.readLine();

            zleceniaModel.setRowCount(0);

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    if (fields.length >= 5) {
                        zleceniaModel.addRow(new Object[]{
                                Integer.parseInt(fields[0]), // ID
                                fields[1], // Odbiorca
                                fields[2], // Miasto
                                fields[3], // Adres
                                fields[4] + " kg" // Waga
                        });
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania zleceń: " + e.getMessage());
        }
    }

    private void wczytajKierowcow() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_KIEROWCOW");
            String response = in.readLine();

            kierowcaBox.removeAllItems();

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                for (int i = 1; i < entries.length; i++) {
                    kierowcaBox.addItem(entries[i]);
                }
            } else {
                kierowcaBox.addItem("Brak dostępnych kierowców");
            }

        } catch (Exception e) {
            e.printStackTrace();
            kierowcaBox.addItem("Błąd wczytywania kierowców");
        }
    }

    private void dodajDoTrasy() {
        int selectedRow = zleceniaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz zlecenie do dodania do trasy.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int zlecenieId = (int) zleceniaModel.getValueAt(selectedRow, 0);
        String odbiorca = (String) zleceniaModel.getValueAt(selectedRow, 1);
        String miasto = (String) zleceniaModel.getValueAt(selectedRow, 2);
        String adres = (String) zleceniaModel.getValueAt(selectedRow, 3);

        if (wybraneZlecenia.contains(zlecenieId)) {
            JOptionPane.showMessageDialog(this, "To zlecenie jest już dodane do trasy.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        wybraneZlecenia.add(zlecenieId);
        String trasaItem = String.format("%d. %s - %s, %s", trasaListModel.size() + 1, odbiorca, miasto, adres);
        trasaListModel.addElement(trasaItem);

        aktualizujStatystykiTrasy();
    }

    private void usunZTrasy() {
        int selectedIndex = trasaList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz pozycję do usunięcia z trasy.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        wybraneZlecenia.remove(selectedIndex);
        trasaListModel.remove(selectedIndex);

        // Przenumeruj pozostałe pozycje
        for (int i = 0; i < trasaListModel.size(); i++) {
            String item = trasaListModel.getElementAt(i);
            String newItem = (i + 1) + item.substring(item.indexOf('.'));
            trasaListModel.setElementAt(newItem, i);
        }

        aktualizujStatystykiTrasy();
    }

    private void wyczyscTrase() {
        wybraneZlecenia.clear();
        trasaListModel.clear();
        aktualizujStatystykiTrasy();
    }

    private void aktualizujStatystykiTrasy() {
        // Proste statystyki - można rozszerzyć
        int liczblaZlecen = wybraneZlecenia.size();
        double lacznaWaga = liczblaZlecen * 5.0; // Mockup
        int szacunkowyCzas = liczblaZlecen * 2;  // 2h na zlecenie

        // Aktualizuj wyświetlane statystyki (wymaga referencji do labeli)
        setTitle("Planowanie tras - Zleceń w trasie: " + liczblaZlecen);
    }

    private void przypiszTraseKierowcy() {
        if (wybraneZlecenia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dodaj przynajmniej jedno zlecenie do trasy.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String kierowca = (String) kierowcaBox.getSelectedItem();
        if (kierowca == null || "Brak dostępnych kierowców".equals(kierowca)) {
            JOptionPane.showMessageDialog(this, "Wybierz kierowcę.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String uwagi = uwagaArea.getText().trim();

        int result = JOptionPane.showConfirmDialog(this,
                String.format("Czy przypisać trasę z %d zleceniami do kierowcy:\n%s?", wybraneZlecenia.size(), kierowca),
                "Potwierdzenie przypisania",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            przypiszTraseDoKierowcy(kierowca, uwagi);
        }
    }

    private void przypiszTraseDoKierowcy(String kierowca, String uwagi) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            StringBuilder zleceniaIds = new StringBuilder();
            for (int i = 0; i < wybraneZlecenia.size(); i++) {
                if (i > 0) zleceniaIds.append(",");
                zleceniaIds.append(wybraneZlecenia.get(i));
            }

            String komenda = "PRZYPISZ_TRASE;" + kierowca + ";" + zleceniaIds.toString() + ";" + uwagi;
            out.println(komenda);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this,
                        "Trasa została pomyślnie przypisana kierowcy!",
                        "Sukces",
                        JOptionPane.INFORMATION_MESSAGE);

                wyczyscTrase();
                wczytajGotoweZlecenia(); // Odśwież listę dostępnych zleceń
            } else {
                String errorMsg = response.split(";", 2).length > 1 ? response.split(";", 2)[1] : "Nieznany błąd";
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas przypisywania trasy:\n" + errorMsg,
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}