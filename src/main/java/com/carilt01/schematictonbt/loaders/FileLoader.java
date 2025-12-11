package com.carilt01.schematictonbt.loaders;

import com.carilt01.schematictonbt.ProgressCallback;
import com.carilt01.schematictonbt.Volume;

import java.io.File;
import java.io.IOException;

public interface FileLoader {

    Volume loadFileToVolume(File file, ProgressCallback callback) throws IOException;

}
