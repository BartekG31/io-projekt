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
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Zlecenia do odbioru: " + odbiorca);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_DO_ODBIORU;" + odbiorca);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");

                if (parts.length == 1) {
                    JLabel label = new JLabel("Brak zleceń do odbioru.");
                    label.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panel.add(label);
                }

                for (int i = 1; i < parts.length; i++) {
                    String[] data = parts[i].split("\\|");
                    String id = data[0];
                    String opis = data[1];
                    String waga = data[2];
                    String dataNadania = data[3];
                    String nadawca = data[4];

                    JPanel box = new JPanel();
                    box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
                    box.setBackground(new Color(245, 245, 245));
                    box.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.GRAY),
                            new EmptyBorder(10, 15, 10, 15)
                    ));
                    box.setAlignmentX(Component.CENTER_ALIGNMENT);
                    box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

                    JLabel info = new JLabel("<html><b>Opis:</b> " + opis +
                            "<br><b>Waga:</b> " + waga + " kg" +
                            "<br><b>Data nadania:</b> " + dataNadania +
                            "<br><b>Nadawca:</b> " + nadawca + "</html>");
                    info.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    box.add(info);

                    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    buttonRow.setBackground(box.getBackground());

                    JButton accept = new JButton("Zatwierdź");
                    JButton reject = new JButton("Odrzuć");

                    accept.setBackground(new Color(34, 139, 34));
                    accept.setForeground(Color.WHITE);
                    accept.addActionListener(ev -> wyslijAkcje(id, "ZATWIERDZ_POJEDYNCZE"));

                    reject.setBackground(new Color(178, 34, 34));
                    reject.setForeground(Color.WHITE);
                    reject.addActionListener(ev -> wyslijAkcje(id, "ODRZUC_POJEDYNCZE"));

                    buttonRow.add(accept);
                    buttonRow.add(reject);
                    box.add(Box.createRigidArea(new Dimension(0, 10)));
                    box.add(buttonRow);

                    panel.add(box);
                    panel.add(Box.createRigidArea(new Dimension(0, 15)));
                }

            } else {
                JLabel label = new JLabel("Błąd: " + response.split(";", 2)[1]);
                label.setForeground(Color.RED);
                panel.add(label);
            }

        } catch (IOException ex) {
            JLabel error = new JLabel("Błąd połączenia z serwerem");
            error.setForeground(Color.RED);
            panel.add(error);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        setContentPane(scrollPane);
        setVisible(true);
    }

    private void wyslijAkcje(String id, String komenda) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(komenda + ";" + id);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, response.split(";", 2)[1], "Sukces", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new OdbiorForm(odbiorca);
            } else {
                JOptionPane.showMessageDialog(this, response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}