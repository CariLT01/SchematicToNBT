package com.carilt01.schematictonbt.userInterface;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class NativeFileChooser {

    JFrame frame;

    public NativeFileChooser(JFrame parent) {
        frame = parent;
    }

    public File chooseFile() {
        FileDialog fileDialog = new FileDialog(frame, "Select a file", FileDialog.LOAD);
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();
        if (directory != null && filename != null) {
            return new File(directory, filename);
        }
        return null;
    }
}
