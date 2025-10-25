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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

public class VolumeMeasurementSerializer {
    
    private static final Logger logger = LoggerFactory.getLogger(VolumeMeasurementSerializer.class);
    
    public VolumeMeasurementSerializer() {

    }

    /*
    This function will serialize the volume into NBT format. However, the serialized data is not intended to be read by Minecraft. It is instead only intended to find the best chunk size for splitting the structure.

     */
    public byte[] serializeVolume(Volume volume, Optional<ProgressCallback> callback) throws IOException {
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

        int width = volume.getWidth();
        int length = volume.getLength();
        int height = volume.getHeight();

        int numberOfBlocksSerialized = 0;
        int totalBlocks = width * length * height;


        long startTime = System.currentTimeMillis();
        long lastCallback = startTime;

        int airIndex = volume.getAirIndex();
        if (airIndex == -1) {
            logger.warn("Cannot find air index. Expect degraded performance");
        }

        for (int index = 0; index < volume.blockData.length; index++) {
            short paletteIndexShort = volume.blockData[index];
            if (airIndex != -1) {
                if (paletteIndexShort == airIndex) continue; // skip air
            }


            Block b = volume.paletteReverseMap.get((int) paletteIndexShort);
            if (airIndex == -1) {
                if (Objects.equals(b.getBlockName(), "minecraft:air")) continue;
            }

            int y = index / (width * length);
            int z = (index / width) % length;
            int x = index % width;

            // Reuse ListTag and CompoundTag if possible
            ListTag<IntTag> positionTag = new ListTag<>(IntTag.class);
            positionTag.add(new IntTag(x));
            positionTag.add(new IntTag(y));
            positionTag.add(new IntTag(z));

            CompoundTag blockTag = new CompoundTag();
            blockTag.put("pos", positionTag);
            blockTag.put("state", new IntTag(palette.get(b)));

            blockListTag.add(blockTag);

            numberOfBlocksSerialized++;



            // Update callback every 1 second (not every block)
            if (callback.isPresent()) {
                long now = System.currentTimeMillis();
                if (now - lastCallback > 1000) {
                    callback.get().update((float) index / totalBlocks, "Building volume...");
                    lastCallback = now;
                }
            }
        }

        int NUMBER_OF_BLOCKS_MAXIMUM = 1_000_000;

        if (numberOfBlocksSerialized > NUMBER_OF_BLOCKS_MAXIMUM) {

            return new byte[256 * 1024];
        }

        logger.info("Number of blocks serialized:{}", numberOfBlocksSerialized);

        CompoundTag root_ct = new CompoundTag();
        root_ct.put("blocks", blockListTag);

        NamedTag root = new NamedTag("root", root_ct);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        if (elapsedTime > 1000) {
            callback.ifPresent(progressCallback -> progressCallback.update(1, "Estimating size..."));
        }

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
