package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class InwentaryzacjaForm extends JFrame {

    public InwentaryzacjaForm() {
        setTitle("Inwentaryzacja przyjętych paczek");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Przyjęte paczki w magazynie");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_INWENTARYZACJE");
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                if (parts.length == 1) {
                    JLabel label = new JLabel("Brak przyjętych paczek.");
                    label.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panel.add(label);
                }

                for (int i = 1; i < parts.length; i++) {
                    String[] data = parts[i].split("\\|");
                    String id = data[0];
                    String odbiorca = data[1];
                    String opis = data[2];
                    String waga = data[3];
                    String dataNadania = data[4];

                    JPanel box = new JPanel();
                    box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
                    box.setBackground(new Color(245, 245, 245));
                    box.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.GRAY),
                            new EmptyBorder(10, 15, 10, 15)
                    ));
                    box.setAlignmentX(Component.CENTER_ALIGNMENT);
                    box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

                    JLabel info = new JLabel("<html><b>ID:</b> " + id +
                            "<br><b>Odbiorca:</b> " + odbiorca +
                            "<br><b>Opis:</b> " + opis +
                            "<br><b>Waga:</b> " + waga + " kg" +
                            "<br><b>Data nadania:</b> " + dataNadania + "</html>");
                    info.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    box.add(info);

                    panel.add(box);
                    panel.add(Box.createRigidArea(new Dimension(0, 15)));
                }

            } else {
                JLabel label = new JLabel("Błąd: " + response.split(";", 2)[1]);
                label.setForeground(Color.RED);
                panel.add(label);
            }

        } catch (IOException e) {
            JLabel err = new JLabel("Błąd połączenia z serwerem");
            err.setForeground(Color.RED);
            panel.add(err);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        setContentPane(scrollPane);
        setVisible(true);
    }
}
