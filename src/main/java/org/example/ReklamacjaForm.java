package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class ReklamacjaForm extends JFrame {
    private final String odbiorca;
    private final JComboBox<String> zlecenieBox;
    private final JTextField produktField;
    private final JTextArea opisArea;

    public ReklamacjaForm(String odbiorca) {
        this.odbiorca = odbiorca;

        setTitle("Zgłoś reklamację");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Zgłoszenie reklamacji");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(new JLabel("Wybierz przesyłkę:"));
        zlecenieBox = new JComboBox<>();
        panel.add(zlecenieBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(new JLabel("Produkt:"));
        produktField = new JTextField();
        panel.add(produktField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(new JLabel("Opis reklamacji:"));
        opisArea = new JTextArea(5, 20);
        opisArea.setLineWrap(true);
        opisArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(opisArea));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton submitButton = new JButton("Zgłoś reklamację");
        submitButton.setBackground(new Color(255, 102, 102));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        submitButton.addActionListener(this::handleSubmit);
        panel.add(submitButton);

        add(panel);
        setVisible(true);

        pobierzZlecenia();
    }

    private void pobierzZlecenia() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("ZLECENIA_DO_REKLAMACJI;" + odbiorca);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] items = response.split(";");
                for (int i = 1; i < items.length; i++) {
                    zlecenieBox.addItem(items[i]);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Brak przesyłek do reklamacji");
                dispose();
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem");
            dispose();
        }
    }

    private void handleSubmit(ActionEvent e) {
        String wybrane = (String) zlecenieBox.getSelectedItem();
        if (wybrane == null || produktField.getText().isEmpty() || opisArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Uzupełnij wszystkie pola!");
            return;
        }

        String idZlecenia = wybrane.split("\\|")[0];

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("ZGLOS_REKLAMACJE;" + idZlecenia + ";" + produktField.getText().trim() + ";" + opisArea.getText().trim());
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Reklamacja zgłoszona.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.split(";", 2)[1]);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem");
        }
    }
}
