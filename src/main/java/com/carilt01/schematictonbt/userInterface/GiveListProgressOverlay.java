package com.carilt01.schematictonbt.userInterface;

import com.formdev.flatlaf.FlatIntelliJLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class GiveListProgressOverlay {

    private JWindow window;
    private final Logger logger = LoggerFactory.getLogger(GiveListProgressOverlay.class);

    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JTextArea listArea;
    private JScrollPane listScrollPane;

    public GiveListProgressOverlay() {
        initialize();
    }

    public void scrollToLine(final int requestedLine) {
        // run on EDT and wait a tiny bit for layout to settle
        SwingUtilities.invokeLater(() -> {
            if (listArea == null || listScrollPane == null) return;

            int lineCount = listArea.getLineCount();
            if (lineCount == 0) return;

            // clamp requested line
            int line = Math.max(0, Math.min(requestedLine, lineCount - 1));

            try {
                // offset of the start of that line
                int startOffset = listArea.getLineStartOffset(line);

                // modelToView2D is the non-deprecated method
                Rectangle2D r2 = listArea.modelToView2D(startOffset);
                if (r2 == null) return;
                Rectangle lineRect = r2.getBounds();

                // get viewport and current view rect
                JViewport viewport = listScrollPane.getViewport();
                Rectangle viewRect = viewport.getViewRect();

                // center the line vertically inside the viewport (or change formula to top-align)
                int targetY = lineRect.y - (viewRect.height - lineRect.height) / 2;
                if (targetY < 0) targetY = 0;

                // ensure we don't request a position beyond content height
                int maxY = listArea.getHeight() - viewRect.height;
                if (targetY > maxY) targetY = Math.max(0, maxY);

                // move the viewport
                viewport.setViewPosition(new Point(viewRect.x, targetY));

                // optional: move caret to the line so keyboard/focus reflect this
                listArea.setCaretPosition(startOffset);

            } catch (BadLocationException ex) {
                logger.error("Bad location: ", ex);
            }
        });
    }

    private void initialize() {

        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            logger.error("An error occurred while setting look and feel: ", e);
        }

        window = new JWindow();
        window.setBackground(new Color(1.f, 1f, 1f, 0.5f));
        window.setAlwaysOnTop(true);

        int WINDOW_WIDTH = 600;
        int WINDOW_HEIGHT = 150;
        window.setBounds(50, 50, WINDOW_WIDTH, WINDOW_HEIGHT);
        window.setShape(new RoundRectangle2D.Double(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 16, 16));

        // Root panel with transparent background
        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setOpaque(false); // allow transparency
        window.add(rootPanel);

        // Center panel: auto height, full width
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        int margin = 5;
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, margin, 0, margin));

        // Text area
        listArea = new JTextArea();
        listArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        //listArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        //listArea.setPreferredSize(new Dimension(0, 50));

        listScrollPane = new JScrollPane(listArea);
        listScrollPane.setPreferredSize(new Dimension(0, 80));
        listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(listScrollPane);
        centerPanel.add(Box.createVerticalStrut(5));

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(67);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT); // center inside BoxLayout
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // width = 100%

        centerPanel.add(progressBar);

        // Text
        statusLabel = new JLabel("<html>grass_block <b>•</b> <b>stone_block</b> <b>•</b> iron_trapdooor</html>");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Windows system font
        centerPanel.add(statusLabel);

        // GridBagConstraints to center vertically and horizontally
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;       // full width
        gbc.weighty = 1.0;       // take remaining space
        gbc.anchor = GridBagConstraints.CENTER; // center in parent
        gbc.fill = GridBagConstraints.HORIZONTAL; // full width

        rootPanel.add(centerPanel, gbc);






    }

    public void setWindowVisible(boolean v) {
        window.setVisible(v);
    }

    public void setListAreaText(String text) {
        SwingUtilities.invokeLater(() -> {
            this.listArea.setText(text);
        });

    }
    public void setProgressBarValue(int value) {
        SwingUtilities.invokeLater(() -> {
            this.progressBar.setValue(value);
        });

    }
    public void setStatusLabelText(String text) {
        SwingUtilities.invokeLater(() -> {
            this.statusLabel.setText(text);
        });

    }
    public void setScrollIndex(int index) {
        SwingUtilities.invokeLater(() -> {
            this.scrollToLine(index);
        });
    }
}
