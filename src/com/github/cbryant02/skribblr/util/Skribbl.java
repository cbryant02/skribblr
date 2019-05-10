package com.github.cbryant02.skribblr.util;

/**
 * Defines enums for colors/tools and provides their positions on the screen
 */
public interface Skribbl {
    double CANVAS_X = 500.0;
    double CANVAS_Y = 160.0;
    double CANVAS_W = 800.0;
    double CANVAS_H = 600.0;

    enum Color {
        WHITE       (0xFFFFFF, 580, 770),
        GREY        (0xC1C1C1, 604, 770),
        RED         (0xEF130B, 628, 770),
        ORANGE      (0xFF7100, 652, 770),
        YELLOW      (0xFFE400, 676, 770),
        GREEN       (0x00CC00, 700, 770),
        CYAN        (0x00B2FF, 724, 770),
        BLUE        (0x231FD3, 748, 770),
        PURPLE      (0xA300BA, 772, 770),
        PINK        (0xD37CAA, 796, 770),
        BROWN       (0xA0522D, 820, 770),
        BLACK       (0x000000, 580, 794),
        DARK_GREY   (0x4C4C4C, 604, 794),
        DARK_RED    (0x740B07, 628, 794),
        DARK_ORANGE (0xC23800, 652, 794),
        DARK_YELLOW (0xE8A200, 676, 794),
        DARK_GREEN  (0x005510, 700, 794),
        DARK_CYAN   (0x00569E, 724, 794),
        DARK_BLUE   (0x0E0865, 748, 794),
        DARK_PURPLE (0x550069, 772, 794),
        DARK_PINK   (0xA75574, 796, 794),
        DARK_BROWN  (0x63300D, 820, 794);

        // Alternative Paint palette
        /*BLACK       (0x000000, 760, 58),
        GRAY        (0x7F7F7F, 782, 58),
        DARK_RED    (0x880015, 804, 58),
        RED         (0xED1C24, 826, 58),
        ORANGE      (0xFF7F27, 848, 58),
        YELLOW      (0xFFF200, 870, 58),
        GREEN       (0x22B14C, 892, 58),
        SKY_BLUE    (0x00A2E8, 914, 58),
        BLUE        (0x3F48CC, 936, 58),
        PURPLE      (0xA349A4, 958, 58),
        WHITE       (0xFFFFFF, 760, 80),
        LIGHT_GRAY  (0xC3C3C3, 782, 80),
        LIGHT_BROWN (0xB97A57, 804, 80),
        PINK        (0xFFAEC9, 826, 80),
        CANARY      (0xFFC90E, 848, 80),
        OFF_WHITE   (0xEFE4B0, 870, 80),
        LIME_GREEN  (0xB5E61D, 892, 80),
        BABY_BLUE   (0x99D9EA, 914, 80),
        LIGHT_BLUE  (0x7092BE, 936, 80),
        PURPLE_PINK (0xC8BFE7, 958, 80);*/

        private final int color;
        private final int x;
        private final int y;

        Color(int color, int x, int y) {
            this.color = color;
            this.x = x;
            this.y = y;
        }

        public java.awt.Color getColor() {
            return new java.awt.Color(color);
        }

        public javafx.scene.paint.Color getFxColor() {
            java.awt.Color awtColor = getColor();
            return new javafx.scene.paint.Color(awtColor.getRed()/255.0, awtColor.getGreen()/255.0, awtColor.getBlue()/255.0, 1);
        }

        public int getRGB() {
            return color;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public static Color valueOf(java.awt.Color color) {
            for(Color pcolor : Color.values())
                if(pcolor.getColor().equals(color)) return pcolor;
            return null;
        }

        @Override
        public String toString() {
            return this.name().charAt(0) + this.name().substring(1).toLowerCase();
        }
    }

    enum Tool {
        PENCIL(870, 780),
        BUCKET(966, 780),
        BRUSH_SMALL(1034, 780);

        /*PENCIL(336, 69),
        BRUSH_SMALL(0,0),
        BUCKET(269, 71);*/

        private final int x;
        private final int y;

        Tool(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
