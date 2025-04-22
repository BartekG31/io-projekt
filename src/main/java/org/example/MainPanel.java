package org.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainPanel extends JFrame {

    public MainPanel(String pelneImieNazwisko, String rola) {
        setTitle("Panel główny - " + rola);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("  Panel użytkownika: " + rola.toUpperCase(), JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(title);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(248, 248, 248));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        switch (rola.toUpperCase()) {
            case "KLIENT" -> {
                buttonPanel.add(button("Zleć transport", () -> new ZlecenieForm()));
                buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                buttonPanel.add(button("Zatwierdź odbiór przesyłki", () -> new OdbiorForm(pelneImieNazwisko)));
                buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                buttonPanel.add(button("Przeglądaj historię zleceń", () ->
                        JOptionPane.showMessageDialog(this, "Opcja jeszcze niezaimplementowana")));
                buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                buttonPanel.add(button("Zgłoś problem z przesyłką", () ->
                        JOptionPane.showMessageDialog(this, "Opcja jeszcze niezaimplementowana")));
                buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                buttonPanel.add(button("Zgłoś reklamację", () ->
                        JOptionPane.showMessageDialog(this, "Opcja jeszcze niezaimplementowana")));
                buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                buttonPanel.add(button("Zaktualizuj dane kontaktowe", () ->
                        JOptionPane.showMessageDialog(this, "Opcja jeszcze niezaimplementowana")));
            }
            default -> {
                JLabel label = new JLabel("Brak dostępnych funkcji dla tej roli.", JLabel.CENTER);
                label.setFont(new Font("SansSerif", Font.PLAIN, 14));
                buttonPanel.add(label);
            }
        }

        mainPanel.add(buttonPanel);
        add(mainPanel);
        setVisible(true);
    }

    private JPanel button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(new Color(70, 105, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);
        button.addActionListener(e -> action.run());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(button, BorderLayout.CENTER);

        return wrapper;
    }
}
