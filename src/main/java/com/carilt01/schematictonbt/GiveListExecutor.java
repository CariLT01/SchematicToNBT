package com.carilt01.schematictonbt;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Native;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GiveListExecutor {
    private final RobotTyper robotTyper;
    private final Logger logger = LoggerFactory.getLogger(GiveListExecutor.class);

    public GiveListExecutor() {
        robotTyper = new RobotTyper();
    }

    private String getBlockNameFromGiveCommand(String command) {
        return command.split("@s ")[1].replace("minecraft:", "");
    }
    public String stripNumber(String formattedCommand) {
        return formattedCommand.split(" ")[0];
    }

    public String makeList(List<String> giveList, int index) {
        StringBuilder finalString = new StringBuilder();

        int i = 0;
        for (String item : giveList) {
            if (i != index) {
                finalString.append("   /").append(item).append("\n");
            } else {
                finalString.append(">>>/").append(item).append("\n");
            }
            i++;
        }
        return finalString.toString();
    }


    public void executeGiveList(String giveListPath, ProgressCallback callback, GiveListExecutorCallbacks executorCallbacks) throws Exception {
        logger.info("Executing give list...");
        logger.info(giveListPath);
        callback.update(-1, "Starting...");
        Path path = Paths.get(giveListPath);
        List<String> lines = Files.readAllLines(path);



        int index = -1;


        logger.info("Typing");

        while (index < lines.size()) {
            logger.info("Waiting for keys");
            callback.update((float) (index + 1) / lines.size(), "Giving...");
            executorCallbacks.setProgress(Math.round(((float)(index + 1) / lines.size()) * 100));
            executorCallbacks.setTextArea(makeList(lines, index));
            executorCallbacks.scrollToLine(index);
            int key = RobotTyper.waitForKeys(NativeKeyEvent.VC_PAGE_UP, NativeKeyEvent.VC_PAGE_DOWN, NativeKeyEvent.VC_INSERT); // Numpad 3 and Numpad 1 Numpad 2
            if (key == NativeKeyEvent.VC_PAGE_UP) index++;
            if (key == NativeKeyEvent.VC_PAGE_DOWN && index > 0) index--;
            logger.info("Typing string!");
            if (index > lines.size() - 1) break;
            robotTyper.typeString("/");
            robotTyper.pasteStringViaClipboard(lines.get(index).replace("\n", ""));

            // Previous text
            String previousText = "[none]";
            if (index != 0) {
                previousText = getBlockNameFromGiveCommand(lines.get(index - 1));
            }

            String currentText = getBlockNameFromGiveCommand(lines.get(index));

            String nextText = "[end]";
            if (index != lines.size() - 1) {
                nextText = getBlockNameFromGiveCommand(lines.get(index + 1));
            }

            String finalText = String.format("<html><span style='white-space: nowrap;'>%s<b> • %s • </b>%s</span></html>", previousText, currentText, nextText);

            executorCallbacks.setStatus(finalText);





        }
        callback.update(1, "Complete!");
        executorCallbacks.finished();
        logger.info("Give list is complete");



    }

}
