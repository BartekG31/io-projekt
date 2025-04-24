package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ProblemForm extends JFrame {
    private final int userId;
    private final JComboBox<String> zleceniaBox;
    private final JTextArea opisField;
    private final HashMap<String, String> zleceniaMap = new HashMap<>();

    public ProblemForm(int userId) {
        this.userId = userId;

        setTitle("Zgłoś problem z przesyłką");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Wybierz zlecenie:");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(label);

        zleceniaBox = new JComboBox<>();
        zleceniaBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        zleceniaBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(zleceniaBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel opisLabel = new JLabel("Opis problemu:");
        opisLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(opisLabel);

        opisField = new JTextArea(5, 30);
        opisField.setLineWrap(true);
        opisField.setWrapStyleWord(true);
        opisField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(opisField);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.add(scroll);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton sendBtn = new JButton("Zgłoś problem");
        sendBtn.setBackground(new Color(178, 34, 34));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> handleSubmit());

        panel.add(sendBtn);
        add(panel);
        setVisible(true);

        loadZlecenia();
    }

    private void loadZlecenia() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("HISTORIA_ZLECEN;" + userId);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] parts = response.split(";");
                for (int i = 1; i < parts.length; i++) {
                    String[] data = parts[i].split("\\|");
                    String opis = data[1];
                    String status = data[2];

                    if (status.equalsIgnoreCase("Zrealizowane")) {
                        String idZlecenia = String.valueOf(i);
                        String label = "Zlecenie: " + opis + " (" + data[3] + ")";
                        zleceniaBox.addItem(label);
                        zleceniaMap.put(label, idZlecenia);
                    }
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSubmit() {
        String selected = (String) zleceniaBox.getSelectedItem();
        String opis = opisField.getText().trim();

        if (selected == null || opis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wybierz zlecenie i wpisz opis problemu!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idZlecenia = zleceniaMap.get(selected);

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("ZGLOS_PROBLEM;" + idZlecenia + ";" + opis);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Problem zgłoszony pomyślnie!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.split(";", 2)[1], "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}
