package com.carilt01.schematictonbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VolumeSplitter {
    private final VolumeMeasurementSerializer vms = new VolumeMeasurementSerializer();

    public VolumeSplitter() {

    }

    public List<Volume> splitVolume(Volume volume) throws IOException {

        int initialXGuess = 2048;
        int initialZGuess = 2048;

        int xStart = 0;
        int xChunk = 0;
        int zChunk = 0;

        List<Volume> completedVolumes = new ArrayList<>();

        int xSlices = (int) Math.ceil(volume.getHeight() / (double) initialXGuess);
        int zSlices = (int) Math.ceil(volume.getLength() / (double) initialZGuess);

        int xTotal = volume.getWidth();
        int zTotal = volume.getLength();

        int totalSlices = xSlices * zSlices;
        int slicesDone = 0;
        int totalNumBlocks = xTotal * zTotal * volume.getHeight();
        int numBlocksRemaining = totalNumBlocks;

        while (xStart < xTotal) {
            int zStart = 0;
            while (zStart < volume.getLength()) {

                xChunk = Math.min(volume.getWidth() - xStart, initialXGuess);
                zChunk = Math.min(volume.getLength() - zStart, initialZGuess);

                int xEnd;
                int yEnd = volume.getHeight();
                int zEnd;

                while (true) {

                    xEnd = Math.min(xStart + xChunk, volume.getWidth());
                    zEnd = Math.min(zStart + zChunk, volume.getLength());

                    //System.out.println("Attempt to split at " + volume.getWidth() + ", " + yEnd + ", " + zEnd);
                    //System.out.println("Start: 0 " + yStart + " " + zStart);
                    System.out.print("Getting size...");
                    Volume areaVolume = volume.collectBlocksInArea(
                            xStart, 0, zStart,
                            xEnd, yEnd, zEnd

                    );
                    System.out.print("\rSerializing and estimating size...");
                    byte[] nbtData = vms.serializeVolume(areaVolume);

                    int compressedSize = nbtData.length;
                    int MAX_VOLUME_SIZE = 246 * 1024; // 10 kb for safety
                    if (compressedSize < MAX_VOLUME_SIZE) {
                        //System.out.print("\rReached a size of: " + compressedSize);
                        break;
                    }

                    xChunk = Math.max(1, xChunk / 2);
                    zChunk = Math.max(1, zChunk / 2);
                }

                // Export
                Volume areaVolume = volume.collectBlocksInArea(
                        xStart, 0, zStart,
                        Math.min(xStart + xChunk, volume.getWidth()), volume.getHeight(),
                        Math.min(zStart + zChunk, volume.getLength())
                );

                int numBlocksFilled = areaVolume.getWidth() * areaVolume.getHeight() * areaVolume.getLength();
                numBlocksRemaining -= numBlocksFilled;

                completedVolumes.add(areaVolume);

                zStart += zChunk;

                xEnd = Math.min(xStart + xChunk, volume.getWidth());
                zEnd = Math.min(zStart + zChunk, volume.getLength());


                slicesDone += 1;

                // Compute percentage
               // int percent = (int) ((slicesDone / (double) totalSlices) * 100);
                System.out.println("Splitting volume: " + Math.round(((float) (totalNumBlocks - numBlocksRemaining) / totalNumBlocks) * 100) +  "% Steps done: " + (totalNumBlocks - numBlocksRemaining) + "/" + totalNumBlocks);
            }

            xStart += xChunk;
        }

        return completedVolumes;

    }

}
