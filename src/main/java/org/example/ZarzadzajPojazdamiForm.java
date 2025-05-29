package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ZarzadzajPojazdamiForm extends JFrame {

    private JTable pojazdyTable;
    private DefaultTableModel tableModel;
    private JTextField markaField;
    private JTextField modelField;
    private JTextField rejestracjaField;
    private JComboBox<String> statusBox;
    private JTextArea uwagaArea;
    private JButton dodajButton;
    private JButton edytujButton;
    private JButton usunButton;
    private int wybranyPojazdId = -1;

    public ZarzadzajPojazdamiForm() {
        setTitle("Zarządzanie flotą pojazdów");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JLabel title = new JLabel("Zarządzanie flotą pojazdów");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Panel główny z tabelą i formularzem
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(248, 248, 248));

        // Tabela z pojazdami
        createTable();
        JScrollPane tableScrollPane = new JScrollPane(pojazdyTable);
        tableScrollPane.setPreferredSize(new Dimension(500, 300));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Lista pojazdów"));
        centerPanel.add(tableScrollPane, BorderLayout.WEST);

        // Formularz do dodawania/edycji
        JPanel formPanel = createFormPanel();
        centerPanel.add(formPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panel z przyciskami akcji
        JPanel buttonPanel = createActionButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        // Wczytaj dane pojazdów
        wczytajPojazdy();

        // Dodaj listener do tabeli
        pojazdyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = pojazdyTable.getSelectedRow();
                if (selectedRow >= 0) {
                    wypelnijFormularz(selectedRow);
                }
            }
        });
    }

    private void createTable() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Marka");
        tableModel.addColumn("Model");
        tableModel.addColumn("Rejestracja");
        tableModel.addColumn("Status");
        tableModel.addColumn("Uwagi");

        pojazdyTable = new JTable(tableModel);
        pojazdyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pojazdyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pojazdyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Ukryj kolumnę ID
        pojazdyTable.getColumnModel().getColumn(0).setMinWidth(0);
        pojazdyTable.getColumnModel().getColumn(0).setMaxWidth(0);
        pojazdyTable.getColumnModel().getColumn(0).setWidth(0);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(new Color(248, 248, 248));
        formPanel.setBorder(BorderFactory.createTitledBorder("Dane pojazdu"));
        formPanel.setPreferredSize(new Dimension(350, 300));

        // Marka
        formPanel.add(new JLabel("Marka:"));
        markaField = new JTextField();
        markaField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        formPanel.add(markaField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Model
        formPanel.add(new JLabel("Model:"));
        modelField = new JTextField();
        modelField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        formPanel.add(modelField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Rejestracja
        formPanel.add(new JLabel("Numer rejestracyjny:"));
        rejestracjaField = new JTextField();
        rejestracjaField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        formPanel.add(rejestracjaField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Status
        formPanel.add(new JLabel("Status:"));
        statusBox = new JComboBox<>(new String[]{"Dostępny", "Zajęty", "W naprawie", "Nieaktywny"});
        statusBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        formPanel.add(statusBox);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Uwagi
        formPanel.add(new JLabel("Uwagi:"));
        uwagaArea = new JTextArea(4, 20);
        uwagaArea.setLineWrap(true);
        uwagaArea.setWrapStyleWord(true);
        JScrollPane uwagaScroll = new JScrollPane(uwagaArea);
        uwagaScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        formPanel.add(uwagaScroll);

        return formPanel;
    }

    private JPanel createActionButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(248, 248, 248));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        dodajButton = new JButton("Dodaj pojazd");
        dodajButton.setBackground(new Color(40, 167, 69));
        dodajButton.setForeground(Color.WHITE);
        dodajButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        dodajButton.setFocusPainted(false);
        dodajButton.addActionListener(e -> dodajPojazd());

        edytujButton = new JButton("Zapisz zmiany");
        edytujButton.setBackground(new Color(255, 193, 7));
        edytujButton.setForeground(Color.BLACK);
        edytujButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        edytujButton.setFocusPainted(false);
        edytujButton.setEnabled(false);
        edytujButton.addActionListener(e -> edytujPojazd());

        usunButton = new JButton("Usuń pojazd");
        usunButton.setBackground(new Color(220, 53, 69));
        usunButton.setForeground(Color.WHITE);
        usunButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        usunButton.setFocusPainted(false);
        usunButton.setEnabled(false);
        usunButton.addActionListener(e -> usunPojazd());

        JButton wyczyscButton = new JButton("Wyczyść formularz");
        wyczyscButton.setBackground(new Color(108, 117, 125));
        wyczyscButton.setForeground(Color.WHITE);
        wyczyscButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        wyczyscButton.setFocusPainted(false);
        wyczyscButton.addActionListener(e -> wyczyscFormularz());

        buttonPanel.add(dodajButton);
        buttonPanel.add(edytujButton);
        buttonPanel.add(usunButton);
        buttonPanel.add(wyczyscButton);

        return buttonPanel;
    }

    private void wczytajPojazdy() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_POJAZDY");
            String response = in.readLine();

            // Wyczyść tabelę
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
                                fields[5]  // Uwagi
                        });
                    }
                }
            } else if (!response.equals("ERROR;Brak pojazdów w systemie")) {
                JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania pojazdów: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem podczas wczytywania pojazdów.");
        }
    }

    private void wypelnijFormularz(int row) {
        wybranyPojazdId = (int) tableModel.getValueAt(row, 0);
        markaField.setText((String) tableModel.getValueAt(row, 1));
        modelField.setText((String) tableModel.getValueAt(row, 2));
        rejestracjaField.setText((String) tableModel.getValueAt(row, 3));
        statusBox.setSelectedItem((String) tableModel.getValueAt(row, 4));
        uwagaArea.setText((String) tableModel.getValueAt(row, 5));

        dodajButton.setEnabled(false);
        edytujButton.setEnabled(true);
        usunButton.setEnabled(true);
    }

    private void wyczyscFormularz() {
        markaField.setText("");
        modelField.setText("");
        rejestracjaField.setText("");
        statusBox.setSelectedIndex(0);
        uwagaArea.setText("");
        wybranyPojazdId = -1;

        dodajButton.setEnabled(true);
        edytujButton.setEnabled(false);
        usunButton.setEnabled(false);

        pojazdyTable.clearSelection();
    }

    private void dodajPojazd() {
        if (!walidujFormularz()) return;

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String wiadomosc = String.join(";",
                    "DODAJ_POJAZD",
                    markaField.getText().trim(),
                    modelField.getText().trim(),
                    rejestracjaField.getText().trim(),
                    (String) statusBox.getSelectedItem(),
                    uwagaArea.getText().trim()
            );

            out.println(wiadomosc);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Pojazd został dodany!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wyczyscFormularz();
                wczytajPojazdy();
            } else {
                JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void edytujPojazd() {
        if (!walidujFormularz() || wybranyPojazdId == -1) return;

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String wiadomosc = String.join(";",
                    "EDYTUJ_POJAZD",
                    String.valueOf(wybranyPojazdId),
                    markaField.getText().trim(),
                    modelField.getText().trim(),
                    rejestracjaField.getText().trim(),
                    (String) statusBox.getSelectedItem(),
                    uwagaArea.getText().trim()
            );

            out.println(wiadomosc);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Pojazd został zaktualizowany!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wyczyscFormularz();
                wczytajPojazdy();
            } else {
                JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void usunPojazd() {
        if (wybranyPojazdId == -1) return;

        int result = JOptionPane.showConfirmDialog(this,
                "Czy na pewno chcesz usunąć ten pojazd?",
                "Potwierdzenie usunięcia",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.YES_OPTION) return;

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("USUN_POJAZD;" + wybranyPojazdId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Pojazd został usunięty!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                wyczyscFormularz();
                wczytajPojazdy();
            } else {
                JOptionPane.showMessageDialog(this, "Błąd: " + response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean walidujFormularz() {
        if (markaField.getText().trim().isEmpty() ||
                modelField.getText().trim().isEmpty() ||
                rejestracjaField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Wypełnij wszystkie wymagane pola!", "Błąd walidacji", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}