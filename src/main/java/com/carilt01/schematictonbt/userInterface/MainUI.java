package com.carilt01.schematictonbt.userInterface;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.carilt01.schematictonbt.Callback;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainUI {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainUI.class);
    private JFrame frame;
    private JProgressBar progressBar;
    private NativeFileChooser nativeFileChooser;
    private JLabel progressText;

    private final Callback callbacks;

    public MainUI(Callback callbacks) {
        this.callbacks = callbacks;
        this.initializeWindow();

    }

    private void initializeWindow() {



        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            logger.error("An error occurred while setting look and feel: ", e);
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

        JScrollPane scrollPane = getJScrollPane();

        JPanel actionsPanel = new JPanel();
        actionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.X_AXIS));

        // Buttons
        JButton loadSchematicFileButton = new JButton("Load schematic...");
        actionsPanel.add(loadSchematicFileButton);
        actionsPanel.add(Box.createHorizontalStrut(50));
        JButton executeGiveListButton = new JButton("Execute give list...");
        actionsPanel.add(executeGiveListButton);


        loadSchematicFileButton.addActionListener(e -> {

            File selectedFile = nativeFileChooser.chooseFile();
            if (selectedFile == null) return;

            callbacks.startProcessFile(selectedFile.getAbsolutePath());
        });

        mainMenuRoot.add(Box.createVerticalGlue()); // center vertically
        mainMenuRoot.add(title);
        mainMenuRoot.add(actionsPanel);
        mainMenuRoot.add(scrollPane);



        mainMenuRoot.add(Box.createVerticalGlue()); // center vertically
        frame.add(mainMenuRoot);
        frame.add(bottomRibbon, BorderLayout.SOUTH);





    }

    private static JScrollPane getJScrollPane() {
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME); // Root logger

        TextAreaAppender appender = new TextAreaAppender(logArea, 200);
        appender.setContext(context);
        appender.start();
        rootLogger.addAppender(appender);


        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(250, 250));
        scrollPane.setMaximumSize(new Dimension(600, 250));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        return scrollPane;
    }

    public void showWindow() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void setProgress(float progress) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(Math.round(progress * 100)));

    }

    public void setProgressBarVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> progressBar.setVisible(visible));

    }

    public void showError(String errorMessage) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                frame,
                errorMessage,
                "Schematic to NBT",
                JOptionPane.ERROR_MESSAGE
        ));

    }

    public void setProgressText(String text) {
        SwingUtilities.invokeLater(() -> progressText.setText(text));
    }

    public void setProgressTextVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> progressText.setVisible(visible));
    }

}

