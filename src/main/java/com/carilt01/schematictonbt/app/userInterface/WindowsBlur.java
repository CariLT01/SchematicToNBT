package com.carilt01.schematictonbt.app.userInterface;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class WindowsBlur {

    // Accent states for SetWindowCompositionAttribute
    public static final int ACCENT_DISABLED = 0;
    public static final int ACCENT_ENABLE_GRADIENT = 1;
    public static final int ACCENT_ENABLE_TRANSPARENTGRADIENT = 2;
    public static final int ACCENT_ENABLE_BLURBEHIND = 3;
    public static final int ACCENT_ENABLE_ACRYLICBLURBEHIND = 4;

    // Structures
    public static class ACCENT_POLICY extends Structure {
        public int AccentState;
        public int AccentFlags;
        public int GradientColor;
        public int AnimationId;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("AccentState", "AccentFlags", "GradientColor", "AnimationId");
        }
    }

    public static class WINDOWCOMPOSITIONATTRIBDATA extends Structure {
        public int Attribute;
        public Pointer Data;
        public int SizeOfData;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("Attribute", "Data", "SizeOfData");
        }
    }

    // User32 interface
    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
        void SetWindowCompositionAttribute(HWND hwnd, WINDOWCOMPOSITIONATTRIBDATA data);
    }

    // Attribute enum
    public static final int WCA_ACCENT_POLICY = 19;

    public static void enableBlur(HWND hwnd) {
        ACCENT_POLICY accent = new ACCENT_POLICY();
        accent.AccentState = ACCENT_ENABLE_ACRYLICBLURBEHIND;
        accent.GradientColor = 0x99FFFFFF; // ARGB: 0xAARRGGBB, here 60% opacity white
        accent.AccentFlags = 0;
        accent.write();

        WINDOWCOMPOSITIONATTRIBDATA data = new WINDOWCOMPOSITIONATTRIBDATA();
        data.Attribute = WCA_ACCENT_POLICY;
        data.Data = accent.getPointer();
        data.SizeOfData = accent.size();
        data.write();

        User32.INSTANCE.SetWindowCompositionAttribute(hwnd, data);
    }
}