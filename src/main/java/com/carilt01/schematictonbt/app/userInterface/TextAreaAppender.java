package com.carilt01.schematictonbt.app.userInterface;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.JTextArea;

public class TextAreaAppender extends AppenderBase<ILoggingEvent> {

    private final JTextArea textArea;
    private final int maxLines; // max number of lines to keep
    private static final Logger logger = LoggerFactory.getLogger(TextAreaAppender.class);

    public TextAreaAppender(JTextArea textArea, int maxLines) {
        this.textArea = textArea;
        this.maxLines = maxLines;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String message = eventObject.getFormattedMessage() + "\n";

        SwingUtilities.invokeLater(() -> {
            textArea.append(message);

            // Remove old lines if exceeding maxLines
            int lines = textArea.getLineCount();
            if (lines > maxLines) {
                try {
                    int endOfFirstLine = textArea.getLineEndOffset(lines - maxLines - 1);
                    textArea.replaceRange("", 0, endOfFirstLine);
                } catch (Exception e) {
                    logger.error("An error occurred while appending lines: ", e);
                }
            }

            // Scroll to bottom
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
