package com.carilt01.schematictonbt.loaders;

import com.carilt01.schematictonbt.Block;
import com.carilt01.schematictonbt.Config;
import com.carilt01.schematictonbt.ProgressCallback;
import com.carilt01.schematictonbt.Volume;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LitematicFileLoader implements FileLoader {

    private static final Logger logger = LoggerFactory.getLogger(LitematicFileLoader.class);


    public Volume loadFileToVolume(File file, ProgressCallback callback) throws IOException {

        NamedTag tagRoot = NBTUtil.read(file);
        CompoundTag tag = (CompoundTag) tagRoot.getTag();

        IntTag versionTag = tag.getIntTag("MinecraftDataVersion");

        if (Config.DATA_VERSION < versionTag.asInt()) {
            callback.showWarning("Warning: File version higher than current target version. Expect missing blocks.");
        }

        CompoundTag regions = tag.getCompoundTag("Regions");

        if (regions == null) {
            throw new IllegalArgumentException("Cannot find Regions tag");
        }

        Map.Entry<String, Tag<?>> firstEntry = regions.entrySet().iterator().next();

        CompoundTag regionTag = (CompoundTag) firstEntry.getValue();
        ListTag<CompoundTag> paletteTag = regionTag.getListTag("BlockStatePalette").asCompoundTagList();

        // Get size

        CompoundTag sizeTag = regionTag.getCompoundTag("Size");

        final int sizeX = Math.abs(sizeTag.getInt("x"));
        final int sizeY = Math.abs(sizeTag.getInt("y"));
        final int sizeZ = Math.abs(sizeTag.getInt("z"));


        logger.info("Size of Litematic region is: {}, {}, {}", sizeX, sizeY, sizeZ);

        // Build palette

        final int paletteSize = paletteTag.size();

        Map<Integer, Block> paletteMap = new HashMap<>();


        AtomicInteger paletteIndex = new AtomicInteger(0);

        paletteTag.forEach(children -> {

            String blockName = children.getString("Name");
            CompoundTag properties = children.getCompoundTag("Properties");

            Map<String, String> propertiesMap = new HashMap<>();

            if (properties != null) {
                properties.forEach(propertyChildren -> propertiesMap.put(propertyChildren.getKey(), propertyChildren.getValue().valueToString()));
            }

            paletteMap.put(paletteIndex.get(), Block.create(blockName, propertiesMap));

            paletteIndex.getAndIncrement();
        });

        // In Litematic files, size is negative, due to a different coordinate system
        // https://litemapy.readthedocs.io/en/latest/litematics.html

        Volume structureVolume = new Volume(sizeX, sizeY, sizeZ);

        //int bitsPerEntry = Math.max(2, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
        //long mask = (1L << bitsPerEntry) - 1;

        int bitsPerEntry = Math.max(2, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
        long mask = (1L << bitsPerEntry) - 1;

        LongArrayTag blockArray = regionTag.getLongArrayTag("BlockStates");

        int blocksPerLong = 64 / bitsPerEntry;
        final int totalBlocks = sizeX * sizeY * sizeZ;

        long[] data = blockArray.getValue();

        for (int idx = 0; idx < totalBlocks; idx++) {
            //int localPaletteIndex = (int) ((datum >>> (j * bitsPerEntry)) & mask);
            long startBit = (long) idx * bitsPerEntry;
            int startLongIndex = (int) (startBit / 64);
            int endLongIndex = (int) ((startBit + bitsPerEntry - 1) / 64);
            int offset = (int) (startBit % 64);

            int y = idx / (sizeZ * sizeX);
            int remainder = idx % (sizeZ * sizeX);
            int z = remainder / sizeX;
            int x = remainder % sizeX;

            int localPaletteIndex;

            if (startLongIndex == endLongIndex) {
                // The entry is contained entirely within one long
                localPaletteIndex = (int) ((data[startLongIndex] >>> offset) & mask);
            } else {
                // The entry splits across two longs (Packed format)
                // Read the end of the first long
                long val1 = data[startLongIndex] >>> offset;
                // Read the start of the second long
                // (64 - offset) is how many bits we took from the first long
                long val2 = data[endLongIndex] << (64 - offset);

                localPaletteIndex = (int) ((val1 | val2) & mask);
            }

            Block localBlock = paletteMap.get(localPaletteIndex);

            if (localBlock == null) {
                throw new IllegalArgumentException(String.format("Invalid palette index: %s", localPaletteIndex));
            }

            structureVolume.setBlock(x, y, z, localBlock);

            //logger.info("Set block: {}", localBlock.getBlockName());
        }

        logger.info("Litematic loader ended ");

        return structureVolume;
    }

}
