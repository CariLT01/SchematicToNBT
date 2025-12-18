package com.carilt01.schematictonbt.app.converter.volume.giveList.mappings.mappingTypes;

import java.util.Map;

public interface GiveListMapping {



    String processBlockName(String blockName, Map<String, String> properties);
    float processBlockCount(String blockName, Map<String, String> properties);
}
