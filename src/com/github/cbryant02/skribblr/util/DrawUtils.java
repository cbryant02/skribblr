package com.github.cbryant02.skribblr.util;

import com.jwetherell.algorithms.datastructures.KdTree;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Static utility methods for image processing and drawing
 */
public class DrawUtils {
    private static final KdTree<ColorPoint> tree;
    private static Skribbl.Color bgColor = Skribbl.Color.WHITE;

    static {
        tree = new KdTree<>();

        // Populate tree with palette values
        for(Skribbl.Color color : Skribbl.Color.values())
            tree.add(new ColorPoint(color.getColor()));
    }

    /**
     * Draw an image
     * @param image Image to draw
     * @return Drawing task, for tracking progress only. No result.
     */
    public static Task<Void> draw(final Image image) {
        return draw(SwingFXUtils.fromFXImage(image, null));
    }

    /**
     * Draw an image.
     * @param image BufferedImage to draw
     * @return Drawing task, for tracking progress only. No result.
     */
    private static Task<Void> draw(final BufferedImage image) {
        final SkribblRobot bot;
        try {
            bot = new SkribblRobot();
        } catch (AWTException ignore) { return null; }
        bot.setAutoDelay(0);

        return new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    // Enter fullscreen if not in fullscreen/using skribbl palette
                    if (Skribbl.Color.values()[0].getRGB() != 0) {
                        if (bot.getPixelColor(312, 32).getRGB() != 0xFFE80920) {
                            bot.mouseMove(610, 200);
                            bot.mouseClick();
                            bot.keyStroke(KeyEvent.VK_F11);
                            Thread.sleep(1500L);
                        }
                    }

                    // Fill background
                    Thread.sleep(20L);
                    bot.select(Skribbl.Tool.BUCKET);
                    bot.select(bgColor);
                    bot.mouseMove((int) Skribbl.CANVAS_X, (int) Skribbl.CANVAS_Y);
                    bot.mouseClick();
                    bot.select(Skribbl.Tool.PENCIL);

                    // Set brush size
                    Thread.sleep(20L);
                    bot.select(Skribbl.Tool.BRUSH_SMALL);
                    bot.mouseMove((int) Skribbl.CANVAS_X, (int) Skribbl.CANVAS_Y);
                    bot.mouseClick();
                    Thread.sleep(20L);
                    bot.mouseWheel(-1);

                /* We want to draw as large as possible, so let's "scale" the image back up to the canvas size
                 * This is achieved simply by multiplying the position of each pixel by the scaling factor that would
                 * make it match the canvas dimensions */
                double scale;
                if(image.getWidth() > image.getHeight()) scale = Skribbl.CANVAS_W / image.getWidth();
                else                                     scale = Skribbl.CANVAS_H / image.getHeight();

                int progress = 0;
                int max = image.getWidth() * image.getHeight();

                    for (int y = 0; y < image.getHeight(); y++) {
                        if (y > Skribbl.CANVAS_H)
                            break;

                        for (int x = 0; x < image.getWidth(); x++) {
                            try {
                                updateProgress(++progress, max);
                                Skribbl.Color pixel = Skribbl.Color.valueOf(new Color(image.getRGB(x, y)));

                                // Skip background color / transparent pixels
                                if (new Color(image.getRGB(x, y), true).getAlpha() < 255 || pixel == bgColor)
                                    continue;

                                // Select color from palette
                                bot.select(pixel);

                                // Draw color on screen
                                bot.mouseMove((int) ((x * scale) + Skribbl.CANVAS_X), (int) ((y * scale) + Skribbl.CANVAS_Y));
                                bot.mouseClick();
                            } catch (Exception ex) {
                                updateProgress(1,1);
                                return null;
                            }
                        }
                    }
                } catch (InterruptedException ex) { return null; }

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
                Color oldPixel = new Color(image.getRGB(x, y), true);
                ColorPoint newPixel = ((ColorPoint) tree.nearestNeighbourSearch(1, new ColorPoint(oldPixel)).toArray()[0]);
                pixels.setPixel(x, y, new int[]{newPixel.getR(), newPixel.getG(), newPixel.getB(), oldPixel.getAlpha()});
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
        return SwingFXUtils.toFXImage(scaleImage(SwingFXUtils.fromFXImage(image, null), factor), null);
    }

    /**
     * Scale an image.
     * @param image Image to scale
     * @param factor Scale factor
     * @return Scaled image
     */
    private static BufferedImage scaleImage(final BufferedImage image, double factor) {
        BufferedImage clone = new BufferedImage((int)(image.getWidth()*factor), (int)(image.getHeight()*factor), image.getType());

        AffineTransform transform = new AffineTransform();
        transform.scale(factor, factor);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        return op.filter(image, clone);
    }

    /**
     * Set the current background color.
     * @param bgColor New background color
     */
    public static void setBgColor(Skribbl.Color bgColor) {
        DrawUtils.bgColor = bgColor;
    }
}