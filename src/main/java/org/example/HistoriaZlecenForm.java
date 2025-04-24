package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class HistoriaZlecenForm extends JFrame {
    private final int userId;

    public HistoriaZlecenForm(int userId) {
        this.userId = userId;

        setTitle("Historia zleceń");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Twoje zlecenia");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("HISTORIA_ZLECEN;" + userId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");

                if (parts.length == 1) {
                    JLabel label = new JLabel("Brak zleceń do wyświetlenia.");
                    label.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panel.add(label);
                }

                for (int i = 1; i < parts.length; i++) {
                    String[] dane = parts[i].split("\\|");
                    String odbiorca = dane[0];
                    String opis = dane[1];
                    String status = dane[2];
                    String data = dane[3];

                    JPanel card = new JPanel();
                    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                    card.setBackground(new Color(245, 245, 245));
                    card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.GRAY),
                            new EmptyBorder(10, 15, 10, 15)
                    ));
                    card.setAlignmentX(Component.CENTER_ALIGNMENT);
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

                    JLabel info = new JLabel("<html><b>Odbiorca:</b> " + odbiorca +
                            "<br><b>Opis:</b> " + opis +
                            "<br><b>Status:</b> " + status +
                            "<br><b>Data nadania:</b> " + data + "</html>");
                    info.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    card.add(info);

                    panel.add(card);
                    panel.add(Box.createRigidArea(new Dimension(0, 15)));
                }

            } else {
                JLabel err = new JLabel("Błąd: " + response.split(";", 2)[1]);
                err.setForeground(Color.RED);
                err.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(err);
            }

        } catch (IOException ex) {
            JLabel err = new JLabel("Błąd połączenia z serwerem");
            err.setForeground(Color.RED);
            err.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(err);
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        setContentPane(scroll);
        setVisible(true);
    }
}
