package com.carilt01.schematictonbt.userInterface;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ComponentWrapper {

    private ComponentWrapper() {}

    /**
     * Wrap a component in a JPanel for left alignment inside BoxLayout.Y_AXIS
     * @param component The component to wrap
     * @return JPanel wrapper
     */
    public static JPanel wrapComponent(JComponent component) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());   // Use BorderLayout to allow left alignment
        wrapper.add(component, BorderLayout.WEST); // Put content to the left
        wrapper.setOpaque(false);
        //wrapper.setBorder(new EmptyBorder(5, 0, 5, 0));

        // Make wrapper height tight around content
        Dimension preferred = component.getPreferredSize();
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
        wrapper.setPreferredSize(new Dimension(preferred.width, preferred.height));

        return wrapper;
    }
}