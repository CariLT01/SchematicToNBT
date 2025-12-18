package com.carilt01.schematictonbt.app.converter.volume.splitter;

import com.carilt01.schematictonbt.app.converter.callbacks.ProgressCallback;
import com.carilt01.schematictonbt.app.converter.volume.Volume;
import com.carilt01.schematictonbt.app.converter.volume.VolumeCoords;
import com.carilt01.schematictonbt.app.converter.volume.VolumeMeasurementSerializer;

import java.io.IOException;
import java.util.List;

public interface VolumeSplittingMethod {

    List<VolumeCoords> split(Volume volume, VolumeMeasurementSerializer vms, ProgressCallback progressCallback, int maxVolumeSize, float quality) throws IOException;

    public default void updateProgress(ProgressCallback progressCallback, float blocksProcessed, int totalBlocks) {
        float t = blocksProcessed / totalBlocks;
        if (t <= 1) {
            progressCallback.update(t, "Splitting volume...");
        } else {
            progressCallback.update(-1, "Splitting volume...");
        }
    }
}
