package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class OdbiorForm extends JFrame {
    private final String odbiorca;

    public OdbiorForm(String odbiorca) {
        this.odbiorca = odbiorca;

        setTitle("Zatwierdź odbiór przesyłki");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 160);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Zatwierdzasz odbiór jako: " + odbiorca);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton confirmBtn = new JButton("Zatwierdź odbiór");
        confirmBtn.setBackground(new Color(34, 139, 34));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(this::handleConfirm);
        panel.add(confirmBtn);

        add(panel);
        setVisible(true);
    }

    private void handleConfirm(ActionEvent e) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("ZATWIERDZ_ODBIOR;" + odbiorca);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, response.split(";", 2)[1], "Sukces", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}
