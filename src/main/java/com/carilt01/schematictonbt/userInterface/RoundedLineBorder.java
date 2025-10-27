package com.carilt01.schematictonbt.userInterface;

import javax.swing.border.LineBorder;
import java.awt.*;

// ---- Custom Rounded Border ----
public class RoundedLineBorder extends LineBorder {
    private final int radius;

    public RoundedLineBorder(Color color, int thickness, int radius) {
        super(color, thickness, true);
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                width - thickness, height - thickness, radius, radius);
        g2.dispose();
    }
}