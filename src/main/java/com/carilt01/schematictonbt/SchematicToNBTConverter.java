package com.carilt01.schematictonbt;

import com.carilt01.schematictonbt.loaders.SchemFileLoader;
import com.carilt01.schematictonbt.loaders.SchematicFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SchematicToNBTConverter {

    private SchematicFileLoader schematicFileLoader;
    private SchemFileLoader schemFileLoader;
    private NBTExporter nbtExporter;
    private VolumeSplitter volumeSplitter;
    private VolumesLayoutImageExporter volumesLayoutImageExporter;

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



        List<VolumeBlockEntry> splittedVolumes = this.volumeSplitter.splitVolume(structureVolume, progressCallback, maxVolumeSize);
        volumesLayoutImageExporter.exportLayout(splittedVolumes, structureVolume.getWidth(), structureVolume.getLength(), file.getAbsolutePath() + "-layout.png");


        int counter = 0;

        for (VolumeBlockEntry vol : splittedVolumes) {
            progressCallback.update((float) (counter + 1) / splittedVolumes.size(), "Saving...");

            this.nbtExporter.exportNbt(vol.volume(), file.getAbsolutePath() + "-output" + counter + ".nbt");
            counter++;
        }


    }

}
