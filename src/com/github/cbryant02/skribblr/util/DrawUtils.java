package com.github.cbryant02.skribblr.util;

import com.jwetherell.algorithms.datastructures.KdTree;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class DrawUtils {
    private static final KdTree<ColorPoint> tree;

    static {
        tree = new KdTree<>();

        // Populate tree with palette values
        for(Palette palette : Palette.values())
            tree.add(new ColorPoint(palette.getColor()));
    }

    public static Thread draw(BufferedImage image) {
        return null;
    }

    public static BufferedImage skribblify(BufferedImage original) {
        BufferedImage clone = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        original.copyData(clone.getRaster());
        WritableRaster pixels = clone.getRaster();

        for(int x = 0; x < original.getWidth(); x++) {
            for(int y = 0; y < original.getWidth(); y++) {
                Color rgb = new Color(original.getRGB(x, y));
                ColorPoint nearest = ((ColorPoint)tree.nearestNeighbourSearch(1, new ColorPoint(rgb)).toArray()[0]);

                pixels.setPixel(x, y, new int[]{nearest.getR(), nearest.getG(), nearest.getB()});
            }
        }

        return clone;
    }

    /**
     * Split a 24-bit color integer into bytes
     * @param bits Original 24-bit color integer
     * @return RGB bytes
     */
    public static int[] splitColor(int bits) {
        int[] rgb = new int[3];

        // Mask bits
        rgb[0] = (bits & 0xFF0000);
        rgb[1] = (bits & 0x00FF00);
        rgb[2] = (bits & 0x0000FF);

        return rgb;
    }
}