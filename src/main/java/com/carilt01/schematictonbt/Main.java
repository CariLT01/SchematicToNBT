package com.carilt01.schematictonbt;

import com.carilt01.schematictonbt.loaders.SchemFileLoader;
import com.carilt01.schematictonbt.loaders.SchematicFileLoader;

import java.io.File;
import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String workingDir = System.getProperty("user.dir");
        System.out.println("Working directory: " + workingDir);

        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
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
        inputFile = "270.schematic";

        if (inputFile == null) {
            System.out.println("No input file specified!");
            return;
        }

        Volume schematicVolume;

        SchematicFileLoader schematicFileLoader = new SchematicFileLoader();
        SchemFileLoader schemFileLoader = new SchemFileLoader();
        NBTExporter nbtExporter = new NBTExporter();

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

            System.out.println("Converting to .nbt");
            nbtExporter.exportNbt(schematicVolume, "output_v2.nbt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}