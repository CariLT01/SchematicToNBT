package com.carilt01.schematictonbt.app.converter.callbacks;

public interface ProgressCallback {
    void update(float progress, String text);
    void showWarning(String message);
}
