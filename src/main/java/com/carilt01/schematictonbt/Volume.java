package com.carilt01.schematictonbt;

public class Volume {

    private final Block[] blocks;
    private final int width;
    private final int height;
    private final int length;

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
    }

    public Block[] getBlocks() {
        return this.blocks;
    }

    public Block getBlockAt(int x, int y, int z) throws IndexOutOfBoundsException {
        if (x > width || y > height || z > length) {
            throw new IndexOutOfBoundsException("Index out of bounds, X, Y or Z is more ");
        }

        if (x < 0 || y < 0 || z < 0) {
            throw new IndexOutOfBoundsException("X Y Z out of bounds");
        }

        int index = (y * width * length) + (z * width) + x;

        Block theBlock = blocks[index];

        if (theBlock == null) {
            throw new IndexOutOfBoundsException("Can't find block at index");
        }

        return theBlock;



    }


}
