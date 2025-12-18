package com.carilt01.schematictonbt.app.converter.volume.giveList.mappings.mappingTypes;

import java.util.Map;

public class GiveListMappingReplacement implements GiveListMapping {

    private final String pattern;
    private final String replacement;

    public GiveListMappingReplacement(String pattern, String replacement) {
        this.pattern = pattern;
        this.replacement = replacement;
    }

    public String processBlockName(String blockName, Map<String, String> properties) {
        return blockName.replace(pattern, replacement);
    }

    public float processBlockCount(String blockName, Map<String, String> properties) {
        return 0;
    }

}
