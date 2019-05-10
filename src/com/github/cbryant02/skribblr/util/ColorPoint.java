package com.github.cbryant02.skribblr.util;

import com.jwetherell.algorithms.datastructures.KdTree;

import java.awt.Color;

/**
 * KdTree point that represents an RGB color value
 */
class ColorPoint extends KdTree.XYZPoint {
    private Color color;

    ColorPoint(Color color) {
        super(color.getRed(), color.getGreen(), color.getBlue());
        this.color = color;
    }

    int getR() {
        return (int)super.getX();
    }

    int getG() {
        return (int)super.getY();
    }

    int getB() {
        return (int)super.getZ();
    }
}