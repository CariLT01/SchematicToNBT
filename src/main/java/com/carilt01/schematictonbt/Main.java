package com.carilt01.schematictonbt;

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
        String outputFile = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--inputFile":
                    if (i + 1 < args.length) {
                        inputFile = args[i + 1];
                        i++; // skip the value
                    }
                    break;
                case "--outputFile":
                    if (i + 1 < args.length) {
                        outputFile = args[i + 1];
                        i++;
                    }
                    break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
            }
        }



        if (inputFile == null || outputFile == null) {
            System.out.println("No input file or output file specified!");
            return;
        }

        if (inputFile.endsWith(".schematic")) {
            System.out.println("Going through intermediary step of converting .schematic to .schem");
            SchematicToSchem converter = new SchematicToSchem();
            converter.convertFile(inputFile);
        } else if (inputFile.endsWith(".schem")) {
            System.out.println("Already in .schem format");
        } else {
            System.out.println("Invalid file extension");
        }

        System.out.println("Converting to .nbt");
        SchemToNBTStructure converter2 = new SchemToNBTStructure();
        converter2.convert("output.schem");
    }
}