package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Calendar;

public class RaportMiesiecznyForm extends JFrame {
    private JSpinner rokSpinner;
    private JComboBox<String> miesiacComboBox;
    private JTextArea raportArea;
    private JButton generateButton;
    private JButton exportButton;
    private JLabel statusLabel;

    private final String[] miesiace = {
            "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
            "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
    };

    public RaportMiesiecznyForm() {
        setTitle("Generator raportów miesięcznych");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        initializeComponents();
        setVisible(true);
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nagłówek
        JLabel titleLabel = new JLabel("📊 Generator raportów miesięcznych");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Panel wyboru okresu
        JPanel selectionPanel = createSelectionPanel();

        // Połącz nagłówek i selekcję
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(248, 248, 248));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(selectionPanel, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Panel raportu
        JPanel reportPanel = createReportPanel();
        mainPanel.add(reportPanel, BorderLayout.CENTER);

        // Panel przycisków
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Ustaw domyślne wartości
        setDefaultValues();
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 248, 248));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Wybierz okres raportu"),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // Panel z kontrolkami
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlsPanel.setBackground(new Color(248, 248, 248));

        // Wybór roku
        JLabel rokLabel = new JLabel("Rok:");
        rokLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        rokSpinner = new JSpinner(new SpinnerNumberModel(currentYear, 2020, currentYear + 5, 1));
        rokSpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) rokSpinner.getEditor()).getTextField().setColumns(6);

        // Wybór miesiąca
        JLabel miesiacLabel = new JLabel("Miesiąc:");
        miesiacLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        miesiacComboBox = new JComboBox<>(miesiace);
        miesiacComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        miesiacComboBox.setSelectedIndex(cal.get(Calendar.MONTH));

        controlsPanel.add(rokLabel);
        controlsPanel.add(rokSpinner);
        controlsPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        controlsPanel.add(miesiacLabel);
        controlsPanel.add(miesiacComboBox);

        panel.add(controlsPanel);
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Raport miesięczny"));

        raportArea = new JTextArea();
        raportArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        raportArea.setEditable(false);
        raportArea.setBackground(Color.WHITE);
        raportArea.setText("Wybierz okres i kliknij 'Generuj raport' aby rozpocząć...");

        JScrollPane scrollPane = new JScrollPane(raportArea);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(248, 248, 248));
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Status
        statusLabel = new JLabel("Gotowy do generowania raportu");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);

        // Przyciski
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setBackground(new Color(248, 248, 248));

        generateButton = new JButton("📊 Generuj raport");
        generateButton.setBackground(new Color(40, 167, 69));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        generateButton.setFocusPainted(false);
        generateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        generateButton.addActionListener(e -> generujRaport());

        exportButton = new JButton("💾 Eksportuj do pliku");
        exportButton.setBackground(new Color(70, 105, 255));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        exportButton.setFocusPainted(false);
        exportButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> eksportujRaport());

        JButton zamknijButton = new JButton("Zamknij");
        zamknijButton.setBackground(new Color(108, 117, 125));
        zamknijButton.setForeground(Color.WHITE);
        zamknijButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        zamknijButton.setFocusPainted(false);
        zamknijButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        zamknijButton.addActionListener(e -> dispose());

        buttonsPanel.add(generateButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonsPanel.add(exportButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonsPanel.add(zamknijButton);

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    private void setDefaultValues() {
        // Ustaw domyślnie poprzedni miesiąc
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        rokSpinner.setValue(cal.get(Calendar.YEAR));
        miesiacComboBox.setSelectedIndex(cal.get(Calendar.MONTH));
    }

    private void generujRaport() {
        generateButton.setEnabled(false);
        statusLabel.setText("Generowanie raportu...");

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return pobierzDaneRaportu();
            }

            @Override
            protected void done() {
                try {
                    String raportData = get();
                    if (raportData != null) {
                        String sformatowanyRaport = formatujRaport(raportData);
                        raportArea.setText(sformatowanyRaport);
                        exportButton.setEnabled(true);
                        statusLabel.setText("Raport wygenerowany pomyślnie");
                    } else {
                        raportArea.setText("Błąd podczas generowania raportu lub brak danych za wybrany okres.");
                        statusLabel.setText("Błąd generowania raportu");
                    }
                } catch (Exception e) {
                    raportArea.setText("Wystąpił błąd: " + e.getMessage());
                    statusLabel.setText("Błąd: " + e.getMessage());
                } finally {
                    generateButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private String pobierzDaneRaportu() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            int rok = (Integer) rokSpinner.getValue();
            int miesiac = miesiacComboBox.getSelectedIndex() + 1;

            out.println("GENERUJ_RAPORT_MIESIECZNY;" + rok + ";" + miesiac);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                return response;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatujRaport(String rawData) {
        try {
            String[] parts = rawData.split(";")[1].split("\\|");

            int totalZlecen = Integer.parseInt(parts[0]);
            int zrealizowane = Integer.parseInt(parts[1]);
            int odrzucone = Integer.parseInt(parts[2]);
            int wTrakcie = Integer.parseInt(parts[3]);
            double totalWaga = Double.parseDouble(parts[4]);

            DecimalFormat df = new DecimalFormat("#,##0.00");
            DecimalFormat percentFormat = new DecimalFormat("#0.0%");

            int rok = (Integer) rokSpinner.getValue();
            String miesiacNazwa = miesiace[miesiacComboBox.getSelectedIndex()];

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════════════\n");
            sb.append("                    RAPORT MIESIĘCZNY FIRMY TRANSPORTOWEJ\n");
            sb.append("═══════════════════════════════════════════════════════════════════\n");
            sb.append(String.format("Okres raportu: %s %d\n", miesiacNazwa, rok));
            sb.append(String.format("Data wygenerowania: %s\n", java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            sb.append("───────────────────────────────────────────────────────────────────\n\n");

            sb.append("📊 PODSUMOWANIE ZLECEŃ:\n");
            sb.append("───────────────────────────────────────────────────────────────────\n");
            sb.append(String.format("• Łączna liczba zleceń:        %,6d\n", totalZlecen));
            sb.append(String.format("• Zlecenia zrealizowane:        %,6d (%s)\n",
                    zrealizowane, percentFormat.format(totalZlecen > 0 ? (double)zrealizowane/totalZlecen : 0)));
            sb.append(String.format("• Zlecenia odrzucone:           %,6d (%s)\n",
                    odrzucone, percentFormat.format(totalZlecen > 0 ? (double)odrzucone/totalZlecen : 0)));
            sb.append(String.format("• Zlecenia w trakcie realizacji:%,6d (%s)\n",
                    wTrakcie, percentFormat.format(totalZlecen > 0 ? (double)wTrakcie/totalZlecen : 0)));
            sb.append("\n");

            sb.append("📦 ANALIZA PRZEWOZÓW:\n");
            sb.append("───────────────────────────────────────────────────────────────────\n");
            sb.append(String.format("• Łączna waga przewieziona:     %s kg\n", df.format(totalWaga)));
            sb.append(String.format("• Średnia waga na zlecenie:     %s kg\n",
                    df.format(totalZlecen > 0 ? totalWaga/totalZlecen : 0)));
            sb.append("\n");

            sb.append("📈 WSKAŹNIKI WYDAJNOŚCI:\n");
            sb.append("───────────────────────────────────────────────────────────────────\n");
            double wskaznikRealizacji = totalZlecen > 0 ? (double)zrealizowane/totalZlecen : 0;
            sb.append(String.format("• Wskaźnik realizacji zleceń:   %s\n", percentFormat.format(wskaznikRealizacji)));

            String ocenaWydajnosci;
            if (wskaznikRealizacji >= 0.95) ocenaWydajnosci = "DOSKONAŁA ⭐⭐⭐⭐⭐";
            else if (wskaznikRealizacji >= 0.90) ocenaWydajnosci = "BARDZO DOBRA ⭐⭐⭐⭐";
            else if (wskaznikRealizacji >= 0.80) ocenaWydajnosci = "DOBRA ⭐⭐⭐";
            else if (wskaznikRealizacji >= 0.70) ocenaWydajnosci = "PRZECIĘTNA ⭐⭐";
            else ocenaWydajnosci = "WYMAGA POPRAWY ⭐";

            sb.append(String.format("• Ocena wydajności:             %s\n", ocenaWydajnosci));
            sb.append("\n");

            sb.append("📋 REKOMENDACJE:\n");
            sb.append("───────────────────────────────────────────────────────────────────\n");
            if (wskaznikRealizacji < 0.85) {
                sb.append("• Zaleca się analizę przyczyn odrzuconych zleceń\n");
                sb.append("• Rozważenie zwiększenia zasobów transportowych\n");
            }
            if (wTrakcie > totalZlecen * 0.15) {
                sb.append("• Duża liczba zleceń w trakcie - sprawdź przepływ procesów\n");
            }
            if (wskaznikRealizacji >= 0.90) {
                sb.append("• Doskonałe wyniki! Utrzymaj obecny poziom jakości\n");
            }

            sb.append("\n");
            sb.append("═══════════════════════════════════════════════════════════════════\n");
            sb.append("                    Koniec raportu\n");
            sb.append("═══════════════════════════════════════════════════════════════════");

            return sb.toString();

        } catch (Exception e) {
            return "Błąd podczas formatowania raportu: " + e.getMessage();
        }
    }

    private void eksportujRaport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Zapisz raport miesięczny");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Pliki tekstowe (*.txt)", "txt"));

        int rok = (Integer) rokSpinner.getValue();
        String miesiacNazwa = miesiace[miesiacComboBox.getSelectedIndex()];
        fileChooser.setSelectedFile(new java.io.File("Raport_" + miesiacNazwa + "_" + rok + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                writer.write(raportArea.getText());
                JOptionPane.showMessageDialog(this,
                        "Raport został zapisany do pliku:\n" + fileChooser.getSelectedFile().getAbsolutePath(),
                        "Eksport zakończony",
                        JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("Raport wyeksportowany do pliku");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas zapisywania pliku: " + e.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}