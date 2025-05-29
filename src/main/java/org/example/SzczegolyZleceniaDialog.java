package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class SzczegolyZleceniaDialog extends JDialog {
    private final int idZlecenia;
    private final String currentStatus;

    public SzczegolyZleceniaDialog(JFrame parent, int idZlecenia) {
        super(parent, "Szczegóły zlecenia", true);
        this.idZlecenia = idZlecenia;
        this.currentStatus = "";

        initializeDialog();
    }

    public SzczegolyZleceniaDialog(JFrame parent, int idZlecenia, String status) {
        super(parent, "Szczegóły zlecenia", true);
        this.idZlecenia = idZlecenia;
        this.currentStatus = status;

        initializeDialog();
    }

    private void initializeDialog() {
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);

        initializeComponents();
        wczytajSzczegolyZlecenia();
        setVisible(true);
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JLabel titleLabel = new JLabel("Szczegóły zlecenia #" + idZlecenia);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel zawartości
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Sekcja danych nadawcy i odbiorcy
        JPanel osobyPanel = createSectionPanel("Dane nadawcy i odbiorcy");
        contentPanel.add(osobyPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja adresu dostawy
        JPanel adresPanel = createSectionPanel("Adres dostawy");
        contentPanel.add(adresPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja szczegółów przesyłki
        JPanel przesylkaPanel = createSectionPanel("Szczegóły przesyłki");
        contentPanel.add(przesylkaPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja statusu i realizacji
        JPanel statusPanel = createSectionPanel("Status i realizacja");
        contentPanel.add(statusPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja historii zmian statusu
        JPanel historiaPanel = createSectionPanel("Historia statusów");
        contentPanel.add(historiaPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel przycisków
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton trackButton = new JButton("Śledź przesyłkę");
        trackButton.setBackground(new Color(70, 105, 255));
        trackButton.setForeground(Color.WHITE);
        trackButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        trackButton.setFocusPainted(false);
        trackButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        trackButton.addActionListener(e -> sledzPrzesylke());

        JButton printButton = new JButton("Drukuj etykietę");
        printButton.setBackground(new Color(40, 167, 69));
        printButton.setForeground(Color.WHITE);
        printButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        printButton.setFocusPainted(false);
        printButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        printButton.addActionListener(e -> drukujEtykiete());

        JButton closeButton = new JButton("Zamknij");
        closeButton.setBackground(new Color(108, 117, 125));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(trackButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(printButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private JPanel createSectionPanel(String title) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(new Color(248, 248, 248));
        sectionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title),
                new EmptyBorder(10, 15, 10, 15)
        ));
        sectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        return sectionPanel;
    }

    private void wczytajSzczegolyZlecenia() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_SZCZEGOLY_ZLECENIA;" + idZlecenia);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] data = response.split(";")[1].split("\\|");
                wypelnijSzczegoly(data);
            } else {
                pokazBlad("Nie udało się pobrać szczegółów zlecenia: " + response.split(";", 2)[1]);
            }

        } catch (IOException e) {
            pokazBlad("Błąd połączenia z serwerem: " + e.getMessage());
        }
    }

    private void wypelnijSzczegoly(String[] data) {
        Component[] components = getContentPane().getComponents();
        JPanel mainPanel = (JPanel) components[0];
        JScrollPane scrollPane = (JScrollPane) mainPanel.getComponent(1);
        JPanel contentPanel = (JPanel) ((JViewport) scrollPane.getComponent(0)).getView();

        // Dane nadawcy i odbiorcy
        JPanel osobyPanel = (JPanel) contentPanel.getComponent(0);
        osobyPanel.removeAll();
        addInfoRow(osobyPanel, "Nadawca:", data.length > 1 ? data[1] : "Brak danych");
        addInfoRow(osobyPanel, "Odbiorca:", data.length > 2 ? data[2] : "Brak danych");

        // Adres dostawy
        JPanel adresPanel = (JPanel) contentPanel.getComponent(2);
        adresPanel.removeAll();
        addInfoRow(adresPanel, "Adres:", data.length > 3 ? data[3] : "Brak danych");
        addInfoRow(adresPanel, "Miasto:", data.length > 4 ? data[4] : "Brak danych");
        addInfoRow(adresPanel, "Kod pocztowy:", data.length > 5 ? data[5] : "Brak danych");

        // Szczegóły przesyłki
        JPanel przesylkaPanel = (JPanel) contentPanel.getComponent(4);
        przesylkaPanel.removeAll();
        addInfoRow(przesylkaPanel, "Opis:", data.length > 6 ? data[6] : "Brak danych");
        addInfoRow(przesylkaPanel, "Waga:", data.length > 7 ? data[7] + " kg" : "Brak danych");
        addInfoRow(przesylkaPanel, "Data nadania:", data.length > 8 ? data[8] : "Brak danych");

        // Status i realizacja
        JPanel statusPanel = (JPanel) contentPanel.getComponent(6);
        statusPanel.removeAll();
        String status = data.length > 9 ? data[9] : currentStatus;
        addInfoRow(statusPanel, "Aktualny status:", status);
        addStatusIndicator(statusPanel, status);
        addInfoRow(statusPanel, "Przypisany pojazd:", data.length > 10 ? data[10] : "Brak przypisania");

        // Historia statusów
        JPanel historiaPanel = (JPanel) contentPanel.getComponent(8);
        historiaPanel.removeAll();
        wczytajHistorieStatusow(historiaPanel);

        // Odśwież widok
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void addInfoRow(JPanel parent, String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(new Color(248, 248, 248));
        rowPanel.setBorder(new EmptyBorder(3, 0, 3, 0));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelComp.setPreferredSize(new Dimension(130, 20));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("SansSerif", Font.PLAIN, 13));

        rowPanel.add(labelComp, BorderLayout.WEST);
        rowPanel.add(valueComp, BorderLayout.CENTER);

        parent.add(rowPanel);
    }

    private void addStatusIndicator(JPanel parent, String status) {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        statusPanel.setBackground(new Color(248, 248, 248));

        JLabel statusDot = new JLabel("●");
        statusDot.setFont(new Font("SansSerif", Font.BOLD, 16));

        Color statusColor = getStatusColor(status);
        statusDot.setForeground(statusColor);

        JLabel statusText = new JLabel(getStatusDescription(status));
        statusText.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusText.setForeground(statusColor);

        statusPanel.add(statusDot);
        statusPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        statusPanel.add(statusText);

        parent.add(statusPanel);
    }

    private Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "nowe": return new Color(108, 117, 125);
            case "przyjęte": return new Color(255, 193, 7);
            case "gotowe do wysyłki": return new Color(70, 105, 255);
            case "w drodze": return new Color(255, 102, 0);
            case "oczekiwanie na odbiór": return new Color(220, 53, 69);
            case "zrealizowane": return new Color(40, 167, 69);
            case "odrzucone": return new Color(220, 53, 69);
            default: return Color.GRAY;
        }
    }

    private String getStatusDescription(String status) {
        switch (status.toLowerCase()) {
            case "nowe": return "Zlecenie oczekuje na przyjęcie";
            case "przyjęte": return "Towar przyjęty do magazynu";
            case "gotowe do wysyłki": return "Paczka przygotowana do wysyłki";
            case "w drodze": return "Przesyłka jest w transporcie";
            case "oczekiwanie na odbiór": return "Dostarczona, oczekuje na odbiór";
            case "zrealizowane": return "Zlecenie zostało pomyślnie zrealizowane";
            case "odrzucone": return "Zlecenie zostało odrzucone";
            default: return "Status nieznany";
        }
    }

    private void wczytajHistorieStatusow(JPanel historiaPanel) {
        // Symulacja historii statusów - w rzeczywistej aplikacji pobierałoby z bazy
        String[] statusy = {"Nowe", "Przyjęte", "Gotowe do wysyłki", currentStatus};
        String[] daty = {"2024-01-15 10:30", "2024-01-15 14:20", "2024-01-16 09:15", "2024-01-16 16:45"};

        for (int i = 0; i < statusy.length && i < daty.length; i++) {
            if (!statusy[i].equals(currentStatus) || i == statusy.length - 1) {
                JPanel historyRow = new JPanel(new BorderLayout());
                historyRow.setBackground(new Color(248, 248, 248));
                historyRow.setBorder(new EmptyBorder(2, 0, 2, 0));

                JLabel timeLabel = new JLabel(daty[i]);
                timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
                timeLabel.setForeground(Color.GRAY);
                timeLabel.setPreferredSize(new Dimension(120, 15));

                JLabel statusLabel = new JLabel(statusy[i]);
                statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

                historyRow.add(timeLabel, BorderLayout.WEST);
                historyRow.add(statusLabel, BorderLayout.CENTER);

                historiaPanel.add(historyRow);
            }
        }
    }

    private void sledzPrzesylke() {
        // Otwórz okno śledzenia przesyłki
        JOptionPane.showMessageDialog(this,
                "Funkcja śledzenia przesyłki zostanie uruchomiona.\n" +
                        "Numer zlecenia: " + idZlecenia,
                "Śledzenie przesyłki",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void drukujEtykiete() {
        // Symulacja drukowania etykiety
        JOptionPane.showMessageDialog(this,
                "Etykieta dla zlecenia #" + idZlecenia + " została wysłana do drukarki.",
                "Drukowanie etykiety",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void pokazBlad(String wiadomosc) {
        JOptionPane.showMessageDialog(this, wiadomosc, "Błąd", JOptionPane.ERROR_MESSAGE);
    }
}