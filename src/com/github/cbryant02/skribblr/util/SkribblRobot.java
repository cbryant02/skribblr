package com.github.cbryant02.skribblr.util;


import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * Adds some convenience methods for prettier code
 */
class SkribblRobot extends Robot {
    private static final long BASE_DELAY = 2L;

    private final int delayMul;

    /**
     * Construct a new SkribblRobot with the specified {@code delayMul}.
     * @param delayMul Multiplier for automatic delay between actions. The bot uses a delay of {@value BASE_DELAY}ms multiplied by {@code delayMul}.<br/>
     *                 Slower computers and internet connections may necessitate a longer delayMul in order to reliably keep up with requests.
     * @throws IllegalArgumentException if {@code delayMul} is negative or zero
     * @throws AWTException if the platform configuration does not allow
     * low-level input control.  This exception is always thrown when
     * GraphicsEnvironment.isHeadless() returns true
     */
    private SkribblRobot(int delayMul) throws IllegalArgumentException, AWTException {
        // Bounds check delayMul
        if(delayMul > 0)
            this.delayMul = delayMul;
        else throw new IllegalArgumentException("Delay multiplier passed to SkribblRobot was negative or zero");
    }

    /**
     * Construct a new SkribblRobot. {@code delayMul} defaults to 1.
     * @throws AWTException if the platform configuration does not allow
     * low-level input control.  This exception is always thrown when
     * GraphicsEnvironment.isHeadless() returns true
     */
    SkribblRobot() throws AWTException {
        this(1);
    }

    /**
     * Press and release mouse with a delay.
     */
    void mouseClick() {
        super.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        delay();
        super.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Select a palette color.
     * @param color Color to select
     */
    void select(Skribbl.Color color) {
        super.mouseMove(color.getX(), color.getY());
        delay();
        mouseClick();
    }

    /**
     * Select a tool.
     * @param tool Tool to select
     */
    public void select(Skribbl.Tool tool) {
        super.mouseMove(tool.getX(), tool.getY());
        delay();
        mouseClick();
    }

    private void delay() {
        try {
            Thread.sleep(BASE_DELAY * delayMul);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }
}