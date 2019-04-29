package com.github.cbryant02.skribblr.util.robot;


import com.github.cbryant02.skribblr.util.Skribbl;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Adds some convenience methods for prettier code and implements an action queue.
 */
public class SkribblRobot extends Robot {
    private static final long BASE_DELAY = 1000L;

    private final int delayMul;
    private final Queue<Runnable> q;

    /**
     * Construct a new SkribblRobot with the specified {@code delayMul}.
     * @param delayMul Multiplier for automatic delay between actions. The bot uses a delay of 10ms multiplied by {@code delayMul}.<br/>
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

        // Initialize queue and start worker
        q = new ArrayDeque<>();
        new Thread(new QueueWorkerRunnable(q)).start();
    }

    /**
     * Construct a new SkribblRobot. {@code delayMul} defaults to 1.
     * @throws AWTException if the platform configuration does not allow
     * low-level input control.  This exception is always thrown when
     * GraphicsEnvironment.isHeadless() returns true
     */
    public SkribblRobot() throws AWTException {
        this(1);
    }

    /**
     * Press and release mouse with a delay.
     */
    public void mouseClick() {
        synchronized (q) {
            q.offer(() -> {
                super.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                delay();
                super.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            });
            q.notify();
        }
    }

    /**
     * Select a palette color.
     * @param color Color to select
     */
    public void select(Skribbl.Color color) {
        synchronized (q) {
            q.offer(() -> {
                super.mouseMove(color.getX(), color.getY());
                delay();
                mouseClick();
            });
            q.notify();
        }
    }

    /**
     * Select a tool.
     * @param tool Tool to select
     */
    public void select(Skribbl.Tool tool) {
        synchronized (q) {
            q.offer(() -> {
                super.mouseMove(tool.getX(), tool.getY());
                delay();
                mouseClick();
            });
            q.notify();
        }
    }

    public void testQ(int p) {
        synchronized (q) {
            q.offer(() -> System.out.println(p));
            q.notify();
        }
    }

    private void delay() {
        try {
            Thread.sleep(BASE_DELAY * delayMul);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private class QueueWorkerRunnable implements Runnable {
        private boolean dead;

        QueueWorkerRunnable(Queue<Runnable> q) {
            this.dead = false;
        }

        @Override
        public void run() {
            while(!dead) {
                try {
                    synchronized (q) {
                        while (q.isEmpty())
                            q.wait();

                        q.remove().run();
                        delay();
                    }
                } catch (InterruptedException ex) { break; }
            }
        }

        void kill() {
            dead = true;
        }
    }
}