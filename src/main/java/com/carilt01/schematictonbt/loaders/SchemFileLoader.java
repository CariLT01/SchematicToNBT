package com.carilt01.schematictonbt.loaders;

import com.carilt01.schematictonbt.Block;
import com.carilt01.schematictonbt.Config;
import com.carilt01.schematictonbt.ProgressCallback;
import com.carilt01.schematictonbt.Volume;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class SchemFileLoader {

    private static final Logger logger = LoggerFactory.getLogger(SchemFileLoader.class);

    public SchemFileLoader() {

    }


    public Volume loadSchemV2ToVolume(CompoundTag tag, ProgressCallback callback) {

        short height = tag.getShort("Height");
        short length = tag.getShort("Length");
        short width = tag.getShort("Width");
        int dataVersion = tag.getInt("DataVersion");

        if (Config.DATA_VERSION < dataVersion) {
            callback.showWarning(String.format("Warning: Schematic uses a newer version\nSchematic: %d\nYour version: %d\n\nExpect missing blocks.", dataVersion, Config.DATA_VERSION));
        }

        Volume schemVolume = new Volume(width, height, length);

        // Build the palette
        CompoundTag paletteOriginalRaw;
        paletteOriginalRaw = tag.getCompoundTag("Palette");

        if (paletteOriginalRaw == null) {
            throw new IllegalArgumentException("Cannot find palette");

        }
        CompoundTag paletteOriginal = paletteOriginalRaw;



        Map<Integer, String> paletteMap = new HashMap<>();


        paletteOriginalRaw.forEach(children -> {
            String blockName = children.getKey();

            int blockIndex = paletteOriginal.getInt(blockName);



            paletteMap.put(blockIndex, blockName);




        });

        // Loop through blocks and add to list

        byte[] blocksDataOriginal = tag.getByteArray("BlockData");

        if (blocksDataOriginal.length == 0) {
            throw new IllegalArgumentException("Cannot find BlockData");
        }

        for (int y = 0; y < height; y++) {

            callback.update((float) y / height, "Loading schem file V2");

            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {

                    int index = (y * width * length + z * width + x);
                    byte type = blocksDataOriginal[index];
                    int typeInt = type & 0xFF;

                    String blockName = paletteMap.get(typeInt);
                    if (blockName == null) {
                        logger.warn("Invalid: wrong index palette not found");
                        blockName = "minecraft:air";
                    }
                    Block newBlock = Block.fromBlockName(blockName);

                    schemVolume.setBlock(x, y, z, newBlock);


                }
            }
        }

        return schemVolume;
    }

    private static int readVarInt(byte[] data, int[] offset) {
        int numRead = 0;
        int result = 0;
        int read;
        do {
            if (offset[0] >= data.length) throw new IllegalStateException("VarInt read out of bounds");
            read = data[offset[0]++] & 0xFF;           // IMPORTANT: mask to 0xFF
            int value = read & 0x7F;
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) throw new IllegalStateException("VarInt is too big");
        } while ((read & 0x80) != 0);
        return result;
    }

    public Volume loadSchemV3ToVolume(CompoundTag tag, ProgressCallback callback) {

        CompoundTag schematicTag = tag.getCompoundTag("Schematic");
        if (schematicTag == null) {
            throw new IllegalArgumentException("Cannot find Schematic tag for V3");
        }
        tag = schematicTag;

        short height = tag.getShort("Height");
        short length = tag.getShort("Length");
        short width = tag.getShort("Width");

        int dataVersion = tag.getInt("DataVersion");

        if (Config.DATA_VERSION < dataVersion) {
            callback.showWarning(String.format("Warning: Schematic uses a newer version\nSchematic: %d\nYour version: %d\n\nExpect missing blocks.", dataVersion, Config.DATA_VERSION));
        }


        Volume schemVolume = new Volume(width, height, length);

        // Build the palette
        CompoundTag paletteOriginalRaw;
        paletteOriginalRaw = tag.getCompoundTag("Blocks").getCompoundTag("Palette");

        if (paletteOriginalRaw == null) {
            throw new IllegalArgumentException("Cannot find palette");

        }
        CompoundTag paletteOriginal = paletteOriginalRaw;



        Map<Integer, String> paletteMap = new HashMap<>();


        paletteOriginalRaw.forEach(children -> {
            String blockName = children.getKey();

            int blockIndex = paletteOriginal.getInt(blockName);



            paletteMap.put(blockIndex, blockName);




        });

        // Loop through blocks and add to list

        byte[] blocksDataOriginal = tag.getCompoundTag("Blocks").getByteArray("Data");

        if (blocksDataOriginal.length == 0) {
            throw new IllegalArgumentException("Cannot find Data");
        }



        int[] offset = new int[]{0}; // pointer to current byte

        for (int y = 0; y < height; y++) {
            callback.update((float) y / height, "Loading schem file V3");

            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int paletteIndex = readVarInt(blocksDataOriginal, offset);
                    String blockName = paletteMap.get(paletteIndex);
                    if (blockName == null) {
                        logger.warn("Invalid palette index: {}. Replacing with air", paletteIndex);
                        blockName = "minecraft:air";
                    };
                    schemVolume.setBlock(x, y, z, Block.fromBlockName(blockName));
                }
            }
        }

        return schemVolume;
    }

    public Volume loadSchemToVolume(File file, ProgressCallback callback) throws IOException {
        //CompoundBinaryTag tag = BinaryTagIO.reader().read(file.toPath(), BinaryTagIO.Compression.GZIP);
        NamedTag tag_root = NBTUtil.read(file);
        CompoundTag tag = (CompoundTag) tag_root.getTag();

        IntTag versionTag = tag.getIntTag("Version");
        if (versionTag == null) {
            CompoundTag schematicTag = tag.getCompoundTag("Schematic");
            if (schematicTag == null) {
                throw new IllegalArgumentException("Cannot find Schematic tag");
            }
            versionTag = schematicTag.getIntTag("Version");
            if (versionTag == null) {
                throw new IllegalArgumentException("Cannot find version tag for V3 reader");
            }

        }


        if (versionTag.asInt() == 2) {
            logger.info("Load schem file version 2");
            return this.loadSchemV2ToVolume(tag, callback);
        } else if (versionTag.asInt() == 3) {
            logger.info("Load schem file version 3");
            logger.warn("--- WARNING ---");
            logger.warn("Schematic V3 Version detected!");
            logger.warn("The v3 loader is extremely unstable and produces inaccurate results!");
            return this.loadSchemV3ToVolume(tag, callback);
        } else {
            throw new IllegalArgumentException("Unsupported schem version: " + versionTag);
        }
    }

}
