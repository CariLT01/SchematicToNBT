package com.carilt01.schematictonbt;


import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class NBTExporter {

    private static final Logger logger = LoggerFactory.getLogger(NBTExporter.class);

    public NBTExporter() {

    }

    public static Set<Block> volumeGetUniqueBlocks(Volume structureVolume) {
        return structureVolume.getUniqueBlocks();
    }



    public void exportNbt(Volume structureVolume, String fileName) throws IOException {


        ListTag<CompoundTag> paletteListBinaryTag = new ListTag<>(CompoundTag.class);
        Set<Block> uniqueBlocks = volumeGetUniqueBlocks(structureVolume);
        Map<Block, Integer> blockPaletteIndex = new HashMap<>();

        int paletteIndexCounter = 0;
        int airPaletteIndex = 0;
        int numberOfBlocksSerialized = 0;

        for (Block uniqueBlock : uniqueBlocks) {
            CompoundTag blockTag = getEntries(uniqueBlock);


            paletteListBinaryTag.add(blockTag);

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
                    //int blockHash = blockAtPos.hashCode();
                    if (!blockPaletteIndex.containsKey(blockAtPos)) {
                        logger.warn("Warn: Block not found during serialization");
                        continue;
                    }
                    int paletteIndex = blockPaletteIndex.get(blockAtPos);
                    if (paletteIndex == airPaletteIndex) continue;
                    ListTag<IntTag> positionTag = new ListTag<>(IntTag.class);
                    positionTag.add(new IntTag(x));
                    positionTag.add(new IntTag(y));
                    positionTag.add(new IntTag(z));


                    CompoundTag blockTag = new CompoundTag();
                    blockTag.put("pos", positionTag);
                    blockTag.putInt("state", paletteIndex);

                    blocksListBinaryTag.add(blockTag);

                    numberOfBlocksSerialized++;

                }
            }
        }

        logger.info("Number of blocks serialized: {}", numberOfBlocksSerialized);

        ListTag<IntTag> sizeBinaryTag = new ListTag<>(IntTag.class);

        Vector3 boundingBox = structureVolume.getBoundingBox();

        sizeBinaryTag.add(new IntTag((int)boundingBox.x));
        sizeBinaryTag.add(new IntTag((int)boundingBox.y));
        sizeBinaryTag.add(new IntTag((int)boundingBox.z));

        CompoundTag root = new CompoundTag();
        root.put("blocks", blocksListBinaryTag);
        root.put("entities", new ListTag<>(CompoundTag.class));
        root.put("palette", paletteListBinaryTag);
        root.put("size", sizeBinaryTag);
        root.putInt("DataVersion", 3465);


        File outputFile = new File(fileName);

        NBTUtil.write(root, outputFile);
        //BinaryTagIO.writer().write(root, outputFile.toPath(), BinaryTagIO.Compression.GZIP);

        logger.info("Saved structure NBT: {}", outputFile.getAbsolutePath());

        VolumeGenerateGiveList giveListGenerator = new VolumeGenerateGiveList();

        List<String> giveList = giveListGenerator.getGiveListFromVolume(structureVolume);

        Path filePath = Path.of(outputFile.getAbsolutePath() + "-giveList.txt");
        Files.write(filePath, giveList);

        logger.info("Wrote give list to: {}", filePath.toAbsolutePath());
    }

    private static CompoundTag getEntries(Block uniqueBlock) {
        CompoundTag propertiesTag = new CompoundTag();

        for (Map.Entry<String, String> property : uniqueBlock.getProperties().entrySet()) {
            propertiesTag.putString(property.getKey(), property.getValue());
        }

        CompoundTag blockTag = new CompoundTag();
        if (uniqueBlock.getProperties().isEmpty()) {
            blockTag.putString("Name", uniqueBlock.getBlockName());
        } else {
            blockTag.put("Properties", propertiesTag);
            blockTag.putString("Name", uniqueBlock.getBlockName());

        }
        return blockTag;
    }
}
