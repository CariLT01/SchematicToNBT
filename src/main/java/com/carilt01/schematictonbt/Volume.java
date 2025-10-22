package com.carilt01.schematictonbt;

public class Volume {

    private final Block[] blocks;
    private final int width;
    private final int height;
    private final int length;

    private boolean hasBeenManipulated = false;

    public Volume(int width, int height, int length) {

        this.width = width;
        this.height = height;
        this.length = length;

        blocks = new Block[width * height * length];

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

    public void setBlock(int x, int y, int z, Block newBlock) {
        int index = (y * width * length) + (z * width) + x;

        blocks[index] = newBlock;

        hasBeenManipulated = true;
    }

    public Block[] getBlocks() {
        return this.blocks;
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

        Block theBlock = blocks[index];

        if (theBlock == null) {
            throw new IndexOutOfBoundsException("Can't find block at index at " + x + ", " + y + ", " + z);
        }

        return theBlock;



    }

    public Volume collectBlocksInArea(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        Volume collectedArea = new Volume(endX - startX, endY - startY, endZ - startZ);

        int sizeX = endX - startX;
        int sizeY = endY - startY;
        int sizeZ = endZ - startZ;

        System.out.println("Requesting a volume of size: " + sizeX + " " + sizeY + " " + sizeZ);

        if (sizeX > width || sizeY > height || sizeZ > length) {
            throw new IndexOutOfBoundsException("Attempt to collect blocks in area where area is bigger than the volume itself");
        }

        if (endX > width || endY > height || endZ > length) {
            throw new IndexOutOfBoundsException(("EndX/Y/Z bigger than volume X/Y/Z"));
        }

        System.out.println("End of: " + endX + " " + endY + " " + endZ);

        for (int y = startY; y  < endY; y++) {
            for (int z = startZ; z < endZ; z++) {
                for (int x = startX; x < endX; x++) {
                    Block blockAtPos = this.getBlockAt(x, y, z);

                    collectedArea.setBlock(x - startX, y - startY, z - startZ, blockAtPos);
                }
            }
        }

        return collectedArea;
    }


}
