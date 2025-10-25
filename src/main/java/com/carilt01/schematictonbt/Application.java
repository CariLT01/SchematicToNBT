package com.carilt01.schematictonbt;

import com.carilt01.schematictonbt.userInterface.IntegerInput;
import com.carilt01.schematictonbt.userInterface.MainUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;


public class Application {


    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private final MainUI mainUI;
    private final SchematicToNBTConverter converter;

    public Application() {

        converter = new SchematicToNBTConverter();

        ProgressCallback progressCallback = new ProgressCallback() {
            @Override
            public void update(float progress, String text) {
                //logger.info("Progress update:{}", progress);
                mainUI.setProgress(progress);
                mainUI.setProgressText(text);
            }
            @Override
            public void showWarning(String message) {
                SwingUtilities.invokeLater(() -> {
                    mainUI.showWarning(message);
                });
            }
        };

        Callback callbacks = new Callback() {
            @Override
            public void startProcessFile(String filePath) {

                IntegerInput integerInput = new IntegerInput();
                int maxVolumeSize = integerInput.askForInt("Maximum KB per file (default: 246)");
                mainUI.setProgressBarVisible(true);
                mainUI.setProgressTextVisible(true);
                new Thread(() -> {

                    progressCallback.update(0, "Loading...");

                    try {
                        converter.convertFile(filePath, true, maxVolumeSize * 1024, progressCallback);
                    } catch (Exception e) {
                        logger.error("An error occurred while saving file: ", e);
                        mainUI.showError(e.getMessage());


                    }

                    progressCallback.update(1, "Done!");

                }).start();



            }

        };

        mainUI = new MainUI(callbacks);
    }

    public void run() {

        this.mainUI.setProgressBarVisible(false);
        this.mainUI.setProgressTextVisible(false);

        this.mainUI.showWindow();
    }
}
