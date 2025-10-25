package com.carilt01.schematictonbt;

public interface ProgressCallback {
    void update(float progress, String text);
    void showWarning(String message);
}
