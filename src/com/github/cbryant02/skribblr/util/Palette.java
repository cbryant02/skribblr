package com.github.cbryant02.skribblr.util;

import java.awt.Color;

public enum Palette {
    WHITE(0xFFFFFF, 0, 0),
    GREY(0xC1C1C1, 1, 0),
    RED(0xEF130B, 2, 0),
    ORANGE(0xFF7100, 3, 0),
    YELLOW(0xFFE400, 4, 0),
    GREEN(0x00CC00, 5, 0),
    CYAN(0x00B2FF, 6, 0),
    BLUE(0x231FD3, 7, 0),
    PURPLE(0xA300BA, 8, 0),
    PINK(0xD37CAA, 9, 0),
    BROWN(0xA0522D, 10, 0),
    BLACK(0x000000, 0, 1),
    DARK_GREY(0x4C4C4C, 1, 1),
    DARK_RED(0x740B07, 2, 1),
    DARK_ORANGE(0xC23800, 3, 1),
    DARK_YELLOW(0xE8A200, 4, 1),
    DARK_GREEN(0x005510, 5, 1),
    DARK_CYAN(0x00569E, 6, 1),
    DARK_BLUE(0x0E0865, 7, 1),
    DARK_PURPLE(0x550069, 8, 1),
    DARK_PINK(0xA75574, 9, 1),
    DARK_BROWN(0x63300D, 10, 1);

    private int color, posX, posY;

    Palette(int color, int posX, int posY) {
        this.color = color;
        this.posX = posX;
        this.posY = posY;
    }

    public Color getColor() {
        return new Color(color);
    }
}
