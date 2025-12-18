package com.carilt01.schematictonbt.app.converter.volume.splitter;

import com.carilt01.schematictonbt.app.converter.callbacks.ProgressCallback;
import com.carilt01.schematictonbt.app.converter.volume.Volume;
import com.carilt01.schematictonbt.app.converter.volume.VolumeCoords;
import com.carilt01.schematictonbt.app.converter.volume.VolumeMeasurementSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class VolumeSplitter {
    private final VolumeMeasurementSerializer vms = new VolumeMeasurementSerializer();
    private static final Logger logger = LoggerFactory.getLogger(VolumeSplitter.class);

    private final VolumeLinearSearchSplit linearSearchSplit = new VolumeLinearSearchSplit();
    private final VolumeBinarySearchSplit binarySearchSplit = new VolumeBinarySearchSplit();

    public VolumeSplitter() {

    }





    public List<VolumeCoords> splitVolume(Volume volume, ProgressCallback progressCallback, int maxVolumeSize, float quality, boolean useBinarySearch) throws IOException {
        if (useBinarySearch) {
            return linearSearchSplit.split(volume, vms, progressCallback, maxVolumeSize, quality);
        } else {
            return binarySearchSplit.split(volume, vms, progressCallback, maxVolumeSize, quality);
        }
    }



}
