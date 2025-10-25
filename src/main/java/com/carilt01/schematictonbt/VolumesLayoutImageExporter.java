package com.carilt01.schematictonbt;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VolumesLayoutImageExporter {
    public VolumesLayoutImageExporter() {

    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f/6f) return p + (q - p) * 6 * t;
        if (t < 1f/2f) return q;
        if (t < 2f/3f) return p + (q - p) * (2f/3f - t) * 6;
        return p;
    }

    public static int[] hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0) {
            // Achromatic color (gray)
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f/3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f/3f);
        }

        return new int[] {
                Math.round(r * 255),
                Math.round(g * 255),
                Math.round(b * 255)
        };
    }

    public void exportLayout(List<VolumeBlockEntry> volumes, int structureWidth, int structureLength, String outputPath) throws IOException {
        final int IMAGE_WIDTH = 2048;
        final int IMAGE_HEIGHT = 2048;

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        int index = 0;

        List<int[]> colors = new ArrayList<>();

        for (int i = 0; i < volumes.size(); i++) {
            float t = (float) i / volumes.size();
            int[] colorRGB = hslToRgb(t, 0.5f, 0.5f);

            colors.add(colorRGB);
        }

        Collections.shuffle(colors);

        for (VolumeBlockEntry entry : volumes) {

            int[] colorRGB = colors.get(index);

            g.setColor(new Color(colorRGB[0], colorRGB[1], colorRGB[2]));

            int x = Math.round((float) entry.beginX() / structureWidth * IMAGE_WIDTH);
            int y = Math.round((float) entry.beginY() / structureLength * IMAGE_HEIGHT);
            int w = Math.round((float) entry.volume().getWidth() / structureWidth * IMAGE_WIDTH);
            int hgt = Math.round((float) entry.volume().getLength() / structureLength * IMAGE_HEIGHT);

            g.fillRect(x, y, w, hgt);

            // Draw black border
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(3)); // thickness of 3 pixels
            g.drawRect(x, y, w, hgt);

            g.setFont(new Font("Arial", Font.BOLD, 36));

            g.drawString(Integer.toString(index), x + w / 2, y + hgt / 2);

            index++;

        }

        g.dispose();
        ImageIO.write(image, "png", new File(outputPath));



    }
}
