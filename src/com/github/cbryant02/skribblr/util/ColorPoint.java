package com.github.cbryant02.skribblr.util;

import com.jwetherell.algorithms.datastructures.KdTree;

import java.awt.Color;

class ColorPoint extends KdTree.XYZPoint {

    ColorPoint(Color color) {
        super(color.getRed(), color.getGreen(), color.getBlue());
    }

    public ColorPoint(int r, int g, int b) {
        super(r, g, b);
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

    public Color toColor() {
        return new Color(getR(), getG(), getB());
    }
}
