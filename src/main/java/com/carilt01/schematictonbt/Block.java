package com.carilt01.schematictonbt;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class Block {
    private final Map<String, String> properties; // immutable
    private final String blockName;
    private final int cachedHash;

    /**
     * Private constructor. Use static factory methods to create a Block.
     */
    private Block(String blockName, Map<String, String> properties) {
        this.blockName = blockName;
        // Create a sorted, unmodifiable copy of properties for consistency
        this.properties = Collections.unmodifiableMap(new TreeMap<>(properties));
        this.cachedHash = computeHash();
    }

    // --- Factory method to parse from string ---
    public static Block fromBlockName(String name) {
        Map<String, String> blockProperties = parseBlockProperties(name);
        String blockName = name.split("\\[")[0];
        return new Block(blockName, blockProperties);
    }

    public static Block create(String name, Map<String, String> properties) {
        return new Block(name, properties);
    }

    // --- Parse block properties from string like "stone[foo=bar]" ---
    private static Map<String, String> parseBlockProperties(String blockState) {
        Map<String, String> properties = new TreeMap<>();

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

    // --- Getters ---
    public String getBlockName() {
        return blockName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    // --- Hashing and equality ---
    private int computeHash() {
        int hash = blockName == null ? 0 : blockName.hashCode();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            hash ^= (entry.getKey() == null ? 0 : entry.getKey().hashCode())
                    ^ (entry.getValue() == null ? 0 : entry.getValue().hashCode());
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Block other)) return false;
        return Objects.equals(blockName, other.blockName) &&
                Objects.equals(properties, other.properties);
    }

    @Override
    public String toString() {
        if (properties.isEmpty()) return blockName;
        StringBuilder sb = new StringBuilder(blockName).append('[');
        properties.forEach((k, v) -> sb.append(k).append('=').append(v).append(','));
        sb.setLength(sb.length() - 1); // remove last comma
        sb.append(']');
        return sb.toString();
    }
}
