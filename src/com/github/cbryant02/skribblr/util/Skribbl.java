package com.github.cbryant02.skribblr.util;

/**
 * Defines enums for color/tool positions.
 */
public interface Skribbl {
    double CANVAS_X = 500.0;
    double CANVAS_Y = 160.0;
    double CANVAS_W = 800.0;
    double CANVAS_H = 600.0;

    enum Color {
        WHITE(0xFFFFFF, 580, 770),
        GREY(0xC1C1C1, 604, 770),
        RED(0xEF130B, 628, 770),
        ORANGE(0xFF7100, 652, 770),
        YELLOW(0xFFE400, 676, 770),
        GREEN(0x00CC00, 700, 770),
        CYAN(0x00B2FF, 724, 770),
        BLUE(0x231FD3, 748, 770),
        PURPLE(0xA300BA, 772, 770),
        PINK(0xD37CAA, 796, 770),
        BROWN(0xA0522D, 820, 770),
        BLACK(0x000000, 580, 794),
        DARK_GREY(0x4C4C4C, 604, 794),
        DARK_RED(0x740B07, 628, 794),
        DARK_ORANGE(0xC23800, 652, 794),
        DARK_YELLOW(0xE8A200, 676, 794),
        DARK_GREEN(0x005510, 700, 794),
        DARK_CYAN(0x00569E, 724, 794),
        DARK_BLUE(0x0E0865, 748, 794),
        DARK_PURPLE(0x550069, 772, 794),
        DARK_PINK(0xA75574, 796, 794),
        DARK_BROWN(0x63300D, 820, 794);

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
    }

    enum Tool {
        PENCIL(870),
        ERASER(918),
        BUCKET(966);

        private final int x;
        private static final int POS_Y = 780;

        Tool(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return POS_Y;
        }
    }
}
