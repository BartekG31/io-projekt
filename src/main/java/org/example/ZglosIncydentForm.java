package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ZglosIncydentForm extends JFrame {
    private final JComboBox<String> typIncydentuBox;
    private final JTextArea opisField;
    private final JTextField lokalizacjaField;

    public ZglosIncydentForm() {
        setTitle("Zgłoś incydent");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Zgłoszenie incydentu");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Typ incydentu
        JLabel typLabel = new JLabel("Typ incydentu:");
        typLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(typLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        typIncydentuBox = new JComboBox<>(new String[]{
                "Problem z pojazdem",
                "Wypadek drogowy",
                "Uszkodzenie przesyłki",
                "Problem z dostępem do odbiorcy",
                "Inne"
        });
        typIncydentuBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        typIncydentuBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(typIncydentuBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Lokalizacja
        JLabel lokalizacjaLabel = new JLabel("Lokalizacja:");
        lokalizacjaLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(lokalizacjaLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        lokalizacjaField = new JTextField();
        lokalizacjaField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lokalizacjaField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lokalizacjaField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.add(lokalizacjaField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Opis incydentu
        JLabel opisLabel = new JLabel("Szczegółowy opis incydentu:");
        opisLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(opisLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        opisField = new JTextArea(6, 30);
        opisField.setLineWrap(true);
        opisField.setWrapStyleWord(true);
        opisField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        opisField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        JScrollPane scrollPane = new JScrollPane(opisField);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.add(scrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Przycisk zgłoszenia
        JButton zglosButton = new JButton("Zgłoś incydent");
        zglosButton.setBackground(new Color(220, 53, 69));
        zglosButton.setForeground(Color.WHITE);
        zglosButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        zglosButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        zglosButton.setFocusPainted(false);
        zglosButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        zglosButton.setMaximumSize(new Dimension(200, 40));
        zglosButton.addActionListener(e -> zglosIncydent());
        panel.add(zglosButton);

        add(panel);
        setVisible(true);
    }

    private void zglosIncydent() {
        String typ = (String) typIncydentuBox.getSelectedItem();
        String lokalizacja = lokalizacjaField.getText().trim();
        String opis = opisField.getText().trim();

        if (lokalizacja.isEmpty() || opis.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Wypełnij wszystkie pola!",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String wiadomosc = "ZGLOS_INCYDENT;" + typ + ";" + lokalizacja + ";" + opis;
            out.println(wiadomosc);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this,
                        "Incydent został zgłoszony do logistyka!",
                        "Sukces",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Błąd: " + response.split(";", 2)[1],
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
}