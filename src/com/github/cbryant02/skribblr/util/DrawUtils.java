package com.github.cbryant02.skribblr.util;

import com.github.cbryant02.skribblr.util.robot.SkribblRobot;
import com.jwetherell.algorithms.datastructures.KdTree;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.AWTException;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class DrawUtils {
    private static final KdTree<ColorPoint> tree;

    static {
        tree = new KdTree<>();

        // Populate tree with palette values
        for(Skribbl.Color color : Skribbl.Color.values())
            tree.add(new ColorPoint(color.getColor()));
    }

    /**
     * Draw an image.
     * @param image Image to draw
     * @return Drawing task, for tracking progress only. No result.
     */
    public static Task<Void> draw(final BufferedImage image) {
        return new Task<Void>() {
            @Override
            protected Void call() throws AWTException {
                SkribblRobot bot = new SkribblRobot();
                for(int y = 0; y < image.getHeight(); y++) {
                    for(int x = 0; x < image.getWidth(); x++) {
                        Skribbl.Color pixel = Skribbl.Color.valueOf(image.getRGB(x, y));

                        // Select color from palette
                        bot.select(pixel);

                        // Draw color on screen
                        bot.mouseMove(x, y);
                        bot.mouseClick();
                    }
                }
                return null;
            }
        };
    }

    /**
     * Converts the colors in an image to the closest color in the Skribbl palette.
     * <br/>
     * Returns a JavaFX image instead of an AWT image.
     * @param image Original image
     * @return Converted image
     */
    public static Image skribblify(final Image image) {
        return SwingFXUtils.toFXImage(skribblify(SwingFXUtils.fromFXImage(image, null)), null);
    }

    /**
     * Converts the colors in an image to the closest color in the Skribbl palette.
     * @param image Original image
     * @return Converted image
     */
    private static BufferedImage skribblify(final BufferedImage image) {
        BufferedImage clone = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        WritableRaster pixels = clone.getRaster();

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                java.awt.Color rgb = new java.awt.Color(image.getRGB(x, y), true);
                ColorPoint nearest = ((ColorPoint) tree.nearestNeighbourSearch(1, new ColorPoint(rgb)).toArray()[0]);

                pixels.setPixel(x, y, new int[]{nearest.getR(), nearest.getG(), nearest.getB(), rgb.getAlpha()});
            }
        }

        return clone;
    }

    /**
     * Scale an image.
     * <br/>
     * Returns a JavaFX image instead of an AWT image.
     * @param image Image to scale
     * @param factor Scale factor
     * @return Scaled image
     */
    public static Image scaleImage(final Image image, double factor) {
        return SwingFXUtils.toFXImage(scaleImage(SwingFXUtils.fromFXImage(image, null), factor/100.0), null);
    }

    /**
     * Scale an image.
     * @param image Image to scale
     * @param factor Scale factor
     * @return Scaled image
     */
    private static BufferedImage scaleImage(final BufferedImage image, double factor) {
        BufferedImage clone = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        AffineTransform transform = new AffineTransform();
        transform.scale(factor, factor);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        return op.filter(image, clone);
    }

    /**
     * Split a 24-bit color integer into bytes
     * @param bits Original 24-bit color integer
     * @return RGB bytes
     */
    public static int[] splitColor(int bits) {
        int[] rgb = new int[3];

        // Mask bits
        rgb[0] = ((bits & 0xFF0000) >> 16);
        rgb[1] = ((bits & 0x00FF00) >> 8);
        rgb[2] =  (bits & 0x0000FF);

        return rgb;
    }
}