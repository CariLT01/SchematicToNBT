package com.carilt01.schematictonbt;

import com.carilt01.schematictonbt.loaders.SchemFileLoader;
import com.carilt01.schematictonbt.loaders.SchematicFileLoader;
import com.carilt01.schematictonbt.userInterface.MainUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            logger.info("Hello and welcome buddy!");

            Application app = new Application();
            app.run();


        } catch (Exception e) {
            logger.error("An error occurred: ", e);
        }

    }
}