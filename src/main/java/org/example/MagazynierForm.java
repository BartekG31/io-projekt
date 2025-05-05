package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MagazynierForm extends JFrame {

    public MagazynierForm(String imie, String nazwisko) {
        setTitle("Panel główny - MAGAZYNIER");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        panel.setBackground(new Color(248, 248, 248));

        JLabel title = new JLabel("Panel użytkownika: " + imie + " " + nazwisko);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title);

        panel.add(makeStyledButton("Przygotuj paczki do wysyłki", e -> new PrzygotujPaczkiForm()));
        panel.add(makeStyledButton("Przyjęcie towaru", e -> {
            // TODO
        }));
        panel.add(makeStyledButton("Inwentaryzacja", e -> {
            // TODO
        }));
        panel.add(makeStyledButton("Kontrola jakości towaru", e -> {
            // TODO
        }));
        panel.add(makeStyledButton("Zgłoś brak towaru", e -> {
            // TODO
        }));

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
}
