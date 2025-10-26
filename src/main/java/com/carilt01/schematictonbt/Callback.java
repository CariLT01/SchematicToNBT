package com.carilt01.schematictonbt;

import java.util.List;

public interface Callback {
    void startProcessFile(String filePath);
    void executeGiveList(String filePath);

}
