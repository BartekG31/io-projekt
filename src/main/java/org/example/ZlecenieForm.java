package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class ZlecenieForm extends JFrame {
    private final JTextField odbiorcaField;
    private final JTextField adresField;
    private final JTextField miastoField;
    private final JTextField kodPocztowyField;
    private final JTextField opisField;
    private final JTextField wagaField;
    private final JTextField dataField;

    public ZlecenieForm() {
        setTitle("Zleć transport");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Formularz przesyłki");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        formPanel.add(title);

        odbiorcaField = addField(formPanel, "Imię i nazwisko odbiorcy:");
        adresField = addField(formPanel, "Adres odbiorcy:");
        miastoField = addField(formPanel, "Miasto:");
        kodPocztowyField = addField(formPanel, "Kod pocztowy:");
        opisField = addField(formPanel, "Opis przesyłki:");
        wagaField = addField(formPanel, "Waga (kg):");
        dataField = addField(formPanel, "Data nadania (YYYY-MM-DD):");

        JButton submitButton = new JButton("Wyślij zlecenie");
        submitButton.setBackground(new Color(70, 105, 255));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.setFocusPainted(false);
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        submitButton.addActionListener(this::handleSubmit);

        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(submitButton);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        setContentPane(contentPanel);
        setVisible(true);
    }

    private JTextField addField(JPanel parent, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        parent.add(label);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        parent.add(field);
        parent.add(Box.createRigidArea(new Dimension(0, 15)));

        return field;
    }

    private void handleSubmit(ActionEvent e) {
        if (odbiorcaField.getText().isEmpty() || adresField.getText().isEmpty() ||
                miastoField.getText().isEmpty() || kodPocztowyField.getText().isEmpty() ||
                opisField.getText().isEmpty() || wagaField.getText().isEmpty() ||
                dataField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wypełnij wszystkie pola!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String komunikat = String.join(";",
                    "DODAJ_ZLECENIE",
                    odbiorcaField.getText(),
                    adresField.getText(),
                    miastoField.getText(),
                    kodPocztowyField.getText(),
                    opisField.getText(),
                    wagaField.getText(),
                    dataField.getText()
            );

            out.println(komunikat);
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