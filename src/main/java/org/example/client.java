package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class client extends JFrame {
    private final JTextField loginField;
    private final JPasswordField passwordField;
    private final JLabel statusLabel;

    public client() {
        setTitle("Logowanie do systemu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 320);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(30, 30, 20, 30));

        JLabel title = new JLabel("Logowanie do systemu");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(mainPanel.getBackground());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Login:"), gbc);

        gbc.gridx = 1;
        loginField = new JTextField(15);
        stylujPole(loginField);
        formPanel.add(loginField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Hasło:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        stylujPole(passwordField);
        formPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("Zaloguj");
        loginButton.setBackground(new Color(70, 105, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginButton.setFocusPainted(false);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.addActionListener(this::handleLogin);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusLabel.setForeground(Color.RED);
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(loginButton);
        mainPanel.add(statusLabel);

        add(mainPanel);
        setVisible(true);
    }

    private void stylujPole(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
    }

    private void handleLogin(ActionEvent e) {
        String login = loginField.getText().trim();
        String haslo = new String(passwordField.getPassword()).trim();

        if (login.isEmpty() || haslo.isEmpty()) {
            pokazBlad("Wprowadź login i hasło.");
            return;
        }

        try (Socket socket = new Socket("localhost", 5000)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("LOGIN;" + login + ";" + haslo);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                int id = Integer.parseInt(parts[1]);
                String imie = parts[2];
                String nazwisko = parts[3];
                String rola = parts[4];

                String pelneImieNazwisko = imie + " " + nazwisko;
                new MainPanel(id, pelneImieNazwisko, rola);
                dispose();
            } else {
                pokazBlad(response.split(";", 2)[1]);
            }

        } catch (IOException ex) {
            pokazBlad("Błąd połączenia z serwerem.");
        }
    }

    private void pokazBlad(String tekst) {
        statusLabel.setText(tekst);
        statusLabel.setForeground(Color.RED);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(client::new);
    }
}