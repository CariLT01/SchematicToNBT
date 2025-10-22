package com.carilt01.schematictonbt;


import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NBTExporter {
    public NBTExporter() {

    }

    public static Set<Block> volumeGetUniqueBlocks(Volume structureVolume) {
        return new HashSet<>(Arrays.asList(structureVolume.getBlocks()));
    }



    public void exportNbt(Volume structureVolume, String fileName) throws IOException {


        ListTag<CompoundTag> paletteListBinaryTag = new ListTag<>(CompoundTag.class);
        Set<Block> uniqueBlocks = volumeGetUniqueBlocks(structureVolume);
        Map<Block, Integer> blockPaletteIndex = new HashMap<>();

        int paletteIndexCounter = 0;
        int airPaletteIndex = 0;

        for (Block uniqueBlock : uniqueBlocks) {
            CompoundTag propertiesTag = new CompoundTag();

            for (Map.Entry<String, String> property : uniqueBlock.getProperties().entrySet()) {
                propertiesTag.putString(property.getKey(), property.getValue());
            }

            CompoundTag blockTag = new CompoundTag();
            if (uniqueBlock.getProperties().size() <= 0) {
                blockTag.putString("Name", uniqueBlock.getBlockName());
            } else {
                blockTag.put("Properties", propertiesTag);
                blockTag.putString("Name", uniqueBlock.getBlockName());

            }




            paletteListBinaryTag.add(blockTag);

            int hash = uniqueBlock.hashCode();
            if (!blockPaletteIndex.containsKey(uniqueBlock)) {
                if (Objects.equals(uniqueBlock.getBlockName(), "minecraft:air")) {
                    airPaletteIndex = paletteIndexCounter;
                }
                blockPaletteIndex.put(uniqueBlock, paletteIndexCounter);
                paletteIndexCounter++;
            }

        }


        ListTag<CompoundTag> blocksListBinaryTag = new ListTag<>(CompoundTag.class);

        for (int y = 0; y < structureVolume.getHeight(); y++) {
            for (int z = 0; z < structureVolume.getLength(); z++) {
                for (int x = 0; x < structureVolume.getWidth(); x++) {

                    Block blockAtPos = structureVolume.getBlockAt(x, y, z);
                    int blockHash = blockAtPos.hashCode();
                    int paletteIndex = blockPaletteIndex.getOrDefault(blockAtPos, 0);
                    if (paletteIndex == airPaletteIndex) continue;
                    ListTag<IntTag> positionTag = new ListTag<>(IntTag.class);
                    positionTag.add(new IntTag(x));
                    positionTag.add(new IntTag(y));
                    positionTag.add(new IntTag(z));


                    CompoundTag blockTag = new CompoundTag();
                    blockTag.put("pos", positionTag);
                    blockTag.putInt("state", paletteIndex);

                    blocksListBinaryTag.add(blockTag);

                }
            }
        }

        ListTag<IntTag> sizeBinaryTag = new ListTag<>(IntTag.class);

        sizeBinaryTag.add(new IntTag(structureVolume.getWidth()));
        sizeBinaryTag.add(new IntTag(structureVolume.getHeight()));
        sizeBinaryTag.add(new IntTag(structureVolume.getLength()));

        CompoundTag root = new CompoundTag();
        root.put("blocks", blocksListBinaryTag);
        root.put("entities", new ListTag<>(CompoundTag.class));
        root.put("palette", paletteListBinaryTag);
        root.put("size", sizeBinaryTag);
        root.putInt("DataVersion", 3465);


        File outputFile = new File(fileName);

        NBTUtil.write(root, outputFile);
        //BinaryTagIO.writer().write(root, outputFile.toPath(), BinaryTagIO.Compression.GZIP);

        System.out.println("Saved structure NBT: " + outputFile.getAbsolutePath());
    }
}
