package com.carilt01.schematictonbt.loaders;

import com.carilt01.schematictonbt.Block;
import com.carilt01.schematictonbt.Volume;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SchemFileLoader {

    public SchemFileLoader() {

    }

    public Volume loadSchemToVolume(File file) throws IOException {
        //CompoundBinaryTag tag = BinaryTagIO.reader().read(file.toPath(), BinaryTagIO.Compression.GZIP);
        NamedTag tag_root = NBTUtil.read(file);
        CompoundTag tag = (CompoundTag) tag_root.getTag();

        short height = tag.getShort("Height");
        short length = tag.getShort("Length");
        short width = tag.getShort("Width");

        Volume schemVolume = new Volume(width, height, length);

        // Build the palette

        CompoundTag paletteOriginal = tag.getCompoundTag("Palette");

        Map<Integer, String> paletteMap = new HashMap<>();


        paletteOriginal.forEach(children -> {
            String blockName = children.getKey();

            int blockIndex = paletteOriginal.getInt(blockName);



            paletteMap.put(blockIndex, blockName);




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
