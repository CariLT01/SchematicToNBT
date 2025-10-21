package com.carilt01.schematictonbt;

import net.kyori.adventure.nbt.*;

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


        ListBinaryTag.Builder<BinaryTag> paletteListBinaryTagBuilder = ListBinaryTag.builder();
        Set<Block> uniqueBlocks = volumeGetUniqueBlocks(structureVolume);
        Map<Block, Integer> blockPaletteIndex = new HashMap<>();

        int paletteIndexCounter = 0;
        int airPaletteIndex = 0;

        for (Block uniqueBlock : uniqueBlocks) {
            CompoundBinaryTag.Builder propertiesTagBuilder = CompoundBinaryTag.builder();

            for (Map.Entry<String, String> property : uniqueBlock.getProperties().entrySet()) {
                propertiesTagBuilder.putString(property.getKey(), property.getValue());
            }

            CompoundBinaryTag propertiesTag = propertiesTagBuilder.build();
            CompoundBinaryTag.Builder blockTagBuilder = CompoundBinaryTag.builder();
            if (uniqueBlock.getProperties().size() <= 0) {
                blockTagBuilder.putString("Name", uniqueBlock.getBlockName());
            } else {
                blockTagBuilder.put("Properties", propertiesTag);
                blockTagBuilder.putString("Name", uniqueBlock.getBlockName());

            }
            CompoundBinaryTag blockTag = blockTagBuilder.build();



            paletteListBinaryTagBuilder.add(blockTag);

            int hash = uniqueBlock.hashCode();
            if (!blockPaletteIndex.containsKey(uniqueBlock)) {
                if (Objects.equals(uniqueBlock.getBlockName(), "minecraft:air")) {
                    airPaletteIndex = paletteIndexCounter;
                }
                blockPaletteIndex.put(uniqueBlock, paletteIndexCounter);
                paletteIndexCounter++;
            }

        }

        ListBinaryTag paletteListBinaryTag = paletteListBinaryTagBuilder.build();

        ListBinaryTag.Builder<BinaryTag> blocksListBinaryTagBuilder = ListBinaryTag.builder();

        for (int y = 0; y < structureVolume.getHeight(); y++) {
            for (int z = 0; z < structureVolume.getLength(); z++) {
                for (int x = 0; x < structureVolume.getWidth(); x++) {

                    Block blockAtPos = structureVolume.getBlockAt(x, y, z);
                    int blockHash = blockAtPos.hashCode();
                    int paletteIndex = blockPaletteIndex.getOrDefault(blockAtPos, 0);
                    if (paletteIndex == airPaletteIndex) continue;
                    ListBinaryTag positionTag = ListBinaryTag.builder()
                            .add(IntBinaryTag.intBinaryTag(x))
                            .add(IntBinaryTag.intBinaryTag(y))
                            .add(IntBinaryTag.intBinaryTag(z))
                            .build();

                    CompoundBinaryTag blockTag = CompoundBinaryTag.builder()
                            .put("pos", positionTag)
                            .putInt("state", paletteIndex)
                            .build();

                    blocksListBinaryTagBuilder.add(blockTag);

                }
            }
        }

        ListBinaryTag blocksListBinaryTag = blocksListBinaryTagBuilder.build();
        ListBinaryTag sizeBinaryTag = ListBinaryTag.builder()
                .add(IntBinaryTag.intBinaryTag(structureVolume.getWidth()))
                .add(IntBinaryTag.intBinaryTag(structureVolume.getHeight()))
                .add(IntBinaryTag.intBinaryTag(structureVolume.getLength()))
                .build();

        CompoundBinaryTag root = CompoundBinaryTag.builder()
                .put("blocks", blocksListBinaryTag)
                .put("entities", ListBinaryTag.builder().build())
                .put("palette", paletteListBinaryTag)
                .put("size", sizeBinaryTag)
                .putInt("DataVersion", 3465)
                .build();

        File outputFile = new File(fileName);
        BinaryTagIO.writer().write(root, outputFile.toPath(), BinaryTagIO.Compression.GZIP);

        System.out.println("Saved structure NBT: " + outputFile.getAbsolutePath());
    }
}
