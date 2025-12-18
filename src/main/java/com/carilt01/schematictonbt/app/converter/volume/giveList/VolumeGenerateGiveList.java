package com.carilt01.schematictonbt.app.converter.volume.giveList;

import com.carilt01.schematictonbt.app.converter.volume.Block;
import com.carilt01.schematictonbt.app.converter.volume.Volume;
import com.carilt01.schematictonbt.app.converter.volume.giveList.mappings.mappingTypes.GiveListMappingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VolumeGenerateGiveList {

    private final GiveListMappingConfiguration mappings;
    private static final Logger logger = LoggerFactory.getLogger(VolumeGenerateGiveList.class);

    public VolumeGenerateGiveList(GiveListMappingConfiguration blockMappingConfiguration) {
        this.mappings = blockMappingConfiguration;
    }

    public List<String> getGiveListFromVolume(Volume volume) {

        Map<Block, Float> giveMap = new HashMap<>();

        for (Block block : volume) {

            if (block.getBlockName().startsWith("minecraft:air")) continue;
            if (block.getBlockName().startsWith("minecraft:water")) continue;

            float giveCount = mappings.processBlockCount(block.getBlockName(), block.getProperties());

            if (!giveMap.containsKey(block)) {
                giveMap.put(block, giveCount);
                continue;
            }



            giveMap.compute(block, (k, count) -> count + giveCount);



        }

        return getGiveList(giveMap);

    }

    private List<String> getGiveList(Map<Block, Float> giveMap) {
        List<String> giveList = new ArrayList<>();

        Map<String, Integer> groupedGiveMap = new HashMap<>();

        for (Map.Entry<Block, Float> entry: giveMap.entrySet()) {

            String blockName = entry.getKey().getBlockName();
            String processedBlockName = mappings.processBlockName(blockName, entry.getKey().getProperties());
            float blockCount = entry.getValue();
            if (Math.floor(blockCount) != blockCount) {
                logger.warn("Block count is not an integer: {} for block {}. Rounding up to nearest integer.", blockCount, entry.getKey().getBlockName());
            }

            int intBlockCount = (int) Math.ceil(blockCount);

            if (!groupedGiveMap.containsKey(processedBlockName)) {
                groupedGiveMap.put(processedBlockName, intBlockCount);
            } else {
                groupedGiveMap.put(processedBlockName, groupedGiveMap.get(processedBlockName) + intBlockCount);
            }

        }
        for (Map.Entry<String, Integer> entry: groupedGiveMap.entrySet()) {




            /*

            Mappings have been moved to:
             -> SchematicToNBTConverter.java:26

            int divisor = 1;

            if (entry.getKey().getBlockName().endsWith("_door") && !entry.getKey().getBlockName().endsWith("trapdoor")) {
                divisor = 2;
            } else if (entry.getKey().getBlockName().endsWith("bed")) {
                divisor = 2;
            }

            String blockName = entry.getKey();

            String mappedBlockName = this.mappings.processBlockName(blockName, )

            blockName = blockName.replace("wall_torch", "torch");
            blockName = blockName.replace("sign", "oak_sign");
            blockName = blockName.replace("wall_sign", "oak_sign");
            blockName = blockName.replace("tripwire", "tripwire_hook");*/

            String block = entry.getKey();
            int count = entry.getValue();

            giveList.add("give @s " + block + " " + count);
        }
        return giveList;
    }

}
