package com.carilt01.schematictonbt;

import com.carilt01.schematictonbt.userInterface.IntegerInput;
import com.carilt01.schematictonbt.userInterface.MainUI;



public class Application {

    private final MainUI mainUI;
    private SchematicToNBTConverter converter;

    public Application() {

        converter = new SchematicToNBTConverter();

        ProgressCallback progressCallback = new ProgressCallback() {
            @Override
            public void update(float progress, String text) {
                System.out.println("Progress update:" + progress);
                mainUI.setProgress(progress);
                mainUI.setProgressText(text);
            }
        };

        Callback callbacks = new Callback() {
            @Override
            public void startProcessFile(String filePath) {

                IntegerInput integerInput = new IntegerInput(mainUI.getFrame());
                int maxVolumeSize = integerInput.askForInt("Maximum KB per file (default: 246)");
                mainUI.setProgressBarVisible(true);
                mainUI.setProgressTextVisible(true);
                new Thread(() -> {

                    progressCallback.update(0, "Loading...");

                    try {
                        converter.convertFile(filePath, true, maxVolumeSize * 1024, progressCallback);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mainUI.showError(e.getMessage());


                    }

                    progressCallback.update(1, "Done!");

                }).start();



            }

            @Override
            public void startExecuteGiveList(String filePath) {

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
