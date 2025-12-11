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

    private static final Logger logger = LoggerFactory.getLogger(SchematicFileLoader.class);


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
                properties.forEach(propertyChildren -> {
                    propertiesMap.put(propertyChildren.getKey(), propertyChildren.getValue().valueToString());
                });
            }

            paletteMap.put(paletteIndex.get(), Block.create(blockName, propertiesMap));

            paletteIndex.getAndIncrement();
        });

        // In Litematic files, size is negative, due to a different coordinate system
        // https://litemapy.readthedocs.io/en/latest/litematics.html

        Volume structureVolume = new Volume(sizeX, sizeY, sizeZ);

        int bitsPerEntry = Math.max(2, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
        long mask = (1L << bitsPerEntry) - 1;

        LongArrayTag blockArray = regionTag.getLongArrayTag("BlockStates");

        int blocksPerLong = 64 / bitsPerEntry;
        int idx = 0;

        for (long datum : blockArray.getValue()) {
            for (int j = 0; j < blocksPerLong && idx < sizeX * sizeY * sizeZ; j++) {
                int localPaletteIndex = (int) ((datum >>> (j * bitsPerEntry)) & mask);

                int y = idx % sizeY;
                int z = (idx / sizeY) % sizeZ;
                int x = idx / (sizeY * sizeZ);

                Block localBlock = paletteMap.get(localPaletteIndex);

                if (localBlock == null) {
                    throw new IllegalArgumentException(String.format("Invalid palette index: %s", localPaletteIndex));
                }

                structureVolume.setBlock(x, y, z, localBlock);

                //logger.info("Set block: {}", localBlock.getBlockName());

                idx++;
            }
        }

        logger.info("Litematic loader ended with index: {}", idx);

        return structureVolume;
    }

}
