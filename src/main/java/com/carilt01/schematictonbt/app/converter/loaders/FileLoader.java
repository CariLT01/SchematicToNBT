package com.carilt01.schematictonbt.app.converter.loaders;

import com.carilt01.schematictonbt.app.converter.callbacks.ProgressCallback;
import com.carilt01.schematictonbt.app.converter.volume.Volume;

import java.io.File;
import java.io.IOException;

public interface FileLoader {

    Volume loadFileToVolume(File file, ProgressCallback callback) throws IOException;

}
