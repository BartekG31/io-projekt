package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class KurierForm extends JFrame {

    public KurierForm(String imie, String nazwisko) {
        setTitle("Panel główny - KURIER");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // Obsługa zamykania okna
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                wyloguj();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        panel.setBackground(new Color(248, 248, 248));

        JLabel title = new JLabel("Panel użytkownika: " + imie + " " + nazwisko);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title);

        panel.add(makeStyledButton("Odbierz paczki z magazynu", e -> new OdbiorPaczkiKurierForm()));
        panel.add(makeStyledButton("Dostarcz paczki", e -> new DostarczPaczkiForm()));
        panel.add(makeStyledButton("Zgłoś incydent", e -> new ZglosIncydentForm()));
        panel.add(makeStyledButton("Zakończ trasę", e -> new ZakonczTraseForm()));

        // Separator i przycisk wyloguj
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setBackground(Color.GRAY);
        panel.add(separator);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton wylogujButton = new JButton("🚪 Wyloguj");
        wylogujButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        wylogujButton.setBackground(new Color(220, 53, 69));
        wylogujButton.setForeground(Color.WHITE);
        wylogujButton.setFocusPainted(false);
        wylogujButton.setMaximumSize(new Dimension(200, 40));
        wylogujButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        wylogujButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wylogujButton.addActionListener(e -> wyloguj());

        JPanel wylogujWrapper = new JPanel(new BorderLayout());
        wylogujWrapper.setBackground(new Color(248, 248, 248));
        wylogujWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        wylogujWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        wylogujWrapper.add(wylogujButton, BorderLayout.CENTER);
        panel.add(wylogujWrapper);

        add(panel);
        setVisible(true);
    }

    private JPanel makeStyledButton(String text, java.util.function.Consumer<ActionEvent> action) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(70, 105, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setPreferredSize(new Dimension(400, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.addActionListener(e -> action.accept(e));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(248, 248, 248));
        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        wrapper.add(button, BorderLayout.CENTER);

        return wrapper;
    }

    private void wyloguj() {
        int wybor = JOptionPane.showConfirmDialog(
                this,
                "Czy na pewno chcesz się wylogować?",
                "Potwierdzenie wylogowania",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (wybor == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new client());
        }
    }
}