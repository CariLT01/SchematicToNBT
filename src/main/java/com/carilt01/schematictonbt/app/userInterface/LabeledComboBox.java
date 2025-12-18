package com.carilt01.schematictonbt.app.userInterface;

import javax.swing.*;
import java.awt.*;

public class LabeledComboBox extends JComboBox<LabeledOption> {

    public LabeledComboBox(LabeledOption[] options) {
        super(options);
        setRenderer(new OptionRenderer());
    }

    // Custom renderer for title + description
    private static class OptionRenderer implements ListCellRenderer<LabeledOption> {
        private final JPanel panel = new JPanel(new BorderLayout());
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();

        public OptionRenderer() {
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            descLabel.setFont(descLabel.getFont().deriveFont(Font.PLAIN, 11f));
            descLabel.setForeground(Color.GRAY);
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(descLabel, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends LabeledOption> list, LabeledOption value, int index,
                boolean isSelected, boolean cellHasFocus) {

            if (value != null) {


                titleLabel.setText(value.getTitle());

                // Use HTML to enable line wrapping
                String wrappedDesc = "<html><body style='width:200px;'>" + value.getDescription() + "</body></html>";
                descLabel.setText(wrappedDesc);

                //descLabel.setText(value.getDescription());
            } else {
                titleLabel.setText("");
                descLabel.setText("");
            }

            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
                titleLabel.setForeground(list.getSelectionForeground());
                descLabel.setForeground(list.getSelectionForeground());
            } else {
                panel.setBackground(list.getBackground());
                titleLabel.setForeground(list.getForeground());
                descLabel.setForeground(Color.GRAY);
            }

            return panel;
        }
    }
}