package com.carilt01.schematictonbt.loaders;

import com.carilt01.schematictonbt.Block;
import com.carilt01.schematictonbt.Volume;
import net.kyori.adventure.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SchemFileLoader {

    public SchemFileLoader() {

    }

    public Volume loadSchemToVolume(File file) throws IOException {
        CompoundBinaryTag tag = BinaryTagIO.reader().read(file.toPath(), BinaryTagIO.Compression.GZIP);

        short height = tag.getShort("Height");
        short length = tag.getShort("Length");
        short width = tag.getShort("Width");

        Volume schemVolume = new Volume(width, height, length);

        // Build the palette

        CompoundBinaryTag paletteOriginal = tag.getCompound("Palette");

        Map<Integer, String> paletteMap = new HashMap<>();


        paletteOriginal.forEach(children -> {
            String blockName = children.getKey();
            BinaryTag blockTag = children.getValue();

            if (blockTag instanceof IntBinaryTag blockIndex) {

                paletteMap.put(blockIndex.value(), blockName);



            }
        });

        // Loop through blocks and add to list

        byte[] blocksDataOriginal = tag.getByteArray("BlockData");

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {

                    int index = (y * width * length + z * width + x);
                    byte type = blocksDataOriginal[index];
                    int typeInt = type & 0xFF;

                    String blockName = paletteMap.get(typeInt);
                    if (blockName == null) {
                        blockName = "minecraft:barrier";
                    }
                    Block newBlock = Block.fromBlockName(blockName);

                    schemVolume.setBlock(x, y, z, newBlock);


                }
            }
        }

        return schemVolume;
    }

}
