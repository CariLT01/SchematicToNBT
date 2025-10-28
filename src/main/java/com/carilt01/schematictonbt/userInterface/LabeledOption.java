package com.carilt01.schematictonbt.userInterface;

public class LabeledOption {
    private final String title;
    private final String description;

    public LabeledOption(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return title; // for debugging
    }
}