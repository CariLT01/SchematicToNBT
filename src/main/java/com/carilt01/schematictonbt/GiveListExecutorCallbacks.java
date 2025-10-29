package com.carilt01.schematictonbt;

public interface GiveListExecutorCallbacks {
    void setStatus(String text);
    void setProgress(int progress);
    void setTextArea(String text);
    void finished();
    void scrollToLine(int index);
}
