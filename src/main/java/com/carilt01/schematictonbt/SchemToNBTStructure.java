package com.carilt01.schematictonbt;

import net.kyori.adventure.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SchemToNBTStructure {


    public SchemToNBTStructure() {

    }

    public static Map<String, String> parseBlockProperties(String blockState) {
        Map<String, String> properties = new HashMap<>();

        int bracketStart = blockState.indexOf('[');
        int bracketEnd = blockState.indexOf(']');

        if (bracketStart != -1 && bracketEnd != -1 && bracketEnd > bracketStart) {
            String props = blockState.substring(bracketStart + 1, bracketEnd);
            String[] pairs = props.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    properties.put(kv[0].trim(), kv[1].trim());
                }
            }
        }

        return properties;
    }

    public void convert(String filename) {
        File file = new File(filename);

        try {
            CompoundBinaryTag tag = BinaryTagIO.reader().read(file.toPath(), BinaryTagIO.Compression.GZIP);

            short height = tag.getShort("Height");
            short length = tag.getShort("Length");
            short width = tag.getShort("Width");

            List<IntBinaryTag> sizeTags = new ArrayList<>();
            sizeTags.add(
                    IntBinaryTag.intBinaryTag(width)
            );
            sizeTags.add(
                    IntBinaryTag.intBinaryTag(height)
            );
            sizeTags.add(
                    IntBinaryTag.intBinaryTag(length)
            );



            ListBinaryTag sizeTag = ListBinaryTag.from(sizeTags);

            // Build the palette

            ListBinaryTag.Builder<BinaryTag> paletteListBuilder = ListBinaryTag.builder();

            CompoundBinaryTag paletteOriginal = tag.getCompound("Palette");

            Map<Integer, CompoundBinaryTag> mappedPalette = new HashMap<>();

            AtomicInteger airPaletteIndex = new AtomicInteger();

            paletteOriginal.forEach(children -> {
                String blockName = children.getKey();
                BinaryTag blockTag = children.getValue();

                if (blockTag instanceof IntBinaryTag blockIndex) {
                    if (blockName.equals("minecraft:air")) {
                        airPaletteIndex.set(blockIndex.value());
                    }
                    Map<String, String> blockProperties = parseBlockProperties(blockName);

                    CompoundBinaryTag.Builder propertiesTagBuilder = CompoundBinaryTag.builder();

                    int numberOfProperties = 0;
                    for (Map.Entry<String, String> entry : blockProperties.entrySet()) {
                        numberOfProperties++;
                        propertiesTagBuilder.putString(entry.getKey(), entry.getValue());
                    }

                    CompoundBinaryTag propertiesTag = propertiesTagBuilder.build();

                    CompoundBinaryTag.Builder paletteEntryTagBuilder = CompoundBinaryTag.builder();
                    if (numberOfProperties > 0) {
                        paletteEntryTagBuilder.put("Properties", propertiesTag);
                    }

                    paletteEntryTagBuilder.putString("Name", blockName.split("\\[")[0]);
                    CompoundBinaryTag paletteEntryTag = paletteEntryTagBuilder.build();

                    mappedPalette.put(blockIndex.value(), paletteEntryTag);

                }
            });

            List<CompoundBinaryTag> orderedList = mappedPalette.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .toList();

            for (CompoundBinaryTag paletteOrderedTag : orderedList) {
                paletteListBuilder.add(paletteOrderedTag);
            }


            ListBinaryTag paletteListTag = paletteListBuilder.build();

            // Build the blocks position

            byte[] blocksDataOriginal = tag.getByteArray("BlockData");
            ListBinaryTag.Builder<BinaryTag> blocksBuilder = ListBinaryTag.builder();

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = (y * width * length + z * width + x);
                        byte type = blocksDataOriginal[index];
                        if (airPaletteIndex.get() == (type & 0xFF)) {
                            continue;
                        }

                        ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
                        builder.add(IntBinaryTag.intBinaryTag(x));
                        builder.add(IntBinaryTag.intBinaryTag(y));
                        builder.add(IntBinaryTag.intBinaryTag(z));
                        ListBinaryTag listTag = builder.build();




                        CompoundBinaryTag blockTag = CompoundBinaryTag.builder()
                                .put("pos", listTag)
                                .putInt("state", type & 0xFF)
                                .build();

                        blocksBuilder.add(blockTag);


                    }
                }
            }

            ListBinaryTag blocksTag = blocksBuilder.build();


            CompoundBinaryTag newRoot = CompoundBinaryTag.builder()
                    .put("palette", paletteListTag)
                    .put("entities", ListBinaryTag.builder().build())
                    .put("blocks", blocksTag)
                    .put("size", sizeTag)
                    .putInt("DataVersion", 3465)
                    .build();

            File outputFile = new File("output.nbt");
            BinaryTagIO.writer().write(newRoot, outputFile.toPath(), BinaryTagIO.Compression.GZIP);

            System.out.println("Saved structure NBT: " + outputFile.getAbsolutePath());





        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
