package com.carilt01.schematictonbt;

import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

public class VolumeMeasurementSerializer {
    
    private static final Logger logger = LoggerFactory.getLogger(VolumeMeasurementSerializer.class);
    
    public VolumeMeasurementSerializer() {

    }

    /*
    This function will serialize the volume into NBT format. However, the serialized data is not intended to be read by Minecraft. It is instead only intended to find the best chunk size for splitting the structure.

     */
    public byte[] serializeVolume(Volume volume) throws IOException {
        logger.info("Preparing...");
        Map<Block, Integer> palette = new HashMap<>();

        logger.info("Creating palette...");

        //Map<Block, Integer> palette = new HashMap<>();
        AtomicInteger paletteIndex = new AtomicInteger(0);

        for (Block b : volume) {
            if (b.getBlockName().startsWith("minecraft:air")) continue;
            palette.computeIfAbsent(b, key -> paletteIndex.getAndIncrement());
        }

        ListTag<CompoundTag> blockListTag = new ListTag<>(CompoundTag.class);

        logger.info("Building volume...");

        // --- 2. Build NBT blocks efficiently ---


        int width = volume.getWidth();
        int length = volume.getLength();
        int numberOfBlocksSerialized = 0;

        int i =0;
        for (Block b: volume) {
            if (b.getBlockName().startsWith("minecraft:air")) continue;

            // Convert linear index to x,y,z
            int y = i / (width * length);
            int z = (i / width) % length;
            int x = i % width;

            // Avoid creating new lists repeatedly
            ListTag<IntTag> positionTag = new ListTag<>(IntTag.class);
            positionTag.add(new IntTag(x));
            positionTag.add(new IntTag(y));
            positionTag.add(new IntTag(z));

            CompoundTag blockTag = new CompoundTag();
            blockTag.put("pos", positionTag);
            blockTag.put("state", new IntTag(palette.get(b)));

            blockListTag.add(blockTag);

            numberOfBlocksSerialized++;
            i++;

        }

        int NUMBER_OF_BLOCKS_MAXIMUM = 256_000;

        if (numberOfBlocksSerialized > NUMBER_OF_BLOCKS_MAXIMUM) {

            return new byte[256 * 1024];
        }

        logger.info("Number of blocks serialized:{}", numberOfBlocksSerialized);

        CompoundTag root_ct = new CompoundTag();
        root_ct.put("blocks", blockListTag);

        NamedTag root = new NamedTag("root", root_ct);

        logger.info("Serializing...");
        byte[] nbtData;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(byteArrayOutputStream);
             NBTOutputStream nbtOut = new NBTOutputStream(gzipOut)) {

            nbtOut.writeTag(root, 512);
            nbtOut.flush();
            gzipOut.finish(); // finalize compression
            nbtData = byteArrayOutputStream.toByteArray();
        }
        return nbtData;
    }
}
