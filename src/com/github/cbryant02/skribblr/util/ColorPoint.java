package com.github.cbryant02.skribblr.util;

import com.jwetherell.algorithms.datastructures.KdTree;
import java.awt.Color;

public class ColorPoint extends KdTree.XYZPoint {

    public ColorPoint(Color color) {
        super(color.getRed(), color.getGreen(), color.getBlue());
    }

    public ColorPoint(int r, int g, int b) {
        super(r, g, b);
    }

    public int getR() {
        return (int)super.getX();
    }

    public int getG() {
        return (int)super.getY();
    }

    public int getB() {
        return (int)super.getZ();
    }

    public Color toColor() {
        return new Color(getR(), getG(), getB());
    }
}
