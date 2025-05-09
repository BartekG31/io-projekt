package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class PrzyjecieTowaruForm extends JFrame {

    public PrzyjecieTowaruForm() {
        setTitle("Przyjęcie towaru");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Zlecenia do przyjęcia");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_DO_PRZYJECIA");
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                if (parts.length == 1) {
                    JLabel label = new JLabel("Brak zleceń do przyjęcia.");
                    label.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panel.add(label);
                }

                for (int i = 1; i < parts.length; i++) {
                    String[] data = parts[i].split("\\|");
                    String id = data[0];
                    String nadawca = data[1];
                    String odbiorca = data[2];
                    String opis = data[3];
                    String waga = data[4];
                    String dataNadania = data[5];

                    JPanel box = new JPanel();
                    box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
                    box.setBackground(new Color(245, 245, 245));
                    box.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.GRAY),
                            new EmptyBorder(10, 15, 10, 15)
                    ));
                    box.setAlignmentX(Component.CENTER_ALIGNMENT);
                    box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

                    JLabel info = new JLabel("<html><b>Nadawca:</b> " + nadawca +
                            "<br><b>Odbiorca:</b> " + odbiorca +
                            "<br><b>Opis:</b> " + opis +
                            "<br><b>Waga:</b> " + waga + " kg" +
                            "<br><b>Data nadania:</b> " + dataNadania + "</html>");
                    info.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    box.add(info);

                    JButton acceptBtn = new JButton("Przyjmij towar");
                    acceptBtn.setBackground(new Color(34, 139, 34));
                    acceptBtn.setForeground(Color.WHITE);
                    acceptBtn.addActionListener(e -> przyjmijZlecenie(id));
                    box.add(Box.createRigidArea(new Dimension(0, 10)));
                    box.add(acceptBtn);

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

    private void przyjmijZlecenie(String idZlecenia) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("PRZYJMIJ_ZLECENIE;" + idZlecenia);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Zlecenie zostało przyjęte.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new PrzyjecieTowaruForm();
            } else {
                JOptionPane.showMessageDialog(this, response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}
