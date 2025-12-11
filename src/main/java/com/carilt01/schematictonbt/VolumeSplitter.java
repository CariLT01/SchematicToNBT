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

    public List<VolumeCoords> splitVolumeLinearSearch(Volume volume, ProgressCallback progressCallback, int maxVolumeSize, float quality) throws IOException {
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

                long startTime = System.currentTimeMillis();

                while (true) {
                    xEnd = Math.min(xStart + curXChunk, volume.getWidth());
                    zEnd = Math.min(zStart + curZChunk, volume.getLength());

                    logger.info("Getting size...");
                    Volume areaVolume;
                    SlowTaskWatcher<Volume> areaCollectionWatcher = new SlowTaskWatcher<>(3000);
                    int finalXStart = xStart;
                    int finalZStart = zStart;
                    int finalXEnd = xEnd;
                    int finalZEnd = zEnd;
                    try {
                        areaVolume = areaCollectionWatcher.run(
                                () -> volume.collectBlocksInArea(finalXStart, 0, finalZStart, finalXEnd, yEnd, finalZEnd),
                                () -> progressCallback.update(-1, "Collecting blocks...")
                        );
                    } catch (Exception e) {
                        logger.error("Exception: ", e);
                        areaVolume = new Volume(0, 0, 0);
                    } finally {
                        updateProgress(progressCallback, (float) blocksProcessed, totalBlocks);
                    }


                    logger.info("Serializing and estimating size...");
                    if (areaVolume.countNonAirBlocks() < blockLimit) {

                        SlowTaskWatcher<byte[]> watcher = new SlowTaskWatcher<>(3000);

                        byte[] nbtData;
                        try {
                            Volume finalAreaVolume = areaVolume;
                            nbtData = watcher.run(
                                    () -> vms.serializeVolume(finalAreaVolume, Optional.of(progressCallback)),
                                    () -> progressCallback.update(-1, "Estimating size..."));
                        } catch (Exception e) {
                            logger.error("Error while serializing volume: ", e);
                            nbtData = new byte[maxVolumeSize];
                        }
                        updateProgress(progressCallback, (float) blocksProcessed, totalBlocks);


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
                        curXChunk = (int) Math.ceil(Math.max(1, curXChunk / quality));
                    } else if (curZChunk > 1) {
                        curZChunk = (int) Math.ceil(Math.max(1, curZChunk / quality));
                    } else {
                        // both are 1 and still too big — cannot proceed
                        throw new IOException("Single-block chunk exceeds MAX_VOLUME_SIZE");
                    }
                }

                // Export using the final xEnd/zEnd determined above
                //Volume areaVolume = volume.collectBlocksInArea(xStart, 0, zStart, xEnd, yEnd, zEnd);
                completedVolumes.add(new VolumeCoords(xStart, 0, zStart, xEnd, yEnd, zEnd));

                blocksProcessed += (xEnd - xStart) * (yEnd) * (zEnd - zStart);

                updateProgress(progressCallback, (float) blocksProcessed, totalBlocks);

                logger.info("Blocks processed progress: {}%", Math.round((float) blocksProcessed / totalBlocks * 100));

                // Advance zStart by the final chunk used for this piece
                zStart += Math.min(curZChunk, zEnd - zStart);
            }

            // Advance xStart by the column chunk size (the one chosen once per x column)
            xStart += xChunkForColumn;
        }

        return completedVolumes;
    }

    public List<VolumeCoords> splitVolumeBinarySearch(Volume volume, ProgressCallback progressCallback, int maxVolumeSize) throws IOException {
        final int initialXGuess = 2048;
        final int initialZGuess = 2048;
        final int blockLimit = 25_000_000;
        final int threshold = 10_000;

        boolean lastWasIncrease = false;
        int lastSize = 0;

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

                long startTime = System.currentTimeMillis();

                String chosenAxis = "";

                if (curZChunk > curXChunk) {
                    chosenAxis = "Z";
                } else {
                    chosenAxis = "X";
                }

                int low = 1;
                int high = chosenAxis.equals("X") ? curXChunk : curZChunk;
                int mid = Math.round((float) (low + high) / 2);
                int bestChunk = mid;

                while (true) {
                    xEnd = Math.min(xStart + curXChunk, volume.getWidth());
                    zEnd = Math.min(zStart + curZChunk, volume.getLength());

                    logger.info("Getting size...");
                    Volume areaVolume;
                    SlowTaskWatcher<Volume> areaCollectionWatcher = new SlowTaskWatcher<>(3000);
                    int finalXStart = xStart;
                    int finalZStart = zStart;
                    int finalXEnd = xEnd;
                    int finalZEnd = zEnd;
                    try {
                        areaVolume = areaCollectionWatcher.run(
                                () -> volume.collectBlocksInArea(finalXStart, 0, finalZStart, finalXEnd, yEnd, finalZEnd),
                                () -> progressCallback.update(-1, "Collecting blocks...")
                        );
                    } catch (Exception e) {
                        logger.error("Exception: ", e);
                        areaVolume = new Volume(0, 0, 0);
                    } finally {
                        updateProgress(progressCallback, (float) blocksProcessed, totalBlocks);
                    }


                    logger.info("Serializing and estimating size...");
                    if (areaVolume.countNonAirBlocks() < blockLimit) {

                        SlowTaskWatcher<byte[]> watcher = new SlowTaskWatcher<>(3000);

                        byte[] nbtData;
                        try {
                            Volume finalAreaVolume = areaVolume;
                            nbtData = watcher.run(
                                    () -> vms.serializeVolume(finalAreaVolume, Optional.of(progressCallback)),
                                    () -> progressCallback.update(-1, "Estimating size..."));
                        } catch (Exception e) {
                            logger.error("Error while serializing volume: ", e);
                            nbtData = new byte[maxVolumeSize];
                        }
                        updateProgress(progressCallback, (float) blocksProcessed, totalBlocks);


                        int compressedSize = nbtData.length;



                        if (compressedSize < maxVolumeSize) {
                            low = mid + 1;
                            bestChunk = mid;
                        } else {
                            high = mid - 1;
                        }





                        logger.info("Got a size of: {} KB", Math.round((float) compressedSize / 1024));

                        if (low >= high) {

                            if (chosenAxis.equals("X")) {
                                curXChunk = bestChunk;
                            } else {
                                curZChunk = bestChunk;
                            }

                            xChunkForColumn = curXChunk; // Save the smaller, valid width
                            xEnd = Math.min(xStart + curXChunk, volume.getWidth());
                            zEnd = Math.min(zStart + curZChunk, volume.getLength());

                            logger.info("Reached a size of: {} KB", Math.round((float) compressedSize / 1024));
                            break;
                        }
                    } else {
                        logger.info("Volume too large, skip serialize step");
                    }


                    // Binary search

                    mid = low + (high - low) / 2;

                    if (chosenAxis.equals("X")) {
                        curXChunk = mid;
                    } else {
                        curZChunk = mid;
                    }

                    curXChunk = Math.max(1, curXChunk);
                    curZChunk = Math.max(1, curZChunk);
                }

                // Export using the final xEnd/zEnd determined above
                //Volume areaVolume = volume.collectBlocksInArea(xStart, 0, zStart, xEnd, yEnd, zEnd);
                completedVolumes.add(new VolumeCoords(xStart, 0, zStart, xEnd, yEnd, zEnd));

                blocksProcessed += (xEnd - xStart) * (yEnd) * (zEnd - zStart);

                updateProgress(progressCallback, (float) blocksProcessed, totalBlocks);

                logger.info("Blocks processed progress: {}%", Math.round((float) blocksProcessed / totalBlocks * 100));

                // Advance zStart by the final chunk used for this piece
                zStart += Math.min(curZChunk, zEnd - zStart);
            }

            // Advance xStart by the column chunk size (the one chosen once per x column)
            xStart += xChunkForColumn;
        }

        return completedVolumes;
    }

    public List<VolumeCoords> splitVolume(Volume volume, ProgressCallback progressCallback, int maxVolumeSize, float quality, boolean useBinarySearch) throws IOException {
        if (useBinarySearch) {
            return splitVolumeBinarySearch(volume, progressCallback, maxVolumeSize);
        } else {
            return splitVolumeLinearSearch(volume, progressCallback, maxVolumeSize, quality);
        }
    }

    private static void updateProgress(ProgressCallback progressCallback, float blocksProcessed, int totalBlocks) {
        float t = blocksProcessed / totalBlocks;
        if (t <= 1) {
            progressCallback.update(t, "Splitting volume...");
        } else {
            progressCallback.update(-1, "Splitting volume...");
        }
    }

}
