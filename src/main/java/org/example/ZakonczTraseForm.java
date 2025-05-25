package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ZakonczTraseForm extends JFrame {
    private final JTextArea raportField;
    private final JSpinner kilometrySpinner;
    private final JTextField spalineField;

    public ZakonczTraseForm() {
        setTitle("Zakończ trasę");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Zakończenie trasy");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Kilometry
        JLabel kmLabel = new JLabel("Przejechane kilometry:");
        kmLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(kmLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        kilometrySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        kilometrySpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        kilometrySpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(kilometrySpinner);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Spalanie
        JLabel spalineLabel = new JLabel("Zużycie paliwa (litry):");
        spalineLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(spalineLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        spalineField = new JTextField();
        spalineField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        spalineField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        spalineField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.add(spalineField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Raport z trasy
        JLabel raportLabel = new JLabel("Raport z trasy (opcjonalnie):");
        raportLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(raportLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        raportField = new JTextArea(6, 30);
        raportField.setLineWrap(true);
        raportField.setWrapStyleWord(true);
        raportField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        raportField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        raportField.setBackground(new Color(248, 248, 248));
        JScrollPane scrollPane = new JScrollPane(raportField);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.add(scrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Panel z przyciskami
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton sprawdzStatusButton = new JButton("Sprawdź status paczek");
        sprawdzStatusButton.setBackground(new Color(108, 117, 125));
        sprawdzStatusButton.setForeground(Color.WHITE);
        sprawdzStatusButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        sprawdzStatusButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sprawdzStatusButton.setFocusPainted(false);
        sprawdzStatusButton.addActionListener(e -> sprawdzStatusPaczek());

        JButton zakonczButton = new JButton("Zakończ trasę");
        zakonczButton.setBackground(new Color(40, 167, 69));
        zakonczButton.setForeground(Color.WHITE);
        zakonczButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        zakonczButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        zakonczButton.setFocusPainted(false);
        zakonczButton.addActionListener(e -> zakonczTrase());

        buttonPanel.add(sprawdzStatusButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(zakonczButton);
        panel.add(buttonPanel);

        add(panel);
        setVisible(true);
    }

    private void sprawdzStatusPaczek() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("SPRAWDZ_STATUS_KURIERA");
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                if (parts.length == 1) {
                    JOptionPane.showMessageDialog(this,
                            "Wszystkie paczki zostały dostarczone!\nMożesz zakończyć trasę.",
                            "Status paczek",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    StringBuilder message = new StringBuilder("Pozostałe paczki do dostarczenia:\n\n");
                    for (int i = 1; i < parts.length; i++) {
                        String[] data = parts[i].split("\\|");
                        message.append("• ").append(data[1]).append(" - ").append(data[2]).append("\n");
                    }
                    message.append("\nZakończ dostarczanie przed zakończeniem trasy.");
                    JOptionPane.showMessageDialog(this,
                            message.toString(),
                            "Status paczek",
                            JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Błąd sprawdzania statusu paczek",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Błąd połączenia z serwerem",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void zakonczTrase() {
        int kilometry = (Integer) kilometrySpinner.getValue();
        String spalanie = spalineField.getText().trim();
        String raport = raportField.getText().trim();

        if (kilometry == 0 || spalanie.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Wypełnij kilometry i zużycie paliwa!",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Double.parseDouble(spalanie);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Zużycie paliwa musi być liczbą!",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String wiadomosc = "ZAKONCZENIE_TRASY;" + kilometry + ";" + spalanie + ";" + raport;
            out.println(wiadomosc);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this,
                        "Trasa została zakończona!\nDane zostały przekazane do logistyka.",
                        "Sukces",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        response.split(";", 2)[1],
                        "Błąd/Informacja",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Błąd połączenia z serwerem",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}