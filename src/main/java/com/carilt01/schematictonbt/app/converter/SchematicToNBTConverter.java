package com.carilt01.schematictonbt.app.converter;

import com.carilt01.schematictonbt.app.converter.callbacks.ProgressCallback;
import com.carilt01.schematictonbt.app.converter.exporters.NBTExporter;
import com.carilt01.schematictonbt.app.converter.loaders.LitematicFileLoader;
import com.carilt01.schematictonbt.app.converter.loaders.SchemFileLoader;
import com.carilt01.schematictonbt.app.converter.loaders.SchematicFileLoader;
import com.carilt01.schematictonbt.app.converter.volume.*;
import com.carilt01.schematictonbt.app.converter.volume.giveList.mappings.mappingTypes.GiveListMappingConfiguration;
import com.carilt01.schematictonbt.app.converter.volume.splitter.VolumeSplitter;

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
    private final LitematicFileLoader litematicFileLoader;

    public SchematicToNBTConverter() {



        schematicFileLoader = new SchematicFileLoader();
        GiveListMappingConfiguration mappings = new GiveListMappingConfiguration.Builder()
                .addReplacementMapping("wall_torch", "torch")
                .addReplacementMapping("sign", "oak_sign")
                .addReplacementMapping("walL_sign", "oak_sign")
                .addReplacementMapping("tripwire", "tripwire_hook")
                .addReplacementMapping("large_fern", "fern")
                .addSuffixCountMapping("_door", -0.5f)
                .addSuffixCountMapping("bed", -0.5f)
                .build();
        nbtExporter = new NBTExporter(mappings);
        schemFileLoader = new SchemFileLoader();
        volumeSplitter = new VolumeSplitter();
        volumesLayoutImageExporter = new VolumesLayoutImageExporter();
        litematicFileLoader = new LitematicFileLoader();
    }

    public Volume loadSchematicFile(File file, ProgressCallback callback) throws IOException {

        String filePath = file.getAbsolutePath();
        Volume structureVolume;

        if (filePath.endsWith(".schem")) {
            structureVolume = schemFileLoader.loadFileToVolume(file, callback);
        } else if (filePath.endsWith(".schematic")) {
            callback.update(-1, "Loading schematic...");
            structureVolume = schematicFileLoader.loadFileToVolume(file, callback);
        } else if (filePath.endsWith(".litematic")) {
            callback.update(-1, "Loading litematic...");
            structureVolume = litematicFileLoader.loadFileToVolume(file, callback);
        } else {
            throw new IllegalArgumentException("Invalid file type");
        }

        return structureVolume;
    }

    public void convertFile(String filePath, boolean shouldSplit, int maxVolumeSize, ProgressCallback progressCallback, float quality, boolean useBinarySearch) throws IOException {
        File file = new File(filePath);
        Volume structureVolume = this.loadSchematicFile(file, progressCallback);


        if (!shouldSplit) {
            this.nbtExporter.exportNbt(structureVolume, file.getAbsolutePath() + "-output-ns.nbt");
            return;
        }



        List<VolumeCoords> outputCoordinates = this.volumeSplitter.splitVolume(structureVolume, progressCallback, maxVolumeSize, quality, useBinarySearch);

        // Convert to Volume block

        List<VolumeBlockEntry> splittedVolumes = new ArrayList<>();

        int tracker = 0;

        for (VolumeCoords coordinates : outputCoordinates) {
            tracker++;
            progressCallback.update((float) tracker / outputCoordinates.size(), "Unpacking...");

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

        volumesLayoutImageExporter.exportLayout(splittedVolumes, structureVolume.getWidth(), structureVolume.getLength(), structureVolume.getHeight(), file.getAbsolutePath() + "-layout.png", progressCallback);


        int counter = 0;

        for (VolumeBlockEntry vol : splittedVolumes) {
            progressCallback.update((float) (counter + 1) / splittedVolumes.size(), "Saving...");

            this.nbtExporter.exportNbt(vol.volume(), file.getAbsolutePath() + "-output" + counter + ".nbt");
            counter++;
        }

        // Free memory (at least, attempt to)


    }

}
