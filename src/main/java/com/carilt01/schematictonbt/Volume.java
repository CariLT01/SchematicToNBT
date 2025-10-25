package com.carilt01.schematictonbt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Volume implements Iterable<Block> {

    public final short[] blockData;
    public final Map<Block, Integer> paletteMap;
    public final Map<Integer, Block> paletteReverseMap;
    private final int width;
    private final int height;
    private final int length;
    private int dataVersion = 3465;
    private int paletteCounter;
    private static final Logger logger = LoggerFactory.getLogger(Volume.class);

    private boolean hasBeenManipulated = false;

    public Volume(int width, int height, int length) {

        this.width = width;
        this.height = height;
        this.length = length;

        blockData = new short[width * height * length];
        paletteMap = new HashMap<>();
        paletteReverseMap = new HashMap<>();

    }

    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    public int getLength() {
        return this.length;
    }
    public void setDataVersion(int version) {
        this.dataVersion = version;
    }
    public int getDataVersion() {
        return this.dataVersion;
    }


    public void setBlock(int x, int y, int z, Block newBlock) {
        int index = (y * width * length) + (z * width) + x;

        if (!paletteMap.containsKey(newBlock)) {
            paletteMap.put(newBlock, paletteCounter);
            paletteReverseMap.put(paletteCounter, newBlock);
            paletteCounter++;
        }

        blockData[index] = paletteMap.get(newBlock).shortValue();

        hasBeenManipulated = true;
    }

    @Override
    public Iterator<Block> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < blockData.length;
            }

            @Override
            public Block next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                short i = blockData[index++];

                return paletteReverseMap.get((int) i);
            }
        };
    }

    public Block getBlockAt(int x, int y, int z) throws IndexOutOfBoundsException {
        if (x >= width || y >= height || z >= length) {
            throw new IndexOutOfBoundsException("Index out of bounds, X, Y or Z is more. " + x + ", " + y + ", " + z + ". \nMaximum structure bounds are: " + width + " " + height + " " + length);
        }

        if (x < 0 || y < 0 || z < 0) {
            throw new IndexOutOfBoundsException("X Y Z out of bounds");
        }

        if (!hasBeenManipulated) {
            throw new RuntimeException("Attempt to get block when no block has been set");
        }

        int index = (y * width * length) + (z * width) + x;

        int blockPaletteIndex = blockData[index];
        Block theBlock = paletteReverseMap.get(blockPaletteIndex);

        if (theBlock == null) {
            throw new IndexOutOfBoundsException("Can't find block at index at " + x + ", " + y + ", " + z);
        }

        return theBlock;



    }

    // Gets the bounding box of the volume BUT skips air blocks
    public Vector3 getBoundingBox() {
        int maxSizeX = 0;
        int maxSizeY = 0;
        int maxSizeZ = 0;

        for (int i = 0; i < blockData.length; i++) {
            // Convert linear index to x,y,z

            int bIndex = blockData[i];
            Block b = this.paletteReverseMap.get(bIndex);
            if (b.getBlockName().startsWith("minecraft:air")) continue;

            int y = i / (width * length);
            int z = (i / width) % length;
            int x = i % width;

            maxSizeX = Math.max(x, maxSizeX);
            maxSizeY = Math.max(y, maxSizeY);
            maxSizeZ = Math.max(z, maxSizeZ);
        }

        return new Vector3(
                maxSizeX + 1,
                maxSizeY + 1,
                maxSizeZ + 1
        );
    }

    public Set<Block> getUniqueBlocks() {
        return new HashSet<>(paletteMap.keySet());
    }

    public Volume collectBlocksInArea(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        Volume collectedArea = new Volume(endX - startX, endY - startY, endZ - startZ);

        int sizeX = endX - startX;
        int sizeY = endY - startY;
        int sizeZ = endZ - startZ;

        logger.info("Requesting a volume of size: {} {} {}", sizeX, sizeY, sizeZ);

        if (sizeX > width || sizeY > height || sizeZ > length) {
            throw new IndexOutOfBoundsException("Attempt to collect blocks in area where area is bigger than the volume itself");
        }

        if (endX > width || endY > height || endZ > length) {
            throw new IndexOutOfBoundsException(("EndX/Y/Z bigger than volume X/Y/Z"));
        }

        logger.info("End of: {} {} {}", endX, endY, endZ);

        for (int y = startY; y  < endY; y++) {
            for (int z = startZ; z < endZ; z++) {
                for (int x = startX; x < endX; x++) {
                    Block blockAtPos = this.getBlockAt(x, y, z);
                    if (blockAtPos == null) {
                        logger.warn("Warn: copying block failed at {} {} {}", x, y, z);
                    }


                    collectedArea.setBlock(x - startX, y - startY, z - startZ, blockAtPos);
                }
            }
        }

        return collectedArea;
    }

    public Vector3 getHighestPositionAt(int x, int z) {
        for (int i = getHeight() - 1; i >= 0; i--) {
            Block targetBlockAtPos = this.getBlockAt(x, i, z);
            if (!Objects.equals(targetBlockAtPos.getBlockName(), "minecraft:air")) {
                return new Vector3(x, i, z);
            }
        }

        return new Vector3(x, 0, z);
    }

    public int countNonAirBlocks() {
        Block airBlock = Block.fromBlockName("minecraft:air");
        int airIndex = paletteMap.getOrDefault(airBlock, -1);

        int counter  =0;
        for (int i = 0; i < blockData.length; i++) {
            if (airIndex != -1) {
                if (i != airIndex) counter++;
            } else {
                if (!airBlock.getBlockName().startsWith("minecraft:air")) counter++;
            }

        }
        return counter;
    }

    public int getAirIndex() {
        for (Map.Entry<Block, Integer> entry : paletteMap.entrySet()) {
            if (entry.getKey().getBlockName().startsWith("minecraft:air")) {
                return entry.getValue();
            }
        }
        return -1;
    }


}
