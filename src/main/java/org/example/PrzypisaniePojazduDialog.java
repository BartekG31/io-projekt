package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class PrzypisaniePojazduDialog extends JDialog {
    private final JFrame parent;
    private final int zlecenieId;
    private final Runnable refreshCallback;
    private final JComboBox<String> pojazaBox;
    private final Map<String, Integer> pojazdMap = new HashMap<>();

    public PrzypisaniePojazduDialog(JFrame parent, int zlecenieId, Runnable refreshCallback) {
        super(parent, "Przypisanie pojazdu do zlecenia", true);
        this.parent = parent;
        this.zlecenieId = zlecenieId;
        this.refreshCallback = refreshCallback;

        setSize(500, 300);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Tytuł
        JLabel titleLabel = new JLabel("🚛 Przypisanie pojazdu do zlecenia");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Informacja o zleceniu
        JLabel infoLabel = new JLabel("Zlecenie ID: " + zlecenieId);
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(infoLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Panel wyboru pojazdu
        JPanel pojazaPanel = new JPanel();
        pojazaPanel.setLayout(new BoxLayout(pojazaPanel, BoxLayout.Y_AXIS));
        pojazaPanel.setBackground(Color.WHITE);
        pojazaPanel.setBorder(BorderFactory.createTitledBorder("Wybierz dostępny pojazd"));

        JLabel instrukcjaLabel = new JLabel("Wybierz pojazd z listy dostępnych:");
        instrukcjaLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instrukcjaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pojazaPanel.add(instrukcjaLabel);
        pojazaPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        pojazaBox = new JComboBox<>();
        pojazaBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pojazaBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        pojazaPanel.add(pojazaBox);

        mainPanel.add(pojazaPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Panel przycisków
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton przypiszButton = new JButton("✅ Przypisz pojazd");
        przypiszButton.setBackground(new Color(40, 167, 69));
        przypiszButton.setForeground(Color.WHITE);
        przypiszButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        przypiszButton.setFocusPainted(false);
        przypiszButton.addActionListener(e -> przypiszPojazd());

        JButton anulujButton = new JButton("❌ Anuluj");
        anulujButton.setBackground(new Color(108, 117, 125));
        anulujButton.setForeground(Color.WHITE);
        anulujButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        anulujButton.setFocusPainted(false);
        anulujButton.addActionListener(e -> dispose());

        buttonPanel.add(przypiszButton);
        buttonPanel.add(anulujButton);
        mainPanel.add(buttonPanel);

        add(mainPanel);

        // Wczytaj dostępne pojazdy
        wczytajDostepnePojazdy();

        setVisible(true);
    }

    private void wczytajDostepnePojazdy() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_DOSTEPNE_POJAZDY");
            String response = in.readLine();

            pojazaBox.removeAllItems();
            pojazdMap.clear();

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");

                if (entries.length == 1) {
                    pojazaBox.addItem("Brak dostępnych pojazdów");
                    return;
                }

                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    if (fields.length >= 4) {
                        int id = Integer.parseInt(fields[0]);
                        String marka = fields[1];
                        String model = fields[2];
                        String rejestracja = fields[3];

                        String displayText = String.format("%s %s (%s)", marka, model, rejestracja);
                        pojazaBox.addItem(displayText);
                        pojazdMap.put(displayText, id);
                    }
                }
            } else {
                pojazaBox.addItem("Brak dostępnych pojazdów");
                JOptionPane.showMessageDialog(this,
                        "Nie znaleziono dostępnych pojazdów w systemie.\nWszystkie pojazdy są obecnie zajęte lub w naprawie.",
                        "Informacja",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Błąd podczas wczytywania listy pojazdów: " + e.getMessage(),
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void przypiszPojazd() {
        String selectedPojazd = (String) pojazaBox.getSelectedItem();

        if (selectedPojazd == null || "Brak dostępnych pojazdów".equals(selectedPojazd)) {
            JOptionPane.showMessageDialog(this,
                    "Nie wybrano pojazdu lub brak dostępnych pojazdów.",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer pojazdId = pojazdMap.get(selectedPojazd);
        if (pojazdId == null) {
            JOptionPane.showMessageDialog(this,
                    "Błąd: Nie można określić ID pojazdu.",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Potwierdzenie przypisania
        int result = JOptionPane.showConfirmDialog(this,
                String.format("Czy na pewno chcesz przypisać pojazd:\n%s\ndo zlecenia ID: %d?", selectedPojazd, zlecenieId),
                "Potwierdzenie przypisania",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        // Wyślij żądanie przypisania
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("PRZYPISZ_POJAZD_DO_ZLECENIA;" + zlecenieId + ";" + pojazdId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this,
                        String.format("Pojazd %s został pomyślnie przypisany do zlecenia!", selectedPojazd),
                        "Sukces",
                        JOptionPane.INFORMATION_MESSAGE);

                // Odśwież dane w rodzicu
                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                dispose();
            } else {
                String errorMsg = response.split(";", 2).length > 1 ? response.split(";", 2)[1] : "Nieznany błąd";
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas przypisywania pojazdu:\n" + errorMsg,
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Błąd połączenia z serwerem: " + e.getMessage(),
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}