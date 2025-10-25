package com.carilt01.schematictonbt;

import com.carilt01.schematictonbt.loaders.SchemFileLoader;
import com.carilt01.schematictonbt.loaders.SchematicFileLoader;
import com.carilt01.schematictonbt.userInterface.MainUI;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {


    public static void main(String[] args) {

        try {
            System.out.println("Hello and welcome buddy!");

            Application app = new Application();
            app.run();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}