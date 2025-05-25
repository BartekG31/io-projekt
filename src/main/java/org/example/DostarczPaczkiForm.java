package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class DostarczPaczkiForm extends JFrame {

    private JTable paczkiTable;
    private DefaultTableModel tableModel;

    public DostarczPaczkiForm() {
        setTitle("Dostarcz paczki");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(248, 248, 248));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Lista paczek w drodze");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Odbiorca");
        tableModel.addColumn("Opis");
        tableModel.addColumn("Waga");
        tableModel.addColumn("Data nadania");

        paczkiTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(paczkiTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton dostarczBtn = new JButton("Dostarcz paczkę");
        dostarczBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        dostarczBtn.setBackground(new Color(70, 105, 255));
        dostarczBtn.setForeground(Color.WHITE);
        dostarczBtn.setFocusPainted(false);
        dostarczBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dostarczBtn.setPreferredSize(new Dimension(200, 40));
        dostarczBtn.setMaximumSize(new Dimension(200, 40));
        dostarczBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        dostarczBtn.addActionListener(e -> dostarczPaczke());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(248, 248, 248));
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        bottomPanel.add(dostarczBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        wczytajPaczki();
    }

    private void wczytajPaczki() {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("POBIERZ_W_DRODZE");
            String response = in.readLine();

            if (response.startsWith("OK")) {
                String[] entries = response.split(";");
                for (int i = 1; i < entries.length; i++) {
                    String[] fields = entries[i].split("\\|");
                    tableModel.addRow(new Object[]{
                            Integer.parseInt(fields[0]),
                            fields[1],
                            fields[2],
                            Double.parseDouble(fields[3]),
                            fields[4]
                    });
                }
            } else {
                JOptionPane.showMessageDialog(this, "Brak paczek w drodze.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania paczek.");
        }
    }

    private void dostarczPaczke() {
        int selectedRow = paczkiTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz paczkę do dostarczenia.");
            return;
        }

        int idZlecenia = (int) tableModel.getValueAt(selectedRow, 0);

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("ZATWIERDZ_DOSTARCZENIE;" + idZlecenia);
            String response = in.readLine();

            if (response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Paczka oczekuję na odbiór'.");
                tableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Błąd podczas zatwierdzania paczki: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.");
        }
    }
}
