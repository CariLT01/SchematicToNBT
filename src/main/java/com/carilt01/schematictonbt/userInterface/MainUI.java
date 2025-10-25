package com.carilt01.schematictonbt.userInterface;

import com.carilt01.schematictonbt.Callback;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.annotation.Native;

public class MainUI {

    private JFrame frame;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private NativeFileChooser nativeFileChooser;
    private JLabel progressText;

    private Callback callbacks;

    public MainUI(Callback callbacks) {
        this.callbacks = callbacks;
        this.initializeWindow();

    }

    private void initializeWindow() {



        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Schematic to NBT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        nativeFileChooser = new NativeFileChooser(frame);

        JPanel mainMenuRoot = new JPanel();
        mainMenuRoot.setLayout(new BoxLayout(mainMenuRoot ,BoxLayout.Y_AXIS));

        JPanel bottomRibbon = new JPanel();
        bottomRibbon.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));




        progressText = new JLabel("Loading...");


        bottomRibbon.add(progressText);

        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(37);
        progressBar.setPreferredSize(new Dimension(300, 20));
        progressBar.setStringPainted(true);



        bottomRibbon.add(progressBar);

        JLabel title = new JLabel("Schematic to NBT");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(250, 250));
        scrollPane.setMaximumSize(new Dimension(600, 250));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.X_AXIS));

        // Buttons
        JButton loadSchematicFileButton = new JButton("Load schematic...");
        actionsPanel.add(loadSchematicFileButton);
        actionsPanel.add(Box.createHorizontalStrut(50));
        JButton executeGiveListButton = new JButton("Execute give list...");
        actionsPanel.add(executeGiveListButton);


        loadSchematicFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                File selectedFile = nativeFileChooser.chooseFile();
                if (selectedFile == null) return;

                callbacks.startProcessFile(selectedFile.getAbsolutePath());
            }
        });

        mainMenuRoot.add(Box.createVerticalGlue()); // center vertically
        mainMenuRoot.add(title);
        mainMenuRoot.add(actionsPanel);
        mainMenuRoot.add(scrollPane);



        mainMenuRoot.add(Box.createVerticalGlue()); // center vertically
        frame.add(mainMenuRoot);
        frame.add(bottomRibbon, BorderLayout.SOUTH);





    }

    public void showWindow() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void setProgress(float progress) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(Math.round(progress * 100));
        });

    }

    public void setProgressBarVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(visible);
        });

    }

    public void showError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    frame,
                    errorMessage,
                    "Schematic to NBT",
                    JOptionPane.ERROR_MESSAGE
            );
        });

    }

    public JFrame getFrame() {
        return frame;
    }

    public void setProgressText(String text) {
        SwingUtilities.invokeLater(() -> {
            progressText.setText(text);
        });
    }

    public void setProgressTextVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            progressText.setVisible(visible);
        });
    }

}

