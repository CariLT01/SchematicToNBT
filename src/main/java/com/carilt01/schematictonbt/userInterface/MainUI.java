package com.carilt01.schematictonbt.userInterface;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.carilt01.schematictonbt.Callback;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class MainUI {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainUI.class);
    private JFrame frame;
    private JProgressBar progressBar;
    private NativeFileChooser nativeFileChooser;
    private JLabel progressText;
    private JLabel chooseFileLabel;
    private JSpinner maximumKbPerFileSpinner;

    private File schematicSelectedFile;
    private File giveListSelectedFile;

    private final Callback callbacks;

    public MainUI(Callback callbacks) {
        this.callbacks = callbacks;
        this.initializeWindow();

    }

    private void initializeWindow() {



        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            logger.error("An error occurred while setting look and feel: ", e);
        }

        frame = new JFrame("Schematic to NBT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        nativeFileChooser = new NativeFileChooser(frame);

        ///  --- Bottom ribbon --- ///

        JPanel bottomRibbon = new JPanel();
        bottomRibbon.setLayout(new BoxLayout(bottomRibbon, BoxLayout.X_AXIS));
        bottomRibbon.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        progressText = new JLabel("Loading...");
        progressText.setAlignmentY(Component.CENTER_ALIGNMENT);

        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(37);
        progressBar.setPreferredSize(new Dimension(300, 5));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        progressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
        progressBar.setStringPainted(false);


        bottomRibbon.add(Box.createHorizontalGlue()); // push everything to the right
        bottomRibbon.add(Box.createHorizontalStrut(5));
        bottomRibbon.add(progressText);
        bottomRibbon.add(Box.createHorizontalStrut(5)); // space between label and bar
        bottomRibbon.add(progressBar);

        /// --- Tabs ---
        JTabbedPane tabbedPane = new JTabbedPane();

        /// --- Schematic conversion tab ---

        JPanel schematicConverterTabPanel = new JPanel();
        schematicConverterTabPanel.setLayout(new BoxLayout(schematicConverterTabPanel, BoxLayout.Y_AXIS));
        JScrollPane schematicConverterTabPanelScrollPane = new JScrollPane(schematicConverterTabPanel);

        //schematicConverterTabPanel.add(Box.createVerticalStrut(10)); // 10px vertical gap
        //schematicConverterTabPanel.add(Box.createHorizontalStrut(10)); // 10px horizontal gap

        //schematicConverterTabPanel.setLayout(new BoxLayout(schematicConverterTabPanel ,BoxLayout.Y_AXIS));

        tabbedPane.add("Convert schematic to NBT", schematicConverterTabPanelScrollPane);
        JScrollPane scrollPane = getJScrollPane();

        // Title
        JLabel title = new JLabel("Convert schematic to NBT");
        title.setFont(new Font("Roboto", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Wrapper panel
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
        wrapperPanel.setBorder(new EmptyBorder(0, 15, 0, 15));


        // Select file panel
        JPanel selectFilePanel = new JPanel();
        selectFilePanel.setLayout(new BoxLayout(selectFilePanel, BoxLayout.X_AXIS));
        selectFilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Choose file label
        chooseFileLabel = new JLabel("No file chosen");
        JButton chooseFileButton = new JButton("Choose file");

        selectFilePanel.add(chooseFileLabel);
        selectFilePanel.add(Box.createHorizontalStrut(5));
        selectFilePanel.add(chooseFileButton);

        chooseFileButton.addActionListener(e -> {
            File selectedFile = nativeFileChooser.chooseFile();
            if (selectedFile == null) return;

            schematicSelectedFile = selectedFile;
            chooseFileLabel.setText("<html><b>Chosen file</b>: " + selectedFile.getName() + "</html>");
        });


        wrapperPanel.add(ComponentWrapper.wrapComponent(selectFilePanel));
        wrapperPanel.add(Box.createVerticalStrut(5));

        // Maximum KB per NBT file panel
        JPanel maximumKbPerFilePanel = new JPanel();
        maximumKbPerFilePanel.setLayout(new BoxLayout(maximumKbPerFilePanel, BoxLayout.X_AXIS));

        JLabel maximumKbPerFileInputLabel = new JLabel("Maximum KB per file: ");
        SpinnerNumberModel model = new SpinnerNumberModel(246, 2, 16384, 2);
        maximumKbPerFileSpinner = new JSpinner(model);

        maximumKbPerFilePanel.add(maximumKbPerFileInputLabel);
        maximumKbPerFilePanel.add(maximumKbPerFileSpinner);
        wrapperPanel.add(ComponentWrapper.wrapComponent(maximumKbPerFilePanel));
        wrapperPanel.add(Box.createVerticalStrut(5));


        // Convert button
        JButton convertFileButton = new JButton("Convert");
        convertFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        convertFileButton.addActionListener(e -> {

            if (schematicSelectedFile == null) {
                this.showError("You must select a file before converting!");
                return;
            }

            callbacks.startProcessFile(schematicSelectedFile.getAbsolutePath());
        });

        schematicConverterTabPanel.add(title);
        schematicConverterTabPanel.add(wrapperPanel);
        schematicConverterTabPanel.add(convertFileButton);
        schematicConverterTabPanel.add(Box.createVerticalStrut(15));
        schematicConverterTabPanel.add(scrollPane);

        ///  --- Execute give list tab ---
        JPanel executeGiveListPanel = new JPanel();
        executeGiveListPanel.setLayout(new BoxLayout(executeGiveListPanel, BoxLayout.Y_AXIS));
        JScrollPane executeGiveListScroll = new JScrollPane(executeGiveListPanel);

        ///  --- Title ---
        JLabel title2  = new JLabel("Execute give list");
        title2.setFont(new Font("Roboto", Font.BOLD, 18));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        executeGiveListPanel.add(title2);

        ///  --- Wrapper panel ---
        JPanel wrapperPanel2 = new JPanel();
        wrapperPanel2.setLayout(new BoxLayout(wrapperPanel2, BoxLayout.Y_AXIS));
        wrapperPanel2.setBorder(new EmptyBorder(0, 15, 0, 15));

        ///  --- File input ---
        JPanel fileInputPanel = new JPanel();
        fileInputPanel.setLayout(new BoxLayout(fileInputPanel, BoxLayout.X_AXIS));

        JLabel fileInputLabel = new JLabel("No file chosen");
        JButton giveListInputButton = new JButton("Choose file");
        giveListInputButton.addActionListener(e -> {
            giveListSelectedFile = nativeFileChooser.chooseFile();
            if (giveListSelectedFile != null) {
                fileInputLabel.setText("<html><b>Chosen file</b>: " + giveListSelectedFile.getName() + "</html>");
            }

        });

        fileInputPanel.add(fileInputLabel);
        fileInputPanel.add(Box.createHorizontalStrut(5));
        fileInputPanel.add(giveListInputButton);

        wrapperPanel2.add(ComponentWrapper.wrapComponent(fileInputPanel));
        executeGiveListPanel.add(wrapperPanel2);
        executeGiveListPanel.add(Box.createVerticalStrut(15));

        ///  --- Instructions --
        JLabel instructions = new JLabel("<html><b>Instructions</b><br>Right arrow to type next. Left arrow to go back. Down arrow to retype.</html>");
        wrapperPanel2.add(ComponentWrapper.wrapComponent(instructions));

        ///  --- Execute button ---
        JButton executeGiveListButton = new JButton("Execute list");
        executeGiveListButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        executeGiveListButton.addActionListener(e -> {
            if (giveListSelectedFile == null) {
                this.showError("You must select file to execute!");
                return;
            }

            callbacks.executeGiveList(giveListSelectedFile.getAbsolutePath());
        });


        executeGiveListPanel.add(executeGiveListButton);



        tabbedPane.add("Execute give list", executeGiveListScroll);



        frame.add(tabbedPane);
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
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(progress < 0);
            progressBar.setValue(Math.round(progress * 100));
        });

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
    public void showWarning(String errorMessage) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                frame,
                errorMessage,
                "Schematic to NBT",
                JOptionPane.WARNING_MESSAGE
        ));

    }

    public void setProgressText(String text) {
        SwingUtilities.invokeLater(() -> progressText.setText(text));
    }

    public void setProgressTextVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> progressText.setVisible(visible));
    }

    public int getMaxKbPerFileValue() {
        return (Integer) this.maximumKbPerFileSpinner.getValue();
    }

}

