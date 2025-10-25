package com.carilt01.schematictonbt.userInterface;

import javax.swing.*;

public class IntegerInput {

    private JFrame frame;

    public IntegerInput(JFrame frame) {
        this.frame = frame;
    }

    public int askForInt(String message) {
        int userInt = 0;
        boolean valid = false;

        while (!valid) {
            String input = JOptionPane.showInputDialog(null, message, "Input", JOptionPane.QUESTION_MESSAGE);
            if (input == null) {
                // User pressed cancel
                System.exit(0);
            }
            try {
                userInt = Integer.parseInt(input);
                valid = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid integer, please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        //JOptionPane.showMessageDialog(null, "You entered": + userInt, "Result", JOptionPane.INFORMATION_MESSAGE);


        return userInt;
    }

}
