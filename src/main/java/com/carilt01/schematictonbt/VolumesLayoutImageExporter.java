package com.carilt01.schematictonbt;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VolumesLayoutImageExporter {
    public VolumesLayoutImageExporter() {

    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f / 6f) return p + (q - p) * 6 * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6;
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
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }

        return new int[]{
                Math.round(r * 255),
                Math.round(g * 255),
                Math.round(b * 255)
        };
    }

    private static float linearInterpolate(float a, float b, float t) {
        return a + t * (b - a);
    }

    public void exportLayout(List<VolumeBlockEntry> volumes, int structureWidth, int structureLength, int structureHeight, String outputPath, ProgressCallback callback) throws IOException {
        // Base size (can scale up or down)
        final int BASE_SIZE = 2048;

        // Calculate aspect ratio
        float aspectRatio = (float) structureWidth / structureLength;

        int imageWidth, imageHeight;

        if (aspectRatio >= 1) {
            // Wider than tall
            imageWidth = BASE_SIZE;
            imageHeight = Math.round(BASE_SIZE / aspectRatio);
        } else {
            // Taller than wide
            imageHeight = BASE_SIZE;
            imageWidth = Math.round(BASE_SIZE * aspectRatio);
        }

// --- Step 1: build normalized height field ---
        float[][] heightField = new float[imageWidth][imageHeight];


        for (int x = 0; x < imageWidth; x++) {
            callback.update((float) x / imageWidth, "Creating height field...");

            for (int y = 0; y < imageHeight; y++) {
                float volumeX = ((float) x / imageWidth) * structureWidth;
                float volumeZ = ((float) y / imageHeight) * structureLength;
                int vx = (int) volumeX;
                int vz = (int) volumeZ;

                for (VolumeBlockEntry vol : volumes) {
                    if (vx < vol.beginX() || vz < vol.beginY()) continue;
                    if (vx >= vol.beginX() + vol.volume().getWidth() ||
                            vz >= vol.beginY() + vol.volume().getLength()) continue;

                    Vector3 highest = vol.volume().getHighestPositionAt(vx - vol.beginX(), vz - vol.beginY());
                    heightField[x][y] = highest.y / (float) structureHeight;
                    break;
                }
            }
        }

        // --- Step 2: generate shaded image ---
        BufferedImage heightMap = getBufferedImage(imageWidth, imageHeight, heightField);

        BufferedImage volumeGroupImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);


        Graphics2D g = volumeGroupImage.createGraphics();

        int index = 0;
        List<int[]> colors = new ArrayList<>();

        // Generate HSL-based colors
        for (int i = 0; i < volumes.size(); i++) {
            float t = (float) i / volumes.size();
            int[] colorRGB = hslToRgb(t, 0.5f, 0.5f);
            colors.add(colorRGB);
        }

        Collections.shuffle(colors);

        int progressTracker = 0;
        for (VolumeBlockEntry entry : volumes) {
            progressTracker++;

            callback.update((float) progressTracker / volumes.size(), "Coloring map...");

            int[] colorRGB = colors.get(index);
            g.setColor(new Color(colorRGB[0], colorRGB[1], colorRGB[2]));

            // Scale positions and sizes according to new image dimensions
            int x = Math.round((float) entry.beginX() / structureWidth * imageWidth);
            int y = Math.round((float) entry.beginY() / structureLength * imageHeight);
            int w = Math.round((float) entry.volume().getWidth() / structureWidth * imageWidth);
            int hgt = Math.round((float) entry.volume().getLength() / structureLength * imageHeight);


            g.fillRect(x, y, w, hgt);

            // Draw black border
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(3));
            g.drawRect(x, y, w, hgt);

            // Draw index text
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString(Integer.toString(index), x + w / 2, y + hgt / 2);

            index++;
        }

        g.dispose();

        BufferedImage outputImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < imageHeight; y++) {
            callback.update((float) y / imageHeight, "Finalizing map...");

            for (int x = 0; x < imageWidth; x++) {
                Color c1 = new Color(volumeGroupImage.getRGB(x, y));
                Color c2 = new Color(heightMap.getRGB(x, y));

                int red = (c1.getRed() * c2.getRed()) / 255;
                int green = (c1.getGreen() * c2.getGreen()) / 255;
                int blue = (c1.getBlue() * c2.getBlue()) / 255;

                outputImage.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }

        ImageIO.write(outputImage, "png", new File(outputPath));
    }

    private static BufferedImage getBufferedImage(int imageWidth, int imageHeight, float[][] heightField) {
        BufferedImage heightMap = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        // Light direction (northwest)
        float lx = -1f, lz = -1f;
        float len = (float) Math.sqrt(lx * lx + lz * lz);
        lx /= len;
        lz /= len;

        float gamma = 0.7f; // controls how altitude influences brightness

        for (int x = 1; x < imageWidth - 1; x++) {
            for (int y = 1; y < imageHeight - 1; y++) {
                float hC = heightField[x][y];
                float hL = heightField[x - 1][y];
                float hR = heightField[x + 1][y];
                float hU = heightField[x][y - 1];
                float hD = heightField[x][y + 1];

                // slope shading (small local contrast)
                float dx = hR - hL;
                float dz = hD - hU;
                float localShade = 0.5f + 0.5f * (dx * lx + dz * lz);
                localShade = Math.max(0.3f, Math.min(1.0f, localShade));

                // global height (absolute brightness)
                float globalHeight = (float) Math.pow(hC, gamma);

                float brightness = localShade * linearInterpolate(0.9f, 1.4f, globalHeight);
                brightness = Math.max(0f, Math.min(1f, brightness));

                int scalar = (int) (brightness * 255);
                heightMap.setRGB(x, y, new Color(scalar, scalar, scalar).getRGB());
            }
        }
        return heightMap;
    }

}
