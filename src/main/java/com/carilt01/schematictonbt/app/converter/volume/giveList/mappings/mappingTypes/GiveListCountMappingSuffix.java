package com.carilt01.schematictonbt.app.converter.volume.giveList.mappings.mappingTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GiveListCountMappingSuffix implements GiveListMapping {

    private final static Logger logger = LoggerFactory.getLogger(GiveListCountMappingSuffix.class);

    private String suffix;
    private float count;

    public GiveListCountMappingSuffix(String suffix, float count) {
        this.suffix = suffix;
        this.count = count;
    }

    public float processBlockCount(String blockName, Map<String, String> properties) {
       if (blockName.endsWith(this.suffix)) {

           logger.info("Modified by {} because block name {} was found", this.count, blockName);

           return this.count;
       } else {
           return 0;
       }
    }

    public String processBlockName(String blockName, Map<String, String> properties) {
        return blockName;
    }
}
