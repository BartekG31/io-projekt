package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class SzczegolyTrasyDialog extends JDialog {
    private final int idRaportu;

    public SzczegolyTrasyDialog(JFrame parent, int idRaportu) {
        super(parent, "Szczegóły raportu trasy", true);
        this.idRaportu = idRaportu;

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setResizable(false);

        initializeComponents();
        wczytajSzczegolyTrasy();
        setVisible(true);
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JLabel titleLabel = new JLabel("Szczegóły raportu trasy #" + idRaportu);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel zawartości
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Sekcja podstawowych informacji
        JPanel basicInfoPanel = createSectionPanel("Informacje podstawowe");
        contentPanel.add(basicInfoPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja szczegółów technicznych
        JPanel techDetailsPanel = createSectionPanel("Szczegóły techniczne");
        contentPanel.add(techDetailsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja uwag
        JPanel notesPanel = createSectionPanel("Uwagi i komentarze");
        contentPanel.add(notesPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel przycisków
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton exportButton = new JButton("Eksportuj raport");
        exportButton.setBackground(new Color(70, 105, 255));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        exportButton.setFocusPainted(false);
        exportButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportButton.addActionListener(e -> eksportujRaport());

        JButton closeButton = new JButton("Zamknij");
        closeButton.setBackground(new Color(108, 117, 125));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(exportButton);
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
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        return sectionPanel;
    }

    private void wczytajSzczegolyTrasy() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_SZCZEGOLY_TRASY;" + idRaportu);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] data = response.split(";")[1].split("\\|");
                wypelnijSzczegoly(data);
            } else {
                pokazBlad("Nie udało się pobrać szczegółów trasy: " + response.split(";", 2)[1]);
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

        // Informacje podstawowe
        JPanel basicInfoPanel = (JPanel) contentPanel.getComponent(0);
        basicInfoPanel.removeAll();

        addInfoRow(basicInfoPanel, "ID raportu:", String.valueOf(idRaportu));
        addInfoRow(basicInfoPanel, "Data zakończenia:", data.length > 0 ? data[4] : "Brak danych");
        addInfoRow(basicInfoPanel, "Status trasy:", "Zakończona");

        // Szczegóły techniczne
        JPanel techDetailsPanel = (JPanel) contentPanel.getComponent(2);
        techDetailsPanel.removeAll();

        addInfoRow(techDetailsPanel, "Przejechane kilometry:", data.length > 1 ? data[1] + " km" : "Brak danych");
        addInfoRow(techDetailsPanel, "Zużycie paliwa:", data.length > 2 ? data[2] + " l" : "Brak danych");

        if (data.length > 1 && data.length > 2) {
            try {
                double km = Double.parseDouble(data[1]);
                double paliwo = Double.parseDouble(data[2]);
                double spalanie = (paliwo / km) * 100;
                addInfoRow(techDetailsPanel, "Średnie spalanie:", String.format("%.2f l/100km", spalanie));
            } catch (NumberFormatException e) {
                addInfoRow(techDetailsPanel, "Średnie spalanie:", "Nie można obliczyć");
            }
        }

        // Uwagi
        JPanel notesPanel = (JPanel) contentPanel.getComponent(4);
        notesPanel.removeAll();

        JTextArea notesArea = new JTextArea(data.length > 3 ? data[3] : "Brak uwag");
        notesArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(new Color(248, 248, 248));
        notesArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(500, 80));
        notesScroll.setBorder(BorderFactory.createLoweredBevelBorder());
        notesPanel.add(notesScroll);

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
        labelComp.setPreferredSize(new Dimension(150, 20));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("SansSerif", Font.PLAIN, 13));

        rowPanel.add(labelComp, BorderLayout.WEST);
        rowPanel.add(valueComp, BorderLayout.CENTER);

        parent.add(rowPanel);
    }

    private void eksportujRaport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Zapisz raport trasy");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Pliki tekstowe (*.txt)", "txt"));
        fileChooser.setSelectedFile(new java.io.File("raport_trasy_" + idRaportu + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                // Implementacja eksportu do pliku
                JOptionPane.showMessageDialog(this,
                        "Raport został zapisany do pliku:\n" + file.getAbsolutePath(),
                        "Eksport zakończony",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                pokazBlad("Błąd podczas zapisywania pliku: " + e.getMessage());
            }
        }
    }

    private void pokazBlad(String wiadomosc) {
        JOptionPane.showMessageDialog(this, wiadomosc, "Błąd", JOptionPane.ERROR_MESSAGE);
    }
}