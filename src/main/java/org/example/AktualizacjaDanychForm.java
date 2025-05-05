package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class AktualizacjaDanychForm extends JFrame {
    private final int userId;
    private final JTextField loginField;
    private final JPasswordField hasloField;
    private final JLabel imieLabel;
    private final JLabel nazwiskoLabel;

    public AktualizacjaDanychForm(int userId) {
        this.userId = userId;

        setTitle("Aktualizacja danych osobowych");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        // Ikonka okna (zmień ścieżkę jeśli trzeba lub usuń)
        try {
            setIconImage(new ImageIcon("src/main/resources/icon.png").getImage());
        } catch (Exception ignored) {}

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Zaktualizuj swoje dane");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Imię i nazwisko w jednej linii
        JPanel namePanel = new JPanel(new GridLayout(1, 2, 20, 0));
        namePanel.setOpaque(false);

        JPanel imieBox = new JPanel();
        imieBox.setLayout(new BoxLayout(imieBox, BoxLayout.Y_AXIS));
        imieBox.setOpaque(false);
        JLabel imieText = new JLabel("Imię:");
        imieText.setFont(new Font("SansSerif", Font.PLAIN, 15));
        imieBox.add(imieText);
        imieLabel = new JLabel("Ładowanie...");
        imieLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        imieBox.add(imieLabel);

        JPanel nazwiskoBox = new JPanel();
        nazwiskoBox.setLayout(new BoxLayout(nazwiskoBox, BoxLayout.Y_AXIS));
        nazwiskoBox.setOpaque(false);
        JLabel nazwiskoText = new JLabel("Nazwisko:");
        nazwiskoText.setFont(new Font("SansSerif", Font.PLAIN, 15));
        nazwiskoBox.add(nazwiskoText);
        nazwiskoLabel = new JLabel("Ładowanie...");
        nazwiskoLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nazwiskoBox.add(nazwiskoLabel);

        namePanel.add(imieBox);
        namePanel.add(nazwiskoBox);
        panel.add(namePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Login
        JLabel loginLabel = new JLabel("Login:");
        loginLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        panel.add(loginLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        loginField = new JTextField();
        stylizujPole(loginField);
        panel.add(loginField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Hasło
        JLabel hasloLabel = new JLabel("Hasło:");
        hasloLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        panel.add(hasloLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        hasloField = new JPasswordField();
        stylizujPole(hasloField);
        panel.add(hasloField);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Przycisk
        JButton updateButton = new JButton("Zapisz zmiany");
        updateButton.setBackground(new Color(70, 105, 255));
        updateButton.setForeground(Color.WHITE);
        updateButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        updateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateButton.setFocusPainted(false);
        updateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 90, 255), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        updateButton.setMaximumSize(new Dimension(200, 45));
        updateButton.addActionListener(e -> aktualizujDane());
        panel.add(updateButton);

        add(panel);
        setVisible(true);

        wczytajDaneUzytkownika();
    }

    private void stylizujPole(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void wczytajDaneUzytkownika() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_UZYTKOWNIKA;" + userId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] dane = response.split(";");
                imieLabel.setText(dane[1]);
                nazwiskoLabel.setText(dane[2]);
                loginField.setText(dane[3]);
                hasloField.setText(dane[4]);
            } else {
                JOptionPane.showMessageDialog(this, "Błąd podczas pobierania danych", "Błąd", JOptionPane.ERROR_MESSAGE);
                dispose();
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem", "Błąd", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void aktualizujDane() {
        String login = loginField.getText().trim();
        String haslo = new String(hasloField.getPassword()).trim();

        if (login.isEmpty() || haslo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wypełnij login i hasło!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String wiadomosc = String.join(";",
                    "AKTUALIZUJ_DANE",
                    String.valueOf(userId),
                    imieLabel.getText(),
                    nazwiskoLabel.getText(),
                    login,
                    haslo
            );

            out.println(wiadomosc);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Dane zostały zaktualizowane!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}
