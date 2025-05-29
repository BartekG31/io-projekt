package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LogistykForm extends JFrame {

    public LogistykForm(String imie, String nazwisko) {
        setTitle("Panel główny - LOGISTYK");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(550, 650);
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

        JLabel title = new JLabel("Panel logistyka: " + imie + " " + nazwisko);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title);

        // Sekcja zarządzania flotą
        panel.add(createSectionLabel("🚛 Zarządzanie flotą"));
        panel.add(makeStyledButton("Zarządzaj pojazdami", e -> new ZarzadzajPojazdamiForm()));
        panel.add(makeStyledButton("Przypisz pojazd do zlecenia", e -> new PrzypisaniePojazduForm()));
        panel.add(makeStyledButton("Przypisz kierowców do pojazdów", e -> new PrzypisanieKierowcowForm()));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja planowania tras
        panel.add(createSectionLabel("🗺️ Planowanie tras"));
        panel.add(makeStyledButton("Przypisz trasę kierowcy", e -> new PrzypisanieTrasy()));
        panel.add(makeStyledButton("Monitoruj trasy pojazdów", e -> new MonitorowanieTrasForm()));
        panel.add(makeStyledButton("Utwórz harmonogram", e -> new HarmonogramForm()));

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja monitorowania
        panel.add(createSectionLabel("📊 Monitorowanie i raporty"));
        panel.add(makeStyledButton("Status wszystkich zleceń", e -> new StatusZlecenForm()));
        panel.add(makeStyledButton("Przegląd incydentów", e -> new PrzegladIncydentowForm()));
        panel.add(makeStyledButton("Generuj raport miesięczny", e -> new RaportMiesiecznyForm()));

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sekcja zarządzania
        panel.add(createSectionLabel("⚙️ Zarządzanie"));
        panel.add(makeStyledButton("Przeglądaj raporty tras", e -> new RaportyTrasForm()));
        panel.add(makeStyledButton("Ustawienia systemu", e -> {
            JOptionPane.showMessageDialog(this, "Funkcja w trakcie rozwoju", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        }));

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

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(10, 5, 5, 0));
        return label;
    }

    private JPanel makeStyledButton(String text, java.util.function.Consumer<ActionEvent> action) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(new Color(40, 167, 69));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setPreferredSize(new Dimension(450, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusable(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(34, 139, 34)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.addActionListener(e -> action.accept(e));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(248, 248, 248));
        wrapper.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
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