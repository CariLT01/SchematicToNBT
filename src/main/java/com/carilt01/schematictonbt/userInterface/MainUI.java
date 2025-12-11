package com.carilt01.schematictonbt.userInterface;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.carilt01.schematictonbt.Callback;
import com.formdev.flatlaf.FlatIntelliJLaf;
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

    public final LabeledOption defaultQualityOption = new LabeledOption("Default Quality", "Balanced Adaptive Splitter – Balances speed and quality");
    public final LabeledOption enhancedQualityOption = new LabeledOption("High Precision", "Precise Adaptive Splitter – Higher quality results but slower speed. Results may vary.");
    public final LabeledOption binarySearchQualityOption = new LabeledOption("[Experimental] Binary Search", "Experimental Splitter - Faster speed than High Precision. Quality varies, but higher than Default.");

    public LabeledComboBox qualityComboBox;

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
        // Balanced, fast, precise combo box

        LabeledOption[] options = {
                defaultQualityOption,
                enhancedQualityOption,
                binarySearchQualityOption
        };

        qualityComboBox = new LabeledComboBox(options);

        wrapperPanel.add(ComponentWrapper.wrapComponent(qualityComboBox));



        // Convert button
        JButton convertFileButton = getJButton();

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
        JLabel instructions = new JLabel("<html><b>Instructions</b><br>Page up to type next. Page down to go back. Insert to retype.</html>");
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

        ///  --- Give list text area ---

        JTextArea giveListExecutorArea = new JTextArea();
        giveListExecutorArea.setEditable(false);
        giveListExecutorArea.setMaximumSize(new Dimension(200, 200));

        JScrollPane giveListScrollable = new JScrollPane(giveListExecutorArea);
        giveListScrollable.setPreferredSize(new Dimension(250, 250));
        giveListScrollable.setMaximumSize(new Dimension(600, 250));

        executeGiveListPanel.add(giveListScrollable);

        ///  -- List summary ---
        JPanel listSummary = new JPanel(new GridBagLayout());
        listSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
        listSummary.setBorder(new RoundedLineBorder(Color.LIGHT_GRAY, 1, 8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); // small padding
        gbc.gridy = 0;

// Previous
        JLabel previousBlockLabel = new JLabel("stone_block");
        previousBlockLabel.setFont(new Font("Roboto", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        listSummary.add(previousBlockLabel, gbc);

// Current
        JLabel currentBlockLabel = new JLabel("grass_block");
        currentBlockLabel.setFont(new Font("Roboto", Font.BOLD, 16));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        currentBlockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        listSummary.add(currentBlockLabel, gbc);

// Next
        JLabel nextBlockLabel = new JLabel("dirt_block");
        nextBlockLabel.setFont(new Font("Roboto", Font.PLAIN, 12));
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        listSummary.add(nextBlockLabel, gbc);

        listSummary.setPreferredSize(new Dimension(200, listSummary.getPreferredSize().height));

        JPanel wrappedListSummary = ComponentWrapper.wrapComponentCenter(listSummary);
        wrappedListSummary.setMaximumSize(new Dimension(500, listSummary.getPreferredSize().height));
        executeGiveListPanel.add(wrappedListSummary);




        tabbedPane.add("Execute give list", executeGiveListScroll);



        frame.add(tabbedPane);
        frame.add(bottomRibbon, BorderLayout.SOUTH);





    }

    private JButton getJButton() {
        JButton convertFileButton = new JButton("Convert");
        convertFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        convertFileButton.putClientProperty("JButton.buttonType", "default"); // ← primary button

        convertFileButton.addActionListener(e -> {

            if (schematicSelectedFile == null) {
                this.showError("You must select a file before converting!");
                return;
            }

            callbacks.startProcessFile(schematicSelectedFile.getAbsolutePath());
        });
        return convertFileButton;
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

    /*
    --- Code to uncomment once needed ---
    public void setPreviousBlockLabel(String text) {
        previousBlockLabel.setText(text);
    }
    public void setCurrentBlockLabel(String text) {
        currentBlockLabel.setText(text);
    }
    public void setNextBlockLabel(String text) {
        nextBlockLabel.setText(text);
    }
    public void setGiveListExecutorArea(String text) {
        giveListExecutorArea.setText(text);
    } */

    public LabeledComboBox getQualityComboBox() {
        return this.qualityComboBox;
    }


}

