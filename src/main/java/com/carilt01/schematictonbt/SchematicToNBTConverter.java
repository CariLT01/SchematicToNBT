package com.carilt01.schematictonbt;

import com.carilt01.schematictonbt.loaders.SchemFileLoader;
import com.carilt01.schematictonbt.loaders.SchematicFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchematicToNBTConverter {

    private final SchematicFileLoader schematicFileLoader;
    private final SchemFileLoader schemFileLoader;
    private final NBTExporter nbtExporter;
    private final VolumeSplitter volumeSplitter;
    private final VolumesLayoutImageExporter volumesLayoutImageExporter;
    private static final Logger logger = LoggerFactory.getLogger(SchematicToNBTConverter.class);

    public SchematicToNBTConverter() {
        schematicFileLoader = new SchematicFileLoader();
        nbtExporter = new NBTExporter();
        schemFileLoader = new SchemFileLoader();
        volumeSplitter = new VolumeSplitter();
        volumesLayoutImageExporter = new VolumesLayoutImageExporter();
    }

    public Volume loadSchematicFile(File file) throws IOException {

        String filePath = file.getAbsolutePath();
        Volume structureVolume;

        if (filePath.endsWith(".schem")) {
            structureVolume = schemFileLoader.loadSchemToVolume(file);
        } else if (filePath.endsWith(".schematic")) {
            structureVolume = schematicFileLoader.loadSchematicToVolume(file);
        } else {
            throw new IllegalArgumentException("Invalid file type");
        }

        return structureVolume;
    }

    public void convertFile(String filePath, boolean shouldSplit, int maxVolumeSize, ProgressCallback progressCallback) throws IOException {
        File file = new File(filePath);
        Volume structureVolume = this.loadSchematicFile(file);


        if (!shouldSplit) {
            this.nbtExporter.exportNbt(structureVolume, file.getAbsolutePath() + "-output-ns.nbt");
            return;
        }



        List<VolumeCoords> outputCoords = this.volumeSplitter.splitVolume(structureVolume, progressCallback, maxVolumeSize);

        // Convert to Volume block

        List<VolumeBlockEntry> splittedVolumes = new ArrayList<>();

        int tracker = 0;

        for (VolumeCoords coordinates : outputCoords) {
            tracker++;
            progressCallback.update((float) tracker / outputCoords.size(), "Unpacking...");

            Volume vol = structureVolume.collectBlocksInArea(
                    coordinates.beginX(),
                    coordinates.beginY(),
                    coordinates.beginZ(),
                    coordinates.endX(),
                    coordinates.endY(),
                    coordinates.endZ()
            );
            splittedVolumes.add(new VolumeBlockEntry(vol, coordinates.beginX(), coordinates.beginZ()));
        }

        volumesLayoutImageExporter.exportLayout(splittedVolumes, structureVolume.getWidth(), structureVolume.getLength(), file.getAbsolutePath() + "-layout.png");


        int counter = 0;

        for (VolumeBlockEntry vol : splittedVolumes) {
            progressCallback.update((float) (counter + 1) / splittedVolumes.size(), "Saving...");

            this.nbtExporter.exportNbt(vol.volume(), file.getAbsolutePath() + "-output" + counter + ".nbt");
            counter++;
        }

        splittedVolumes = null; // Free memory (at least, attempt to)
        structureVolume = null;
        splittedVolumes = null;
        outputCoords = null;


    }

}
