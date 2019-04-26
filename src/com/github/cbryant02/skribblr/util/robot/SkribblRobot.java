package com.github.cbryant02.skribblr.util.robot;


import com.github.cbryant02.skribblr.util.Skribbl;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

/**
 * Adds some convenience methods for prettier code and implements an action queue.
 */
public class SkribblRobot extends Robot {
    private final int delayMul;
    private final Queue<Runnable> actionQueue;

    /**
     * Construct a new SkribblRobot with the specified {@code delayMul}.
     * @param delayMul Multiplier for automatic delay between actions. The bot uses a delay of 10ms multiplied by {@code delayMul}.<br/>
     *                 Slower computers and internet connections may necessitate a longer delayMul in order to reliably keep up with requests.
     * @throws IllegalArgumentException if {@code delayMul} is negative or zero
     * @throws AWTException if the platform configuration does not allow
     * low-level input control.  This exception is always thrown when
     * GraphicsEnvironment.isHeadless() returns true
     */
    public SkribblRobot(int delayMul) throws IllegalArgumentException, AWTException {
        // Bounds check delayMul
        if(delayMul > 0)
            this.delayMul = delayMul;
        else throw new IllegalArgumentException("Delay multiplier passed to SkribblRobot was negative or zero");

        // Initialize queue and start worker
        actionQueue = new ArrayDeque<>();
        new Thread(new QueueWorkerRunnable(actionQueue));
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
        actionQueue.offer(() -> {
            super.mousePress(InputEvent.BUTTON1_DOWN_MASK);

            try {
                Thread.sleep(10L * delayMul);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            super.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        });
    }

    /**
     * Select a palette color.
     * @param color Color to select
     */
    public void select(Skribbl.Color color) {
        actionQueue.offer(() -> {
            super.mouseMove(color.getX(), color.getY());
            mouseClick();
        });
    }

    /**
     * Select a tool.
     * @param tool Tool to select
     */
    public void select(Skribbl.Tool tool) {
        actionQueue.offer(() -> {
            super.mouseMove(tool.getX(), tool.getY());
            mouseClick();
        });
    }

    private class QueueWorkerRunnable implements Runnable {
        private final Queue<Runnable> q;
        private boolean dead;

        QueueWorkerRunnable(Queue<Runnable> q) {
            this.q = q;
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
                    }
                } catch (InterruptedException ex) { break; }
            }
        }

        void kill() {
            dead = true;
        }
    }
}