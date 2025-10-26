package com.carilt01.schematictonbt;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class RobotTyper {
    private final Map<Character, Integer> keyMap = new HashMap<>();
    private final Map<Character, Boolean> shiftMap = new HashMap<>();
    private Robot robot;
    private static final Logger logger = LoggerFactory.getLogger(RobotTyper.class);

    public RobotTyper() {
        initializeKeyMap();
    }

    private void initializeKeyMap() {
        // Lowercase letters
        for (char c = 'a'; c <= 'z'; c++) {
            keyMap.put(c, KeyEvent.getExtendedKeyCodeForChar(c));
            shiftMap.put(c, false);
        }
        // Digits
        for (char c = '0'; c <= '9'; c++) {
            keyMap.put(c, KeyEvent.getExtendedKeyCodeForChar(c));
            shiftMap.put(c, false);
        }
        // Space and some symbols
        keyMap.put(' ', KeyEvent.VK_SPACE); shiftMap.put(' ', false);
        keyMap.put('.', KeyEvent.VK_PERIOD); shiftMap.put('.', false);
        keyMap.put(',', KeyEvent.VK_COMMA);  shiftMap.put(',', false);
        keyMap.put('/', KeyEvent.VK_SLASH);  shiftMap.put('/', false);
        keyMap.put('_', KeyEvent.VK_MINUS);  shiftMap.put('_', true);  // Shift + '-'
        keyMap.put(':', KeyEvent.VK_SEMICOLON); shiftMap.put(':', true); // Shift + ';'
        keyMap.put('!', KeyEvent.VK_1); shiftMap.put('!', true);
        keyMap.put('@', KeyEvent.VK_2); shiftMap.put('@', true);
        keyMap.put('?', KeyEvent.VK_SLASH); shiftMap.put('?', true);

        try {
            robot = new Robot();
        } catch (AWTException e) {
            logger.error("Failed to initialize Robot", e);
        }
    }

    public void typeString(String text) {
        if (robot == null) return;
        for (char c : text.toCharArray()) {
            boolean shift = shiftMap.getOrDefault(c, Character.isUpperCase(c));
            int keyCode = keyMap.getOrDefault(Character.toLowerCase(c), -1);
            if (keyCode == -1) continue; // unsupported char

            if (shift) robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
            if (shift) robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.delay(50);
        }
    }

    public static void initializeHook() {
        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            // Turn off JNativeHook logging
            java.util.logging.Logger jnLogger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
            jnLogger.setLevel(Level.OFF);
        } catch (NativeHookException ex) {
            throw new RuntimeException("Failed to register native hook", ex);
        }
    }

    public void pasteStringViaClipboard(String s) {
        try {
            StringSelection ss = new StringSelection(s);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

            // Ctrl+V (Windows/Linux) or Meta+V (macOS)
            boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
            if (isMac) {
                robot.keyPress(KeyEvent.VK_META);
            } else {
                robot.keyPress(KeyEvent.VK_CONTROL);
            }

            robot.keyPress(KeyEvent.VK_V);
            robot.delay(20);
            robot.keyRelease(KeyEvent.VK_V);

            if (isMac) {
                robot.keyRelease(KeyEvent.VK_META);
            } else {
                robot.keyRelease(KeyEvent.VK_CONTROL);
            }

            // small pause so paste completes
            robot.delay(40);
        } catch (Exception ex) {
            logger.warn("Clipboard paste fallback failed for: " + s, ex);
        }
    }


    // Waits for a single key code; returns when that key is pressed
    public static void waitForKey(int keyCode) throws InterruptedException {
        waitForKeys(keyCode); // delegate to multi-key version
    }

    public static int waitForKeys(int... keyCodes) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger pressedKey = new AtomicInteger(-1);

        NativeKeyListener listener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                int code = e.getKeyCode();
                for (int k : keyCodes) {
                    if (code == k) {
                        pressedKey.compareAndSet(-1, code);
                        latch.countDown();
                        break;
                    }
                }
            }
            @Override public void nativeKeyReleased(NativeKeyEvent e) {}
            @Override public void nativeKeyTyped(NativeKeyEvent e) {}
        };

        GlobalScreen.addNativeKeyListener(listener);
        latch.await();
        GlobalScreen.removeNativeKeyListener(listener);

        return pressedKey.get();
    }

    // quick example constants for numpad keys (platform/driver dependent; test them)
    public static final int NUMPAD_1 = 0x61; // 97
    public static final int NUMPAD_2 = 0x62; // 98
    public static final int NUMPAD_3 = 0x63; // 99
    public static final int NUMPAD_6 = 0x66; // 102
}
