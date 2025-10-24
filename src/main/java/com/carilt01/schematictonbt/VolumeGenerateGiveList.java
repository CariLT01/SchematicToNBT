package com.carilt01.schematictonbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolumeGenerateGiveList {

    public VolumeGenerateGiveList() {

    }

    public List<String> getGiveListFromVolume(Volume volume) {

        Map<String, Integer> giveMap = new HashMap<>();

        for (Block block : volume.getBlocks()) {

            if (block.getBlockName().startsWith("minecraft:air")) continue;
            if (!giveMap.containsKey(block.getBlockName())) {
                giveMap.put(block.getBlockName(), 1);
                continue;
            }
            giveMap.compute(block.getBlockName(), (k, count) -> count + 1);

        }

        List<String> giveList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry: giveMap.entrySet()) {

            int dividor = 1;

            if (entry.getKey().endsWith("door") && !entry.getKey().endsWith("trapdoor")) {
                dividor = 2;
            } else if (entry.getKey().endsWith("bed")) {
                dividor = 2;
            }

            giveList.add("give @s " + entry.getKey().replace("wall_torch", "torch") + " " + entry.getValue() / dividor);
        }

        return giveList;

    }

}
