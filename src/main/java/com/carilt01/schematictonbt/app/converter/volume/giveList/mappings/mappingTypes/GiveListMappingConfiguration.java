package com.carilt01.schematictonbt.app.converter.volume.giveList.mappings.mappingTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GiveListMappingConfiguration {

    private static Logger logger = LoggerFactory.getLogger(GiveListMappingConfiguration.class);
    private final List<GiveListMapping> mappings;

    private GiveListMappingConfiguration(List<GiveListMapping> mappings) {
        this.mappings = mappings;
    }

    public String processBlockName(String blockName, Map<String, String> properties) {
        String newBlockName = blockName;

        for (GiveListMapping mapping : this.mappings) {
            newBlockName = mapping.processBlockName(newBlockName, properties);
        }

        return newBlockName;
    }

    public float processBlockCount(String blockName, Map<String, String> properties) {

        float newBlockCount = 1;

        for (GiveListMapping mapping : this.mappings) {
            float mappingModification = mapping.processBlockCount(blockName, properties);
            newBlockCount += mappingModification;

            //logger.info("Mapping modified by: {}", mappingModification);
        }



        return newBlockCount;
    }

    public static class Builder {

        private List<GiveListMapping> mappings = new ArrayList<>();

        public Builder addReplacementMapping(String pattern, String replacement) {
            mappings.add(new GiveListMappingReplacement(pattern, replacement));
            return this;
        }

        public Builder addSuffixCountMapping(String suffix, float count) {
            mappings.add(new GiveListCountMappingSuffix(suffix, count));
            return this;
        }

        public GiveListMappingConfiguration build() {
            return new GiveListMappingConfiguration(this.mappings);
        }

    }

}
