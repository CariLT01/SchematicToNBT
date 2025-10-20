package com.carilt01.schematictonbt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.nbt.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

class BlockEntry {
    int type;
    int meta;
    String name;
    String text_type; // optional if you need it
}


public class SchematicToSchem {

    private List<BlockEntry> blockTypes;
    private Map<Long, BlockEntry> mappedBlockTypes = new HashMap<>();

    public SchematicToSchem() {

        this.loadBlockTypes();

    }

    private void loadBlockTypes() {
        System.out.println("Loading block types...");
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader("blocks.json")) {
            Type listType = new TypeToken<List<BlockEntry>>(){}.getType();
            List<BlockEntry> blocks = gson.fromJson(fileReader, listType);

            for (BlockEntry b : blocks) {
                long uniqueId = ((long) b.type << 32) | (b.meta & 0xFFFFFFFFL);

                mappedBlockTypes.put(uniqueId, b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void convertFile(String fileName) {

        File file = new File(fileName);


        try {


            CompoundBinaryTag tag = BinaryTagIO.reader().read(file.toPath(), BinaryTagIO.Compression.GZIP);

            short width = tag.getShort("Width");
            short height = tag.getShort("Height");
            short length = tag.getShort("Length");

            byte[] blocksOriginal = tag.getByteArray("Blocks");
            byte[] metadataOriginal = tag.getByteArray("Data");

            CompoundBinaryTag.Builder paletteBuilder = CompoundBinaryTag.builder();



            // Palette pass, scan for all unique blocks

            Set<String> uniquePalettes = new HashSet<>();
            List<String> paletteIndices = new ArrayList<>();
            List<Integer> blockTypesList = new ArrayList<>();

            int paletteIndex = 0;
            int blocksFailed = 0;

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = (y * width * length + z * width + x);

                        byte blockData = blocksOriginal[index];
                        byte blockMetadata = metadataOriginal[index];

                        long hash = ((long) (int) blockData << 32) | ((int) blockMetadata & 0xFFFFFFFFL);

                        BlockEntry entry = this.mappedBlockTypes.get(hash);

                        String blockName = "";
                        if (entry != null) {
                            blockName = "minecraft:" + entry.name.toLowerCase().replace(" ", "_");
                        } else {
                            System.out.println("Warn: failed to get block hash: " + hash);
                            blockName = "minecraft:stone";
                            blocksFailed++;
                        }



                        if (!uniquePalettes.contains(blockName)) {
                            uniquePalettes.add(blockName);

                            paletteIndices.add(blockName);

                            paletteIndex++;
                        }

                        int blockType = paletteIndices.indexOf(blockName);
                        if (blockType == -1) {
                            throw new RuntimeException("Index of block name " + blockName + " could not be found!");
                        }
                        blockTypesList.add(blockType);







                    }
                }
            }

            int total = width * height * length;
            float successRate = 1 - ((float) blocksFailed / total);
            float perc = Math.round(successRate * 100);

            System.out.println("Success rate: " + perc + "%");

            // Build the palette compound tag

            for (String item : uniquePalettes) {
                int indexInPalette = paletteIndices.indexOf(item);

                paletteBuilder.putInt(item, indexInPalette);

            }

            CompoundBinaryTag palette = paletteBuilder.build();

            byte[] blocksBytes = new byte[width * height * length];

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = (y * width * length + z * width + x);

                        int blockTypePaletteIndex = blockTypesList.get(index);

                        blocksBytes[index] = (byte)blockTypePaletteIndex;


                    }
                }
            }




            ByteArrayBinaryTag blocksData = ByteArrayBinaryTag.byteArrayBinaryTag(blocksBytes);

            CompoundBinaryTag metadata = CompoundBinaryTag.builder()
                    .putInt("WEOffsetX", 0)
                    .putInt("WEOffsetY", 0)
                    .putInt("WEOffsetZ", 0)
                    .build();

            ListBinaryTag blockEntities = ListBinaryTag.empty();

            CompoundBinaryTag root = CompoundBinaryTag.builder()
                    .put("Metadata", metadata)
                    .put("Palette", palette)
                    .putInt("DataVersion", 3465)
                    .put("Height", ShortBinaryTag.of(height))
                    .put("Length", ShortBinaryTag.of(length))
                    .put("Width", ShortBinaryTag.of(width))
                    .putInt("Version", 2)
                    .putInt("PaletteMax", paletteIndices.toArray().length)
                    .put("BlockData", blocksData)
                    .put("BlockEntities", blockEntities)
                    .build();

            File outputFile = new File("output.schem");
            BinaryTagIO.writer().write(root, outputFile.toPath(), BinaryTagIO.Compression.GZIP);

            System.out.println("Saved structure NBT: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
