package com.github.cbryant02.skribblr.util;

import com.jwetherell.algorithms.datastructures.KdTree;

import java.awt.Color;

/**
 * KdTree point that represents an RGB color value
 */
class ColorPoint extends KdTree.XYZPoint {

    /**
     * Construct a new ColorPoint from a {@link Color}
     * @param color Color to construct point from
     */
    ColorPoint(Color color) {
        super(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * @return Red value of this point
     */
    int getR() {
        return (int)super.getX();
    }

    /**
     * @return Green value of this point
     */
    int getG() {
        return (int)super.getY();
    }

    /**
     * @return Blue value of this point
     */
    int getB() {
        return (int)super.getZ();
    }
}