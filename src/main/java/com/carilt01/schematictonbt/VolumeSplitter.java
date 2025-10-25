package com.carilt01.schematictonbt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VolumeSplitter {
    private final VolumeMeasurementSerializer vms = new VolumeMeasurementSerializer();
    private static final Logger logger = LoggerFactory.getLogger(VolumeSplitter.class);

    public VolumeSplitter() {

    }

    public List<VolumeCoords> splitVolume(Volume volume, ProgressCallback progressCallback, int maxVolumeSize) throws IOException {
        final int initialXGuess = 2048;
        final int initialZGuess = 2048;
        final int blockLimit = 25_000_000;
        //final int MAX_VOLUME_SIZE = 246 * 1024;


        // Track progress
        int blocksProcessed = 0;
        int totalBlocks = volume.getWidth() * volume.getHeight() * volume.getLength();

        int xStart = 0;
        int yEnd = volume.getHeight();

        List<VolumeCoords> completedVolumes = new ArrayList<>();

        progressCallback.update(0, "Splitting volume...");

        while (xStart < volume.getWidth()) {
            int zStart = 0;

            // Decide the X chunk for this entire column (do NOT change it per z-iteration)
            int xChunkForColumn = Math.min(volume.getWidth() - xStart, initialXGuess);

            while (zStart < volume.getLength()) {
                // Decide z chunk candidate for this row
                int zChunkForRow = Math.min(volume.getLength() - zStart, initialZGuess);

                // Use local trial values so we don't overwrite the column chunk
                int curXChunk = xChunkForColumn;
                int curZChunk = zChunkForRow;

                int xEnd;
                int zEnd;

                // Reduce curXChunk/curZChunk until serialized size fits
                while (true) {
                    xEnd = Math.min(xStart + curXChunk, volume.getWidth());
                    zEnd = Math.min(zStart + curZChunk, volume.getLength());

                    logger.info("Getting size...");
                    Volume areaVolume = volume.collectBlocksInArea(xStart, 0, zStart, xEnd, yEnd, zEnd);

                    logger.info("Serializing and estimating size...");
                    if (areaVolume.countNonAirBlocks() < blockLimit) {
                        byte[] nbtData = vms.serializeVolume(areaVolume, Optional.of(progressCallback));
                        progressCallback.update((float) blocksProcessed / totalBlocks, "Splitting volume...");

                        int compressedSize = nbtData.length;
                        if (compressedSize < maxVolumeSize) {
                            xChunkForColumn = curXChunk; // Save the smaller, valid width
                            logger.info("Reached a size of: {} KB", Math.round((float) compressedSize / 1024));
                            break;
                        }
                    } else {
                        logger.info("Volume too large, skip serialize step");
                    }


                    // Shrink the axis more likely to reduce data first (heuristic)
                    if (curXChunk >= curZChunk && curXChunk > 1) {
                        curXChunk = Math.max(1, curXChunk / 2);
                    } else if (curZChunk > 1) {
                        curZChunk = Math.max(1, curZChunk / 2);
                    } else {
                        // both are 1 and still too big — cannot proceed
                        throw new IOException("Single-block chunk exceeds MAX_VOLUME_SIZE");
                    }
                }

                // Export using the final xEnd/zEnd determined above
                //Volume areaVolume = volume.collectBlocksInArea(xStart, 0, zStart, xEnd, yEnd, zEnd);
                completedVolumes.add(new VolumeCoords(xStart, 0, zStart, xEnd, yEnd, zEnd));

                blocksProcessed += (xEnd - xStart) * (yEnd) * (zEnd - zStart);

                progressCallback.update((float) blocksProcessed / totalBlocks, "Splitting volume...");

                logger.info("Blocks processed progress: {}%", Math.round((float) blocksProcessed / totalBlocks * 100));

                // Advance zStart by the final chunk used for this piece
                zStart += Math.min(curZChunk, zEnd - zStart);
            }

            // Advance xStart by the column chunk size (the one chosen once per x column)
            xStart += xChunkForColumn;
        }

        return completedVolumes;
    }

}
