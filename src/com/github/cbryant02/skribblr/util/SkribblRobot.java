package com.github.cbryant02.skribblr.util;


import java.awt.AWTException;
import java.awt.event.InputEvent;

/**
 * Adds some convenience methods for prettier code
 */
public class SkribblRobot extends Robot {
    private static final long defaultDelay = 10L;

    private static long delay = defaultDelay;

    /**
     * Constructs a new SkribblRobot in the coordinate system of the primary screen.
     * @throws AWTException if the platform configuration does not allow
     * low-level input control.  This exception is always thrown when
     * GraphicsEnvironment.isHeadless() returns true
     */
    SkribblRobot() throws AWTException {
        super();
    }

    /**
     * Set base action delay
     * @param delay New action delay
     */
    public static void setDelay(long delay) {
        SkribblRobot.delay = delay;
    }

    /**
     * Get the default base action delay
     */
    public static long getDefaultDelay() {
        return defaultDelay;
    }

    /**
     * Press and release mouse with a delay.
     * @throws InterruptedException if current thread is interrupted while asleep
     */
    void mouseClick() throws InterruptedException {
        super.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(delay);
        super.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Press and release a key with a delay.
     * @param keycode Key to press (e.g KeyEvent.VK_A)
     * @throws InterruptedException if current thread is interrupted while asleep
     */
    @SuppressWarnings("SameParameterValue")
    void keyStroke(int keycode) throws InterruptedException {
        super.keyPress(keycode);
        Thread.sleep(delay);
        super.keyRelease(keycode);
    }

    /**
     * Select a palette color.
     * @param color Color to select
     * @throws InterruptedException if current thread is interrupted while asleep
     */
    void select(Skribbl.Color color) throws InterruptedException {
        super.mouseMove(color.getX(), color.getY());
        Thread.sleep(delay);
        mouseClick();
        mouseClick();
    }

    /**
     * Select a tool.
     * @param tool Tool to select
     * @throws InterruptedException if current thread is interrupted while asleep
     */
    void select(Skribbl.Tool tool) throws InterruptedException {
        super.mouseMove(tool.getX(), tool.getY());
        Thread.sleep(delay);
        mouseClick();
        mouseClick();
    }
}