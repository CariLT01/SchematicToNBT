package com.carilt01.schematictonbt.app;

import com.carilt01.schematictonbt.app.converter.SchematicToNBTConverter;
import com.carilt01.schematictonbt.app.converter.callbacks.Callback;
import com.carilt01.schematictonbt.app.converter.callbacks.GiveListExecutorCallbacks;
import com.carilt01.schematictonbt.app.converter.callbacks.ProgressCallback;
import com.carilt01.schematictonbt.app.giveListExecutor.GiveListExecutor;
import com.carilt01.schematictonbt.app.giveListExecutor.RobotTyper;
import com.carilt01.schematictonbt.app.userInterface.GiveListProgressOverlay;
import com.carilt01.schematictonbt.app.userInterface.LabeledOption;
import com.carilt01.schematictonbt.app.userInterface.MainUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;


public class Application {


    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private final MainUI mainUI;
    private final SchematicToNBTConverter converter;
    private final GiveListProgressOverlay overlay;

    public Application() {

        RobotTyper.initializeHook();
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

        overlay = new GiveListProgressOverlay();

        GiveListExecutorCallbacks executorCallbacks = new GiveListExecutorCallbacks() {
            @Override
            public void setStatus(String text) {
                overlay.setStatusLabelText(text);
            }

            @Override
            public void setProgress(int progress) {
                overlay.setProgressBarValue(progress);
            }

            @Override
            public void setTextArea(String text) {
                overlay.setListAreaText(text);
            }
            @Override
            public void finished() {
                overlay.setWindowVisible(false);
            }
            @Override
            public void scrollToLine(int index) {
                overlay.scrollToLine(index);
            }
        };

        Callback callbacks = new Callback() {
            @Override
            public void startProcessFile(String filePath) {

                int maxVolumeSize = mainUI.getMaxKbPerFileValue();
                mainUI.setProgressBarVisible(true);
                mainUI.setProgressTextVisible(true);

                float quality;
                LabeledOption selectedOption = (LabeledOption) mainUI.getQualityComboBox().getSelectedItem();
                if (selectedOption == mainUI.enhancedQualityOption) {
                    quality = 1.1f;
                } else {
                    quality = 2;
                }

                boolean isBinarySearch = selectedOption == mainUI.binarySearchQualityOption;

                new Thread(() -> {

                    progressCallback.update(0, "Loading...");

                    try {
                        converter.convertFile(filePath, true, maxVolumeSize * 1024, progressCallback, quality, isBinarySearch);
                    } catch (Exception e) {
                        logger.error("An error occurred while saving file: ", e);
                        mainUI.showError(e.getMessage());


                    }

                    progressCallback.update(1, "Done!");

                }).start();



            }
            @Override
            public void executeGiveList(String filePath) {
                overlay.setWindowVisible(true);
                new Thread(() -> {
                    mainUI.setProgressBarVisible(true);
                    mainUI.setProgressTextVisible(true);

                    GiveListExecutor executor = new GiveListExecutor();
                    try {
                        executor.executeGiveList(filePath, progressCallback, executorCallbacks);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
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
