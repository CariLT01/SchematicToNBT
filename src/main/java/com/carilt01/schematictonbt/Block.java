package com.carilt01.schematictonbt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Block {
    private Map<String, String> properties;
    private String blockName;

    public Block() {
        this.properties = new HashMap<>();
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public String getBlockName() {
        return this.blockName;
    }

    public static Map<String, String> parseBlockProperties(String blockState) {
        Map<String, String> properties = new HashMap<>();

        int bracketStart = blockState.indexOf('[');
        int bracketEnd = blockState.indexOf(']');

        if (bracketStart != -1 && bracketEnd != -1 && bracketEnd > bracketStart) {
            String props = blockState.substring(bracketStart + 1, bracketEnd);
            String[] pairs = props.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    properties.put(kv[0].trim(), kv[1].trim());
                }
            }
        }

        return properties;
    }

    public static Block fromBlockName(String name) {
        Map<String, String> blockProperties = parseBlockProperties(name);
        String blockName = name.split("\\[")[0];

        Block newBlock = new Block();
        newBlock.setBlockName(blockName);
        newBlock.setProperties(blockProperties);

        return newBlock;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block)) return false;
        Block other = (Block) o;
        return Objects.equals(blockName, other.blockName) &&
                Objects.equals(new TreeMap<>(properties), new TreeMap<>(other.properties));
    }

    @Override
    public int hashCode() {
        int accumulatedHash = (blockName == null ? 0 : blockName.hashCode());
        Map<String, String> sortedProperties = new TreeMap<>(properties);
        for (Map.Entry<String, String> entry : sortedProperties.entrySet()) {
            int keyHash = entry.getKey() == null ? 0 : entry.getKey().hashCode();
            int valueHash = entry.getValue() == null ? 0 : entry.getValue().hashCode();
            accumulatedHash ^= keyHash ^ valueHash;
        }
        return accumulatedHash;
    }
}
