package com.github.cbryant02.skribblr;

import javafx.scene.image.Image;

public class DrawUtils {
    public static Thread draw(Image image) {
        return null;
    }

    public static Image skribblify() {
        return null;
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