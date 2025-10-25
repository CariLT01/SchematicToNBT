package com.carilt01.schematictonbt.loaders;

import com.carilt01.schematictonbt.Block;
import com.carilt01.schematictonbt.Main;
import com.carilt01.schematictonbt.Volume;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.*;
import java.util.*;

public class SchematicFileLoader {

    private Map<String, String> mappedBlockTypes = new HashMap<>();

    public SchematicFileLoader() {
        this.loadBlockTypes();
    }

    private void loadBlockTypes() {
        System.out.println("Loading block types...");
        Gson gson = new Gson();

        try (InputStream inputStream = Main.class.getResourceAsStream("/blocks.json")) {
            // Parse the top-level object
            assert inputStream != null;
            JsonObject root = gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);
            JsonObject blocks = root.getAsJsonObject("blocks");

            for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                String key = entry.getKey(); // "0:0"
                String value = entry.getValue().getAsString(); // "minecraft:stone"


                mappedBlockTypes.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Volume loadSchematicToVolume(File file) throws IOException {



        //CompoundTag tag = BinaryTagIO.reader().read(file.toPath(), BinaryTagIO.Compression.GZIP);
        NamedTag tag_root = NBTUtil.read(file);
        CompoundTag tag = (CompoundTag) tag_root.getTag();

        short width = tag.getShort("Width");
        short height = tag.getShort("Height");
        short length = tag.getShort("Length");

        Volume schematicVolume = new Volume(width, height, length);

        byte[] blocksOriginal = tag.getByteArray("Blocks");
        byte[] metadataOriginal = tag.getByteArray("Data");





        // Palette pass, scan for all unique blocks





        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int index = (y * width * length + z * width + x);
                    byte blockData = blocksOriginal[index];
                    byte blockMetadata = metadataOriginal[index];
                    int unsignedBlockData = blockData & 0xFF;
                    String entry = mappedBlockTypes.get(unsignedBlockData + ":" + blockMetadata);
                    String blockName = "";

                    blockName = Objects.requireNonNullElse(entry, "minecraft:air");

                    Block newBlock = Block.fromBlockName(blockName);

                    schematicVolume.setBlock(x, y, z, newBlock);
                }
            }
        }

        return schematicVolume;
    }

}
