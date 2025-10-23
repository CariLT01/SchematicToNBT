package com.carilt01.schematictonbt;

import com.carilt01.schematictonbt.loaders.SchemFileLoader;
import com.carilt01.schematictonbt.loaders.SchematicFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {



        String workingDir = System.getProperty("user.dir");
        System.out.println("Working directory: " + workingDir);


        System.out.println("Hello and welcome buddy!");

        String inputFile = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--inputFile")) {
                if (i + 1 < args.length) {
                    inputFile = args[i + 1];
                    i++; // skip the value
                }
            } else {
                System.out.println("Unknown argument: " + args[i]);
            }
        }

        // Debug purposes
        inputFile = (inputFile == null) ? "235.schematic" : inputFile;


        Volume schematicVolume;

        SchematicFileLoader schematicFileLoader = new SchematicFileLoader();
        SchemFileLoader schemFileLoader = new SchemFileLoader();
        NBTExporter nbtExporter = new NBTExporter();
        VolumeSplitter volumeSplitter = new VolumeSplitter();

        try {
            if (inputFile.endsWith(".schematic")) {
                System.out.println("Loading using .schematic legacy loader");
                schematicVolume = schematicFileLoader.loadSchematicToVolume(new File(inputFile));
            } else if (inputFile.endsWith(".schem")) {
                schematicVolume = schemFileLoader.loadSchemToVolume(new File(inputFile));
            } else {
                System.out.println("Invalid file extension");
                return;
            }

            /*System.out.println("Splitting...");
            List<Volume> splitVolumes = volumeSplitter.splitVolume(schematicVolume);

            System.out.println("Exporting volumes...");
            int counter = 0;
            for (Volume vol : splitVolumes) {
                nbtExporter.exportNbt(vol, "output_v2_schematic" + counter  + ".nbt");
                counter++;
            }*/

            nbtExporter.exportNbt(schematicVolume, "output_v2.nbt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}