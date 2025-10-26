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

    public void executeGiveList(String giveListPath, ProgressCallback callback) throws Exception {
        logger.info("Executing give list...");
        logger.info(giveListPath);
        Path path = Paths.get(giveListPath);
        List<String> lines = Files.readAllLines(path);

        callback.update(-1, "Starting...");
        Thread.sleep(5000);

        int index = -1;


        logger.info("Typing");

        while (index < lines.size()) {
            logger.info("Waiting for keys");
            callback.update((float) (index + 1) / lines.size(), "Giving...");
            int key = RobotTyper.waitForKeys(NativeKeyEvent.VC_RIGHT, NativeKeyEvent.VC_LEFT, NativeKeyEvent.VC_DOWN); // Numpad 3 and Numpad 1 Numpad 2
            if (key == NativeKeyEvent.VC_RIGHT) index++;
            if (key == NativeKeyEvent.VC_LEFT && index > 0) index--;
            logger.info("Typing string!");
            if (index > lines.size() - 1) break;
            robotTyper.typeString("/");
            robotTyper.pasteStringViaClipboard(lines.get(index).replace("\n", ""));

        }
        callback.update(1, "Complete!");
        logger.info("Give list is complete");



    }

}
