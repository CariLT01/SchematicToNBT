package com.carilt01.schematictonbt;

import java.util.*;

public class VolumeGenerateGiveList {

    public VolumeGenerateGiveList() {

    }

    public List<String> getGiveListFromVolume(Volume volume) {

        Map<String, Integer> giveMap = new HashMap<>();

        for (Block block : volume) {

            if (block.getBlockName().startsWith("minecraft:air")) continue;
            if (!giveMap.containsKey(block.getBlockName())) {
                giveMap.put(block.getBlockName(), 1);
                continue;
            }

            giveMap.compute(block.getBlockName(), (k, count) -> count + 1);

        }

        return getGiveList(giveMap);

    }

    private static List<String> getGiveList(Map<String, Integer> giveMap) {
        List<String> giveList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry: giveMap.entrySet()) {


            int dividor = 1;

            if (entry.getKey().endsWith("_door") && !entry.getKey().endsWith("trapdoor")) {
                dividor = 2;
            } else if (entry.getKey().endsWith("bed")) {
                dividor = 2;
            }

            String blockName = entry.getKey();
            blockName = blockName.replace("wall_torch", "torch");
            blockName = blockName.replace("sign", "oak_sign");
            blockName = blockName.replace("tripwire", "tripwire_hook");

            giveList.add("give @s " + blockName + " " + entry.getValue() / dividor);
        }
        return giveList;
    }

}
